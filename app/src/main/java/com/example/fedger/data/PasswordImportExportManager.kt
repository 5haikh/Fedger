package com.example.fedger.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.fedger.model.Credential
import com.example.fedger.model.PasswordEntry
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.OutputStreamWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import java.security.SecureRandom
import android.util.Base64

/**
 * Manager for importing and exporting password data
 */
class PasswordImportExportManager(
    private val repository: PasswordRepository,
    private val credentialDao: CredentialDao
) {
    
    private val TAG = "PassImportExportMgr"
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    
    /**
     * Data class to hold password data for export/import
     */
    data class PasswordDataExport(
        val passwordEntries: List<PasswordEntry>,
        val credentials: List<Credential>,
        val version: Int = 1,  // For future compatibility
        val exportKey: String // Transport key for additional security
    )
    
    /**
     * Export all password data to a file
     * @param useDecryptedValues Whether to export decrypted values (plaintext passwords)
     * @return Result with a Boolean indicating success or failure
     */
    suspend fun exportData(context: Context, uri: Uri, useDecryptedValues: Boolean = false): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Exporting password data to $uri" + if (useDecryptedValues) " with decrypted values" else "")
            
            // Generate a random export key for this export
            val exportKey = generateExportKey()
            
            // Get all password entries
            val passwordEntries = repository.allPasswordEntries.first()
            
            // Get all credentials for all entries
            val allCredentials = mutableListOf<Credential>()
            
            for (entry in passwordEntries) {
                // Get credentials for this entry
                val credentials = repository.getCredentialsForEntry(entry.id).first()
                
                val processedCredentials = credentials.map { credential ->
                    try {
                        if (useDecryptedValues) {
                            // For decrypted export, first ensure we have the decrypted value
                            // Call repository.decryptCredentialValue to properly decrypt from storage
                            val decryptedValue = repository.decryptCredentialValue(credential)
                            
                            Log.d(TAG, "Exporting plaintext value for credential ${credential.id}: $decryptedValue")
                            
                            // Return a new credential object with the decrypted value
                            credential.copy(
                                value = decryptedValue,
                                // Add a flag to indicate this is a plaintext value
                                type = (credential.type ?: "") + "_DECRYPTED"
                            )
                        } else {
                            // For normal export with encryption, apply transport encryption to the already encrypted value
                            // We don't decrypt first, just apply additional transport encryption
                            val transportEncrypted = simpleEncrypt(credential.value, exportKey)
                            credential.copy(value = transportEncrypted)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process credential ${credential.id}", e)
                        credential.copy(value = "ENCRYPTION_FAILED")
                    }
                }
                
                allCredentials.addAll(processedCredentials)
            }
            
            // Create the export data object
            val exportData = PasswordDataExport(
                passwordEntries = passwordEntries,
                credentials = allCredentials,
                exportKey = if (useDecryptedValues) "PLAINTEXT_NO_KEY_NEEDED" else exportKey,
                version = if (useDecryptedValues) 2 else 1  // Use version 2 for plaintext exports
            )
            
            // Convert to JSON
            val jsonData = gson.toJson(exportData)
            
            // Write to file
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonData)
                }
            }
            
            Log.d(TAG, "Export completed successfully. Exported ${passwordEntries.size} entries and ${allCredentials.size} credentials")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Import password data from a file
     * @param containsDecryptedValues Whether the imported file contains decrypted values
     * @return Result with a Triple containing success flag, number of entries, and number of credentials
     */
    suspend fun importData(context: Context, uri: Uri, containsDecryptedValues: Boolean = false): Result<Triple<Boolean, Int, Int>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Importing password data from $uri" + if (containsDecryptedValues) " with decrypted values" else "")
            
            // Read file content
            val jsonContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(inputStream.reader()).use { reader ->
                    reader.readText()
                }
            } ?: throw Exception("Could not read file")
            
            // Parse JSON data
            val typeToken = object : TypeToken<PasswordDataExport>() {}.type
            val importedData = gson.fromJson<PasswordDataExport>(jsonContent, typeToken)
            
            // Get the export key (not needed if importing decrypted values)
            val exportKey = importedData.exportKey
            
            // Check if file contains plaintext values based on version or flag
            val isPlaintextFile = containsDecryptedValues || 
                                  importedData.version == 2 || 
                                  exportKey == "PLAINTEXT_NO_KEY_NEEDED"
            
            // Insert all password entries first
            for (entry in importedData.passwordEntries) {
                // Create a fresh entry to avoid ID conflicts
                val newEntry = PasswordEntry(
                    title = entry.title,
                    description = entry.description,
                    category = entry.category,
                    createdAt = entry.createdAt,
                    updatedAt = entry.updatedAt
                )
                
                val newEntryId = repository.addPasswordEntry(newEntry)
                
                // Find all credentials for this entry
                val entryCredentials = importedData.credentials.filter { it.entryId == entry.id }
                
                // Create new credentials with the new entry ID
                val newCredentials = entryCredentials.map { credential ->
                    // Check if this specific credential is decrypted (might have _DECRYPTED suffix in type)
                    val isDecrypted = isPlaintextFile || (credential.type ?: "").endsWith("_DECRYPTED")
                    
                    val finalValue = if (isDecrypted) {
                        // If importing decrypted values, use them directly
                        // Remove _DECRYPTED suffix from type if present
                        val cleanType = (credential.type ?: "").replace("_DECRYPTED", "")
                        
                        Log.d(TAG, "Importing plaintext value for credential ${credential.id}: ${credential.value}")
                        
                        // Create with plaintext value (will be encrypted by repository.addCredentials)
                        Credential(
                            entryId = newEntryId.toInt(),
                            label = credential.label,
                            displayName = credential.displayName ?: "",
                            value = credential.value, // Use the plaintext value directly
                            isProtected = credential.isProtected,
                            type = cleanType,
                            position = credential.position,
                            notes = credential.notes ?: ""
                        )
                    } else {
                        // For encrypted values, decrypt using export key first
                        try {
                            val decryptedWithTransportKey = simpleDecrypt(credential.value, exportKey)
                            
                            // Create credential with the transport-decrypted value
                            // Note: This value is still encrypted with the app's encryption
                            Credential(
                                entryId = newEntryId.toInt(),
                                label = credential.label,
                                displayName = credential.displayName ?: "",
                                value = decryptedWithTransportKey,
                                isProtected = credential.isProtected,
                                type = credential.type ?: "",
                                position = credential.position,
                                notes = credential.notes ?: ""
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to decrypt imported credential with export key", e)
                            
                            // Create with original value as fallback
                            Credential(
                                entryId = newEntryId.toInt(),
                                label = credential.label,
                                displayName = credential.displayName ?: "",
                                value = credential.value,
                                isProtected = credential.isProtected,
                                type = credential.type ?: "",
                                position = credential.position,
                                notes = credential.notes ?: ""
                            )
                        }
                    }
                    
                    finalValue
                }
                
                // For plaintext values, we need to manually encrypt before adding to database
                if (isPlaintextFile) {
                    // This will properly encrypt the plaintext values
                    repository.addCredentials(newCredentials)
                } else {
                    // For normal import, credentials are already encrypted with app encryption
                    // but we need to bypass the repository's re-encryption
                    credentialDao.insertAll(newCredentials)
                }
            }
            
            Log.d(TAG, "Import completed successfully. Imported ${importedData.passwordEntries.size} entries and ${importedData.credentials.size} credentials")
            Result.success(Triple(true, importedData.passwordEntries.size, importedData.credentials.size))
        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Generate a random key for export encryption
     */
    private fun generateExportKey(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32) // 256 bits
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
    
    /**
     * Simple encryption for transport security (XOR with key)
     */
    private fun simpleEncrypt(text: String, key: String): String {
        val textBytes = text.toByteArray(Charsets.UTF_8)
        val keyBytes = Base64.decode(key, Base64.NO_WRAP)
        val result = ByteArray(textBytes.size)
        
        for (i in textBytes.indices) {
            result[i] = (textBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
        }
        
        return Base64.encodeToString(result, Base64.NO_WRAP)
    }
    
    /**
     * Simple decryption for transport security (XOR with key)
     */
    private fun simpleDecrypt(encryptedText: String, key: String): String {
        val encryptedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
        val keyBytes = Base64.decode(key, Base64.NO_WRAP)
        val result = ByteArray(encryptedBytes.size)
        
        for (i in encryptedBytes.indices) {
            result[i] = (encryptedBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
        }
        
        return String(result, Charsets.UTF_8)
    }
} 