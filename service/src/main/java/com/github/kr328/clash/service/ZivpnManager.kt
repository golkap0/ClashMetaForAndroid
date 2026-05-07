package com.github.kr328.clash.service

import android.content.Context
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.service.store.ServiceStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class ZivpnManager(private val context: Context) {
    private val store = ServiceStore(context)
    private val processes = mutableListOf<Process>()

    suspend fun start() = withContext(Dispatchers.IO) {
        if (!store.zivpnEnabled) return@withContext

        stop()

        val libDir = context.applicationInfo.nativeLibraryDir
        val libuz = File(libDir, "libuz.so")
        val libload = File(libDir, "libload.so")

        if (!libuz.exists() || !libload.exists()) {
            Log.e("ZIVPN binaries not found in $libDir")
            return@withContext
        }

        val serverPorts = "6000-7750,7751-9500,9501-11225,11251-13000,13001-14750,14751-16500,16501-18250,18251-19999"
        val tunnels = mutableListOf<String>()

        var host = store.zivpnServerHost
        var auth = store.zivpnAuthUser

        val accounts = store.zivpnAccounts
        val selected = store.zivpnSelectedAccount
        if (selected >= 0 && selected < accounts.size) {
            val content = accounts[selected].substringAfter("zivpn://")
            val parts = content.split("@")
            if (parts.size == 2) {
                host = parts[0]
                auth = parts[1]
            }
        }

        for (i in 0 until store.zivpnCoreCount) {
            val port = 1080 + i
            val json = JSONObject().apply {
                put("server", "$host:$serverPorts")
                put("obfs", store.zivpnObfsKey)
                put("auth", auth)
                put("socks5", JSONObject().put("listen", "127.0.0.1:$port"))
                put("insecure", true)
                if (store.zivpnUpLimit != "0") put("up", store.zivpnUpLimit)
                if (store.zivpnDownLimit != "0") put("down", store.zivpnDownLimit)
                put("recvwindowconn", store.zivpnRecvWinConn)
                put("recvwindow", store.zivpnRecvWin)
            }

            try {
                val pb = ProcessBuilder(libuz.absolutePath, "-s", store.zivpnObfsKey, "--config", json.toString())
                    .redirectOutput(ProcessBuilder.Redirect.to(File("/dev/null")))
                    .redirectError(ProcessBuilder.Redirect.to(File("/dev/null")))
                val p = pb.start()
                processes.add(p)
                tunnels.add("127.0.0.1:$port")
                Log.i("Started libuz on port $port")
            } catch (e: Exception) {
                Log.e("Failed to start libuz: ${e.message}")
            }
        }

        if (tunnels.isNotEmpty()) {
            try {
                val args = mutableListOf(libload.absolutePath, "-lhost", "127.0.0.1", "-lport", "7777", "-tunnel")
                args.addAll(tunnels)
                val pb = ProcessBuilder(args)
                    .redirectOutput(ProcessBuilder.Redirect.to(File("/dev/null")))
                    .redirectError(ProcessBuilder.Redirect.to(File("/dev/null")))
                val p = pb.start()
                processes.add(p)
                Log.i("Started libload on port 7777")
            } catch (e: Exception) {
                Log.e("Failed to start libload: ${e.message}")
            }
        }
    }

    fun stop() {
        processes.forEach {
            it.destroy()
            try {
                it.waitFor()
            } catch (e: Exception) {
                // Ignore
            }
        }
        processes.clear()
        Log.i("Stopped all ZIVPN processes")
    }
}
