package com.example.fedger.data

import com.example.fedger.model.Credential
import com.example.fedger.model.PasswordEntry
import com.example.fedger.model.PasswordEntryWithCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for password management operations
 */
class PasswordRepository(
    private val passwordEntryDao: PasswordEntryDao,
    private val credentialDao: CredentialDao,
    private val database: FedgerDatabase
) {
    // Password Entry operations
    val allPasswordEntries = passwordEntryDao.getAllPasswordEntries()
    val allPasswordEntriesWithCredentials = passwordEntryDao.getAllPasswordEntriesWithCredentials()
    
    suspend fun addPasswordEntry(entry: PasswordEntry): Long {
        return passwordEntryDao.insert(entry)
    }
    
    suspend fun updatePasswordEntry(entry: PasswordEntry) {
        passwordEntryDao.update(entry)
    }
    
    suspend fun deletePasswordEntry(entry: PasswordEntry) {
        passwordEntryDao.delete(entry)
    }
    
    fun getPasswordEntryById(id: Int): Flow<PasswordEntry?> {
        return passwordEntryDao.getPasswordEntryById(id)
    }
    
    fun getPasswordEntryWithCredentials(id: Int): Flow<PasswordEntryWithCredentials?> {
        return passwordEntryDao.getPasswordEntryWithCredentials(id)
    }
    
    fun searchPasswordEntries(query: String): Flow<List<PasswordEntry>> {
        return passwordEntryDao.searchPasswordEntries(query)
    }
    
    fun getAllCategories(): Flow<List<String>> {
        return passwordEntryDao.getAllCategories()
    }
    
    fun getPasswordEntriesByCategory(category: String): Flow<List<PasswordEntry>> {
        return passwordEntryDao.getPasswordEntriesByCategory(category)
    }
    
    // Credential operations with encryption/decryption
    
    fun getCredentialsForEntry(entryId: Int): Flow<List<Credential>> {
        return credentialDao.getCredentialsByEntryId(entryId).map { credentials ->
            credentials.map { credential ->
                // We don't decrypt here, as we want to keep sensitive data encrypted in memory
                // Decryption happens at display time
                credential
            }
        }
    }
    
    suspend fun addCredential(credential: Credential): Long {
        // Encrypt the value before storing
        val encryptedCredential = credential.copy(
            value = EncryptionUtil.encrypt(credential.value)
        )
        return credentialDao.insert(encryptedCredential)
    }
    
    suspend fun addCredentials(credentials: List<Credential>): List<Long> {
        // Encrypt all values before storing
        val encryptedCredentials = credentials.map { credential ->
            credential.copy(
                value = EncryptionUtil.encrypt(credential.value)
            )
        }
        return credentialDao.insertAll(encryptedCredentials)
    }
    
    suspend fun updateCredential(credential: Credential) {
        // Encrypt the value before storing
        val encryptedCredential = credential.copy(
            value = EncryptionUtil.encrypt(credential.value)
        )
        credentialDao.update(encryptedCredential)
    }
    
    suspend fun deleteCredential(credential: Credential) {
        credentialDao.delete(credential)
    }
    
    suspend fun deleteAllCredentialsForEntry(entryId: Int) {
        credentialDao.deleteAllForEntry(entryId)
    }
    
    fun getCredentialById(id: Int): Flow<Credential?> {
        return credentialDao.getCredentialById(id)
    }
    
    // Helper method to decrypt a credential value    
    fun decryptCredentialValue(credential: Credential): String {        
        return try {            
            EncryptionUtil.decrypt(credential.value)        
        } catch (e: Exception) {            
            // If decryption fails, return the original value            
            credential.value        
        }    
    }
    
    // Helper to save both entry and credentials in a transaction
    suspend fun savePasswordEntryWithCredentials(entry: PasswordEntry, credentials: List<Credential>) {
        // Create a new entry and get its ID
        val entryId = passwordEntryDao.insert(entry).toInt()
        
        // Encrypt and save credentials with the new entry ID
        val encryptedCredentials = credentials.mapIndexed { index, credential ->
            // Try to decrypt first in case it's already encrypted
            val decryptedValue = try {
                EncryptionUtil.decrypt(credential.value)
            } catch (e: Exception) {
                credential.value
            }
            
            credential.copy(
                entryId = entryId,
                position = index,
                value = EncryptionUtil.encrypt(decryptedValue)
            )
        }
        
        credentialDao.insertAll(encryptedCredentials)
    }
    
    // Helper to update an entry with credentials    
    suspend fun updatePasswordEntryWithCredentials(entry: PasswordEntry, credentials: List<Credential>) {
        // Update the entry
        passwordEntryDao.update(entry)
        
        // Delete existing credentials
        credentialDao.deleteAllForEntry(entry.id)
        
        // Encrypt and save new credentials
        val encryptedCredentials = credentials.mapIndexed { index, credential ->
            // Try to decrypt first in case it's already encrypted
            val decryptedValue = try {
                EncryptionUtil.decrypt(credential.value)
            } catch (e: Exception) {
                credential.value
            }
            
            credential.copy(
                entryId = entry.id,
                position = index,
                value = EncryptionUtil.encrypt(decryptedValue)
            )
        }
        
        credentialDao.insertAll(encryptedCredentials)
    }
} 