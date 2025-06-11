package my.zaif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import my.zaif.data.dao.StoredCredentialDao
import my.zaif.data.entity.CredentialType
import my.zaif.data.entity.Entity
import my.zaif.data.entity.StoredCredential
import my.zaif.data.repository.EntityRepository
import my.zaif.data.repository.StoredCredentialRepository

class EntityDetailViewModel(
    private val entityId: Long,
    private val entityRepository: EntityRepository,
    private val storedCredentialRepository: StoredCredentialRepository
) : ViewModel() {

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Entity state
    private val _entity = MutableStateFlow<Entity?>(null)
    val entity: StateFlow<Entity?> = _entity

    // Credentials list
    val credentials = storedCredentialRepository.getCredentialsForEntity(entityId)
        .catch { e ->
            _error.value = "Failed to load credentials: ${e.message}"
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadEntity()
    }

    private fun loadEntity() {
        viewModelScope.launch {
            try {
                val loadedEntity = entityRepository.getEntityById(entityId)
                _entity.value = loadedEntity
                if (loadedEntity == null) {
                    _error.value = "Entity not found"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load entity: ${e.message}"
            }
        }
    }

    fun updateEntityName(newName: String) {
        viewModelScope.launch {
            try {
                val currentEntity = _entity.value ?: return@launch
                val updatedEntity = currentEntity.copy(entityName = newName)
                entityRepository.updateEntity(updatedEntity)
                _entity.value = updatedEntity
            } catch (e: Exception) {
                _error.value = "Failed to update entity name: ${e.message}"
            }
        }
    }

    fun addCredential(
        credentialLabel: String,
        credentialType: CredentialType,
        username: String? = null,
        passwordValue: String? = null,
        customFieldKey: String? = null
    ) {
        viewModelScope.launch {
            try {
                val credential = StoredCredential(
                    entityId = entityId,
                    credentialLabel = credentialLabel,
                    credentialType = credentialType,
                    username = username,
                    passwordValue = passwordValue,
                    customFieldKey = customFieldKey
                )
                storedCredentialRepository.insertCredential(credential)
            } catch (e: Exception) {
                _error.value = "Failed to add credential: ${e.message}"
            }
        }
    }

    fun updateCredential(credential: StoredCredential) {
        viewModelScope.launch {
            try {
                storedCredentialRepository.updateCredential(credential)
            } catch (e: Exception) {
                _error.value = "Failed to update credential: ${e.message}"
            }
        }
    }

    fun deleteCredential(credential: StoredCredential) {
        viewModelScope.launch {
            try {
                storedCredentialRepository.deleteCredential(credential)
            } catch (e: Exception) {
                _error.value = "Failed to delete credential: ${e.message}"
            }
        }
    }
    
    fun deleteEntity(entity: Entity) {
        viewModelScope.launch {
            try {
                entityRepository.deleteEntity(entity)
            } catch (e: Exception) {
                _error.value = "Failed to delete entity: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    class Factory(
        private val entityId: Long,
        private val entityRepository: EntityRepository,
        private val storedCredentialRepository: StoredCredentialRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EntityDetailViewModel::class.java)) {
                return EntityDetailViewModel(entityId, entityRepository, storedCredentialRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 