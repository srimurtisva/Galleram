package com.srimurtiseva.galleram.utils

import java.io.InputStream
import java.security.MessageDigest

object HashUtils {
    /**
     * Calculates the SHA-256 hash of an input stream.
     * Uses a buffer to avoid loading the entire file into memory.
     */
    fun calculateSHA256(inputStream: InputStream): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            val hashBytes = digest.digest()
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream.close()
        }
    }
}
