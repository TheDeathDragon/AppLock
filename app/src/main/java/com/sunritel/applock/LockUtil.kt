package com.sunritel.applock

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object LockUtil {
    private const val mTAG = "Rin"
    fun sha256(plaintext: String): String? {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(plaintext.toByteArray())
            val digest = md.digest()
            val sb = StringBuilder()
            for (b in digest) {
                sb.append(String.format("%02x", b.toInt() and 0xff))
            }
            sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            log("Sha256 Error: $e")
            null
        }
    }

    fun log(message: String) {
        val isUserDebug = android.os.Build.TYPE == "userdebug"
        if (isUserDebug) {
            android.util.Log.d(mTAG, message)
        }
    }
}