package com.github.kr328.clash.service.clash.module

import android.app.Service
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.service.StatusProvider
import com.github.kr328.clash.service.data.ImportedDao
import com.github.kr328.clash.service.data.SelectionDao
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.service.util.importedDir
import com.github.kr328.clash.service.util.sendProfileLoaded
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import java.util.*

class ConfigurationModule(service: Service) : Module<ConfigurationModule.LoadException>(service) {
    data class LoadException(val message: String)

    private val store = ServiceStore(service)
    private val reload = Channel<Unit>(Channel.CONFLATED)

    override suspend fun run() {
        val broadcasts = receiveBroadcast {
            addAction(Intents.ACTION_PROFILE_CHANGED)
            addAction(Intents.ACTION_OVERRIDE_CHANGED)
        }

        var loaded: UUID? = null

        reload.trySend(Unit)

        while (true) {
            val changed: UUID? = select {
                broadcasts.onReceive {
                    if (it.action == Intents.ACTION_PROFILE_CHANGED)
                        UUID.fromString(it.getStringExtra(Intents.EXTRA_UUID))
                    else
                        null
                }
                reload.onReceive {
                    null
                }
            }

            try {
                val zivpnUUID = UUID(0, 0)
                if (store.zivpnEnabled) {
                    if (loaded == zivpnUUID && changed == null)
                        continue

                    loaded = zivpnUUID

                    val configDir = service.cacheDir.resolve("zivpn").apply { mkdirs() }
                    val configFile = configDir.resolve("config.yaml")
                    configFile.writeText("proxies: [{name: \"ZIVPN-Core\", type: socks5, server: \"127.0.0.1\", port: 7777}]\nproxy-groups: [{name: PROXY, type: select, proxies: [\"ZIVPN-Core\"]}]\nrules: [\"MATCH,PROXY\"]")

                    Clash.load(configDir).await()

                    val zivpnOverride = com.github.kr328.clash.core.model.ConfigurationOverride().apply {
                        mixedPort = 7890
                        allowLan = false
                        mode = com.github.kr328.clash.core.model.TunnelState.Mode.Rule
                        logLevel = com.github.kr328.clash.core.model.LogMessage.Level.Silent
                        externalController = "127.0.0.1:9090"
                        ipv6 = false
                        geodataMode = true
                        dns.apply {
                            enable = true
                            ipv6 = false
                            listen = "0.0.0.0:1053"
                            enhancedMode = com.github.kr328.clash.core.model.ConfigurationOverride.DnsEnhancedMode.FakeIp
                            nameServer = listOf("https://1.1.1.1/dns-query", "https://8.8.8.8/dns-query")
                            fallback = listOf("https://1.0.0.1/dns-query", "https://8.8.4.4/dns-query")
                            fallbackFilter.geoIp = false
                            fallbackFilter.ipcidr = listOf("240.0.0.0/4")
                        }
                    }
                    Clash.patchOverride(Clash.OverrideSlot.Session, zivpnOverride)

                    StatusProvider.currentProfile = "ZIVPN"

                    service.sendProfileLoaded(zivpnUUID)

                    Log.d("ZIVPN Profile loaded")

                    continue
                }

                val current = store.activeProfile
                    ?: throw NullPointerException("No profile selected")

                if (current == loaded && changed != null && changed != loaded)
                    continue

                loaded = current

                val active = ImportedDao().queryByUUID(current)
                    ?: throw NullPointerException("No profile selected")

                Clash.load(service.importedDir.resolve(active.uuid.toString())).await()

                val remove = SelectionDao().querySelections(active.uuid)
                    .filterNot { Clash.patchSelector(it.proxy, it.selected) }
                    .map { it.proxy }

                SelectionDao().removeSelections(active.uuid, remove)

                StatusProvider.currentProfile = active.name

                service.sendProfileLoaded(current)

                Log.d("Profile ${active.name} loaded")
            } catch (e: Exception) {
                return enqueueEvent(LoadException(e.message ?: "Unknown"))
            }
        }
    }
}