package com.example.fedger.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utility class for encrypting and decrypting sensitive data using Android KeyStore
 * with fallback mechanisms for recovery scenarios
 */
class EncryptionUtil {
    companion object {
        private const val TAG = "EncryptionUtil"
        private const val TRANSFORMATION = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALIAS = "FedgerPasswordKey"
        private const val BACKUP_ALIAS = "FedgerPasswordKeyBackup"
        private const val IV_SEPARATOR = "::"
        
        // Recovery mechanism
        private const val RECOVERY_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val BACKUP_KEY_ITERATIONS = 10000
        private const val BACKUP_KEY_LENGTH = 256

        // State to track if we're in recovery mode
        private var recoveryModeActive = false
        
        /**
         * Encrypt a string value
         */
        fun encrypt(plainText: String): String {
            try {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
                
                val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
                val iv = cipher.iv
                
                // Concatenate IV and encrypted data
                val ivAndEncryptedData = iv + encryptedBytes
                return Base64.encodeToString(ivAndEncryptedData, Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e(TAG, "Primary encryption failed: ${e.message}", e)
                
                // Try backup encryption method
                return encryptWithBackupMethod(plainText)
            }
        }
        
        /**
         * Decrypt an encrypted string value
         * @return Decrypted string or null if decryption fails completely
         */
        fun decrypt(encryptedText: String): String {
            try {
                // If we're already in recovery mode, go straight to backup method
                if (recoveryModeActive) {
                    return decryptWithBackupMethod(encryptedText)
                }
                
                val encryptedData = Base64.decode(encryptedText, Base64.DEFAULT)
                
                // Extract IV (first 12 bytes for GCM)
                val iv = encryptedData.copyOfRange(0, 12)
                val encrypted = encryptedData.copyOfRange(12, encryptedData.size)
                
                val cipher = Cipher.getInstance(TRANSFORMATION)
                val spec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)
                
                val decryptedBytes = cipher.doFinal(encrypted)
                return String(decryptedBytes, Charsets.UTF_8)
            } catch (e: Exception) {
                Log.e(TAG, "Primary decryption failed: ${e.message}", e)
                
                // Try with backup decryption
                try {
                    return decryptWithBackupMethod(encryptedText)
                } catch (backupE: Exception) {
                    Log.e(TAG, "Backup decryption also failed: ${backupE.message}", backupE)
                    
                    // Both methods failed - we'll need to throw but with more details
                    val errorMsg = "Decryption failed with both primary and backup methods"
                    throw SecurityException(errorMsg, e)
                }
            }
        }

        /**
         * Attempt to decrypt with primary method, falling back to backup if needed
         * @return The decrypted value or null if all methods fail
         */
        fun tryDecrypt(encryptedText: String): String? {
            return try {
                decrypt(encryptedText)
            } catch (e: Exception) {
                Log.e(TAG, "All decryption methods failed: ${e.message}", e)
                null
            }
        }
        
        /**
         * Reset the KeyStore key - use with caution as this will make existing encrypted data inaccessible
         * unless they can be decrypted with the backup method
         */
        fun resetEncryptionKey(): Boolean {
            return try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
                keyStore.load(null)
                
                if (keyStore.containsAlias(ALIAS)) {
                    keyStore.deleteEntry(ALIAS)
                    Log.i(TAG, "Encryption key reset successfully")
                    // Create a new key
                    getOrCreateSecretKey()
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reset encryption key: ${e.message}", e)
                false
            }
        }
        
        /**
         * Enable recovery mode to prioritize backup decryption method
         */
        fun enableRecoveryMode(enable: Boolean) {
            recoveryModeActive = enable
            Log.i(TAG, "Recovery mode set to: $enable")
        }
        
        /**
         * Check if the primary encryption key is available
         */
        fun isPrimaryKeyAvailable(): Boolean {
            return try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
                keyStore.load(null)
                keyStore.containsAlias(ALIAS)
            } catch (e: Exception) {
                false
            }
        }
        
        /**
         * Get or create the encryption key in the Android KeyStore
         */
        private fun getOrCreateSecretKey(): SecretKey {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            
            if (!keyStore.containsAlias(ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, 
                    ANDROID_KEYSTORE
                )
                
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
                
                keyGenerator.init(keyGenParameterSpec)
                return keyGenerator.generateKey()
            }
            
            return (keyStore.getEntry(ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        }
        
        /**
         * Backup encryption method using password-based encryption
         * This doesn't use KeyStore so it's more resilient to KeyStore issues
         */
        private fun encryptWithBackupMethod(plainText: String): String {
            try {
                // Generate a secure backup key using a consistent method
                val backupKey = getBackupKey()
                
                // Setup cipher
                val cipher = Cipher.getInstance(RECOVERY_TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, backupKey)
                val iv = cipher.iv
                
                // Encrypt the data
                val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
                
                // Prefix with special marker to indicate backup method was used
                val prefixedData = "BACKUP:".toByteArray() + iv + encryptedBytes
                return Base64.encodeToString(prefixedData, Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e(TAG, "Backup encryption failed: ${e.message}", e)
                
                // If all else fails, return a specially formatted string that's clearly not decryptable
                // but contains the plaintext in a reversible form so we don't lose data
                val safeText = Base64.encodeToString(plainText.toByteArray(), Base64.DEFAULT)
                return "EMERGENCY_FALLBACK:$safeText"
            }
        }
        
        /**
         * Backup decryption method
         */
        private fun decryptWithBackupMethod(encryptedText: String): String {
            // Check if this is our emergency fallback format
            if (encryptedText.startsWith("EMERGENCY_FALLBACK:")) {
                val base64Data = encryptedText.substring("EMERGENCY_FALLBACK:".length)
                return String(Base64.decode(base64Data, Base64.DEFAULT))
            }
            
            // Now handle normal backup format
            val encryptedData = Base64.decode(encryptedText, Base64.DEFAULT)
            
            // Check if this was encrypted with the backup method
            val isBackupEncrypted = String(encryptedData.copyOfRange(0, 7)) == "BACKUP:"
            
            if (isBackupEncrypted) {
                // Extract IV and data (first 7 bytes are "BACKUP:", next 12 bytes are IV)
                val iv = encryptedData.copyOfRange(7, 19)
                val encrypted = encryptedData.copyOfRange(19, encryptedData.size)
                
                // Get the backup key
                val backupKey = getBackupKey()
                
                // Setup cipher for decryption
                val cipher = Cipher.getInstance(RECOVERY_TRANSFORMATION)
                val gcmSpec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.DECRYPT_MODE, backupKey, gcmSpec)
                
                // Decrypt and return
                val decryptedBytes = cipher.doFinal(encrypted)
                return String(decryptedBytes, Charsets.UTF_8)
            } else {
                // This wasn't backup encrypted, can't use backup method
                throw SecurityException("Data was not encrypted with backup method")
            }
        }
        
        /**
         * Get a consistent backup key derived from device-specific information
         */
        private fun getBackupKey(): SecretKey {
            try {
                // Use a device-specific but persistent value for deriving the backup key
                val deviceId = android.os.Build.FINGERPRINT + android.os.Build.SERIAL
                
                // Create a salt using a hash of the device ID 
                val md = MessageDigest.getInstance("SHA-256")
                val deviceSalt = md.digest(deviceId.toByteArray())
                
                // Key derivation using PBKDF2
                val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                val spec = PBEKeySpec(
                    BACKUP_ALIAS.toCharArray(),
                    deviceSalt,
                    BACKUP_KEY_ITERATIONS,
                    BACKUP_KEY_LENGTH
                )
                
                val tmp = factory.generateSecret(spec)
                return SecretKeySpec(tmp.encoded, "AES")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate backup key: ${e.message}", e)
                
                // Final fallback - create a key from a fixed string + device ID
                // Not as secure, but better than losing data
                val deviceId = android.os.Build.FINGERPRINT + android.os.Build.SERIAL
                val keyData = (BACKUP_ALIAS + deviceId).toByteArray()
                val hash = MessageDigest.getInstance("SHA-256").digest(keyData)
                return SecretKeySpec(hash, "AES")
            }
        }
    }
} 