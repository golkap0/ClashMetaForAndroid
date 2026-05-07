package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.design.databinding.DesignSettingsMetaFeatureBinding
import com.github.kr328.clash.design.preference.*
import com.github.kr328.clash.design.util.*
import com.github.kr328.clash.service.store.ServiceStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MetaFeatureSettingsDesign(
    context: Context,
    configuration: ConfigurationOverride,
    store: ServiceStore
) : Design<MetaFeatureSettingsDesign.Request>(context) {
    enum class Request {
        ResetOverride, ImportGeoIp, ImportGeoSite, ImportCountry, ImportASN,
        ImportZivpnAccount, ExportZivpnAccount
    }

    private val binding = DesignSettingsMetaFeatureBinding
        .inflate(context.layoutInflater, context.root, false)

    override val root: View
        get() = binding.root

    suspend fun requestResetConfirm(): Boolean {
        return suspendCancellableCoroutine { ctx ->
            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(R.string.reset_override_settings)
                .setMessage(R.string.reset_override_settings_message)
                .setPositiveButton(R.string.ok) { _, _ -> ctx.resume(true) }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()

            dialog.setOnDismissListener {
                if (!ctx.isCompleted)
                    ctx.resume(false)
            }

            ctx.invokeOnCancellation {
                dialog.dismiss()
            }
        }
    }

    init {
        binding.self = this

        binding.activityBarLayout.applyFrom(context)

        binding.scrollRoot.bindAppBarElevation(binding.activityBarLayout)

        val booleanValues: Array<Boolean?> = arrayOf(
            null,
            true,
            false
        )
        val booleanValuesText: Array<Int> = arrayOf(
            R.string.dont_modify,
            R.string.enabled,
            R.string.disabled
        )

        val screen = preferenceScreen(context) {
            category(R.string.settings)

            selectableList(
                value = configuration::unifiedDelay,
                values = booleanValues,
                valuesText = booleanValuesText,
                title = R.string.unified_delay,
            )

            selectableList(
                value = configuration::geodataMode,
                values = booleanValues,
                valuesText = booleanValuesText,
                title = R.string.geodata_mode,
            )

            selectableList(
                value = configuration::tcpConcurrent,
                values = booleanValues,
                valuesText = booleanValuesText,
                title = R.string.tcp_concurrent,
            )

            selectableList(
                value = configuration::findProcessMode,
                values = arrayOf(
                    null,
                    ConfigurationOverride.FindProcessMode.Off,
                    ConfigurationOverride.FindProcessMode.Strict,
                    ConfigurationOverride.FindProcessMode.Always
                ),
                valuesText = arrayOf(
                    R.string.dont_modify,
                    R.string.off,
                    R.string.strict,
                    R.string.always,
                ),
                title = R.string.find_process_mode,
            ) {

            }

            category(R.string.sniffer_setting)

            val snifferDependencies: MutableList<Preference> = mutableListOf()

            val sniffer = selectableList(
                value = configuration.sniffer::enable,
                values = arrayOf(
                    null,
                    true,
                    false
                ),
                valuesText = arrayOf(
                    R.string.dont_modify,
                    R.string.enabled,
                    R.string.disabled
                ),
                title = R.string.strategy
            ) {
                listener = OnChangedListener {
                    if (configuration.sniffer.enable == false) {
                        snifferDependencies.forEach {
                            it.enabled = false
                        }
                    } else {
                        snifferDependencies.forEach {
                            it.enabled = true
                        }
                    }
                }
            }

            editableTextList(
                value = configuration.sniffer.sniff.http::ports,
                adapter = TextAdapter.String,
                title = R.string.sniff_http_ports,
                placeholder = R.string.dont_modify,
                configure = snifferDependencies::add,
            )

            selectableList(
                value = configuration.sniffer.sniff.http::overrideDestination,
                values = booleanValues,
                valuesText = booleanValuesText,
                title = R.string.sniff_http_override_destination,
                configure = snifferDependencies::add,
            )

            editableTextList(
                value = configuration.sniffer.sniff.tls::ports,
                adapter = TextAdapter.String,
                title = R.string.sniff_tls_ports,
                placeholder = R.string.dont_modify,
                configure = snifferDependencies::add,
            )

            selectableList(
                value = configuration.sniffer.sniff.tls::overrideDestination,
                values = booleanValues,
                valuesText = booleanValuesText,
                title = R.string.sniff_tls_override_destination,
                configure = snifferDependencies::add,
            )

            editableTextList(
                value = configuration.sniffer.sniff.quic::ports,
                adapter = TextAdapter.String,
                title = R.string.sniff_quic_ports,
                placeholder = R.string.dont_modify,
                configure = snifferDependencies::add,
            )

            selectableList(
                value = configuration.sniffer.sniff.quic::overrideDestination,
                values = booleanValues,
                valuesText = booleanValuesText,
                title = R.string.sniff_quic_override_destination,
                configure = snifferDependencies::add,
            )

            selectableList(
                value = configuration.sniffer::forceDnsMapping,
                values = booleanValues,
                valuesText = booleanValuesText,
                title = R.string.force_dns_mapping,
                configure = snifferDependencies::add,
            )

            selectableList(
                value = configuration.sniffer::parsePureIp,
                values = booleanValues,
                valuesText = booleanValuesText,
                title = R.string.parse_pure_ip,
                configure = snifferDependencies::add,
            )

            selectableList(
                value = configuration.sniffer::overrideDestination,
                values = booleanValues,
                valuesText = booleanValuesText,
                title = R.string.override_destination,
                configure = snifferDependencies::add,
            )

            editableTextList(
                value = configuration.sniffer::forceDomain,
                adapter = TextAdapter.String,
                title = R.string.force_domain,
                placeholder = R.string.dont_modify,
                configure = snifferDependencies::add,
            )

            editableTextList(
                value = configuration.sniffer::skipDomain,
                adapter = TextAdapter.String,
                title = R.string.skip_domain,
                placeholder = R.string.dont_modify,
                configure = snifferDependencies::add,
            )

            editableTextList(
                value = configuration.sniffer::skipSrcAddress,
                adapter = TextAdapter.String,
                title = R.string.skip_src_address,
                placeholder = R.string.dont_modify,
                configure = snifferDependencies::add,
            )

            editableTextList(
                value = configuration.sniffer::skipDstAddress,
                adapter = TextAdapter.String,
                title = R.string.skip_dst_address,
                placeholder = R.string.dont_modify,
                configure = snifferDependencies::add,
            )

            sniffer.listener?.onChanged()

            /*
            category(R.string.geox_url_setting)

            val geoxUrlDependencies: MutableList<Preference> = mutableListOf()

            editableText(
                value = configuration.geoxurl::geoip,
                adapter = NullableTextAdapter.String,
                title = R.string.geox_geoip,
                placeholder = R.string.dont_modify,
                empty = R.string.geoip_url,
                configure = geoxUrlDependencies::add,
            )

            editableText(
                value = configuration.geoxurl::mmdb,
                adapter = NullableTextAdapter.String,
                title = R.string.geox_mmdb,
                placeholder = R.string.dont_modify,
                empty = R.string.mmdb_url,
                configure = geoxUrlDependencies::add,
            )

            editableText(
                value = configuration.geoxurl::geosite,
                adapter = NullableTextAdapter.String,
                title = R.string.geox_geosite,
                placeholder = R.string.dont_modify,
                empty = R.string.geosite_url,
                configure = geoxUrlDependencies::add,
            )
            */

            category(R.string.geox_files)

            clickable (
                title = R.string.import_geoip_file,
                summary = R.string.press_to_import,
            ){
                clicked {
                    requests.trySend(Request.ImportGeoIp)
                }
            }

            clickable (
                title = R.string.import_geosite_file,
                summary = R.string.press_to_import,
            ){
                clicked {
                    requests.trySend(Request.ImportGeoSite)
                }
            }

            clickable (
                title = R.string.import_country_file,
                summary = R.string.press_to_import,
            ){
                clicked {
                    requests.trySend(Request.ImportCountry)
                }
            }
            
            clickable (
                title = R.string.import_asn_file,
                summary = R.string.press_to_import,
            ){
                clicked {
                    requests.trySend(Request.ImportASN)
                }
            }

            category(R.string.zivpn_settings)

            switch(
                value = store::zivpnEnabled,
                title = R.string.zivpn_enabled,
            )

            editableText(
                value = store::zivpnAuthUser,
                adapter = TextAdapter.String,
                title = R.string.zivpn_auth_user,
            )

            editableText(
                value = store::zivpnServerHost,
                adapter = TextAdapter.String,
                title = R.string.zivpn_server_host,
            )

            editableText(
                value = store::zivpnObfsKey,
                adapter = TextAdapter.String,
                title = R.string.zivpn_obfs_key,
            )

            val coreCounts = Array(10) { it + 1 }
            selectableList(
                value = store::zivpnCoreCount,
                values = coreCounts,
                valuesText = coreCounts.map { it.toString() },
                title = R.string.zivpn_core_count,
            )

            editableText(
                value = store::zivpnUpLimit,
                adapter = TextAdapter.String,
                title = R.string.zivpn_up_limit,
            )

            editableText(
                value = store::zivpnDownLimit,
                adapter = TextAdapter.String,
                title = R.string.zivpn_down_limit,
            )

            editableText(
                value = store::zivpnRecvWinConn,
                adapter = object : TextAdapter<Int> {
                    override fun from(value: Int): String = value.toString()
                    override fun to(text: String): Int = text.toIntOrNull() ?: 0
                },
                title = R.string.zivpn_recv_win_conn,
            )

            editableText(
                value = store::zivpnRecvWin,
                adapter = object : TextAdapter<Int> {
                    override fun from(value: Int): String = value.toString()
                    override fun to(text: String): Int = text.toIntOrNull() ?: 0
                },
                title = R.string.zivpn_recv_win,
            )

            clickable(
                title = R.string.import_zivpn_account,
            ) {
                clicked {
                    requests.trySend(Request.ImportZivpnAccount)
                }
            }

            clickable(
                title = R.string.export_zivpn_account,
            ) {
                clicked {
                    requests.trySend(Request.ExportZivpnAccount)
                }
            }
        }

        binding.content.addView(screen.root)
    }

    fun requestClear() {
        requests.trySend(Request.ResetOverride)
    }
}
