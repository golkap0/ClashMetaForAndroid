package com.github.kr328.clash.service.model

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.util.Parcelizer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

@Serializable
data class LibuzConfig(
    val authUser: String = "",
    val serverHost: String = "",
    val obfsKey: String = "",
    val upLimit: String = "1 Mbps",
    val downLimit: String = "1 Mbps",
    val recvWinConn: Int = 262144,
    val recvWin: Int = 4194304,
    val coreCount: Int = 4,
    val enabled: Boolean = false
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        Parcelizer.encodeToParcel(serializer(), parcel, this)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LibuzConfig> {
        override fun createFromParcel(parcel: Parcel): LibuzConfig {
            return Parcelizer.decodeFromParcel(serializer(), parcel)
        }

        override fun newArray(size: Int): Array<LibuzConfig?> {
            return arrayOfNulls(size)
        }
    }
}
