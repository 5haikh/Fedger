package my.zaif.data.repository

import kotlinx.coroutines.flow.Flow
import my.zaif.data.dao.StoredCredentialDao
import my.zaif.data.entity.StoredCredential
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StoredCredentialRepository(private val storedCredentialDao: StoredCredentialDao) {
    
    // Mutex for synchronizing credential operations
    private val credentialMutex = Mutex()
    
    fun getCredentialsForEntity(entityId: Long): Flow<List<StoredCredential>> {
        return storedCredentialDao.getCredentialsForEntity(entityId)
    }
    
    suspend fun insertCredential(credential: StoredCredential): Long {
        if (credential.credentialLabel.isBlank()) {
            throw IllegalArgumentException("Credential label cannot be blank")
        }
        
        return credentialMutex.withLock {
            storedCredentialDao.insertCredential(credential)
        }
    }
    
    suspend fun updateCredential(credential: StoredCredential) {
        if (credential.credentialLabel.isBlank()) {
            throw IllegalArgumentException("Credential label cannot be blank")
        }
        
        credentialMutex.withLock {
            // First verify the credential exists
            val existingCredential = storedCredentialDao.getCredentialById(credential.id)
                ?: throw IllegalArgumentException("Cannot update non-existent credential with ID ${credential.id}")
                
            storedCredentialDao.updateCredential(credential)
        }
    }
    
    suspend fun deleteCredential(credential: StoredCredential) {
        credentialMutex.withLock {
            storedCredentialDao.deleteCredential(credential)
        }
    }
    
    suspend fun getCredentialById(id: Long): StoredCredential? {
        if (id <= 0) {
            throw IllegalArgumentException("Invalid credential ID: $id")
        }
        return storedCredentialDao.getCredentialById(id)
    }
} 