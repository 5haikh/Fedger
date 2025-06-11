package my.zaif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import my.zaif.data.entity.Entity
import my.zaif.data.repository.EntityRepository

class CredentialsViewModel(
    private val entityRepository: EntityRepository
) : ViewModel() {

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // StateFlow of all entities
    val allEntities: StateFlow<List<Entity>> = entityRepository.allEntities
        .catch { e ->
            _error.value = "Failed to load entities: ${e.message}"
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Function to insert a new entity
    fun insertEntity(entityName: String) {
        if (entityName.isBlank()) {
            _error.value = "Entity name cannot be blank"
            return
        }
        
        viewModelScope.launch {
            try {
                val entity = Entity(entityName = entityName)
                entityRepository.insertEntity(entity)
            } catch (e: Exception) {
                _error.value = "Failed to insert entity: ${e.message}"
            }
        }
    }

    // Function to update an existing entity
    fun updateEntity(entity: Entity) {
        viewModelScope.launch {
            try {
                entityRepository.updateEntity(entity)
            } catch (e: Exception) {
                _error.value = "Failed to update entity: ${e.message}"
            }
        }
    }
    
    // Function to delete an entity
    fun deleteEntity(entity: Entity) {
        viewModelScope.launch {
            try {
                entityRepository.deleteEntity(entity)
            } catch (e: Exception) {
                _error.value = "Failed to delete entity: ${e.message}"
            }
        }
    }
    
    // Clear error state
    fun clearError() {
        _error.value = null
    }

    // Factory class for creating the ViewModel with dependencies
    class Factory(
        private val entityRepository: EntityRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CredentialsViewModel::class.java)) {
                return CredentialsViewModel(entityRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 