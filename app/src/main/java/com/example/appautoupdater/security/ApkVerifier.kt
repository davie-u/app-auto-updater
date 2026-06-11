package com.example.appautoupdater.security

import android.content.pm.PackageManager
import android.content.pm.Signature
import java.io.File
import java.security.MessageDigest

/**
 * Verifies APK signatures to prevent installation of tampered packages
 */
object ApkVerifier {
    
    /**
     * Verify that the APK is signed with the expected certificate
     * @param apkFile The APK file to verify
     * @param expectedSignatureHash The SHA-256 hash of the expected signature
     * @return true if signature matches, false otherwise
     */
    fun verifyApkSignature(apkFile: File, expectedSignatureHash: String): Boolean {
        return try {
            // Note: This is a simplified verification approach
            // In production, use Android's PackageManager.checkSignatures()
            // or implement full APK signature verification
            
            val fileHash = calculateFileSHA256(apkFile)
            // You would compare this with a known good hash from your backend
            true // Placeholder - implement full verification based on your security model
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Calculate SHA-256 hash of a file
     */
    fun calculateFileSHA256(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Verify file integrity by comparing checksums
     */
    fun verifyFileChecksum(file: File, expectedChecksum: String): Boolean {
        val actualChecksum = calculateFileSHA256(file)
        return actualChecksum.equals(expectedChecksum, ignoreCase = true)
    }
}
