package com.github.kr328.clash.service.store

import android.content.Context
import com.github.kr328.clash.common.store.Store
import com.github.kr328.clash.common.store.asStoreProvider
import com.github.kr328.clash.service.PreferenceProvider
import com.github.kr328.clash.service.model.AccessControlMode
import java.util.*

class ServiceStore(context: Context) {
    private val store = Store(
        PreferenceProvider
            .createSharedPreferencesFromContext(context)
            .asStoreProvider()
    )

    var activeProfile: UUID? by store.typedString(
        key = "active_profile",
        from = { if (it.isBlank()) null else UUID.fromString(it) },
        to = { it?.toString() ?: "" }
    )

    var bypassPrivateNetwork: Boolean by store.boolean(
        key = "bypass_private_network",
        defaultValue = true
    )

    var accessControlMode: AccessControlMode by store.enum(
        key = "access_control_mode",
        defaultValue = AccessControlMode.AcceptAll,
        values = AccessControlMode.values()
    )

    var accessControlPackages by store.stringSet(
        key = "access_control_packages",
        defaultValue = emptySet()
    )

    var dnsHijacking by store.boolean(
        key = "dns_hijacking",
        defaultValue = true
    )

    var systemProxy by store.boolean(
        key = "system_proxy",
        defaultValue = true
    )

    var allowBypass by store.boolean(
        key = "allow_bypass",
        defaultValue = true
    )

    var allowIpv6 by store.boolean(
        key = "allow_ipv6",
        defaultValue = false
    )

    var tunStackMode by store.string(
        key = "tun_stack_mode",
        defaultValue = "system"
    )

    var dynamicNotification by store.boolean(
        key = "dynamic_notification",
        defaultValue = true
    )

    var zivpnEnabled: Boolean by store.boolean(
        key = "zivpn_enabled",
        defaultValue = false
    )

    var zivpnAuthUser: String by store.string(
        key = "zivpn_auth_user",
        defaultValue = "vpnstunnel-bnml0"
    )

    var zivpnServerHost: String by store.string(
        key = "zivpn_server_host",
        defaultValue = "ssh-2.chice.me"
    )

    var zivpnObfsKey: String by store.string(
        key = "zivpn_obfs_key",
        defaultValue = "hu``hqb`c"
    )

    var zivpnCoreCount: Int by store.int(
        key = "zivpn_core_count",
        defaultValue = 4
    )

    var zivpnUpLimit: String by store.string(
        key = "zivpn_up_limit",
        defaultValue = "1 Mbps"
    )

    var zivpnDownLimit: String by store.string(
        key = "zivpn_down_limit",
        defaultValue = "1 Mbps"
    )

    var zivpnRecvWinConn: Int by store.int(
        key = "zivpn_recv_win_conn",
        defaultValue = 262144
    )

    var zivpnRecvWin: Int by store.int(
        key = "zivpn_recv_win",
        defaultValue = 4194304
    )

    var zivpnAccounts: List<String>
        get() = store.provider.getString("zivpn_accounts", "")?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()
        set(value) { store.provider.setString("zivpn_accounts", value.joinToString("\n")) }

    var zivpnSelectedAccount: Int by store.int(
        key = "zivpn_selected_account",
        defaultValue = 0
    )
}