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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import my.zaif.data.entity.Person
import my.zaif.data.repository.PersonRepository
import my.zaif.data.repository.TransactionRepository

data class PersonWithBalance(
    val person: Person,
    val balance: Double
)

class LedgerViewModel(
    private val personRepository: PersonRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // StateFlow of all people with their balances
    val peopleWithBalances: StateFlow<List<PersonWithBalance>> = combine(
        personRepository.allPeople,
        transactionRepository.getAllBalances()
    ) { people, balances ->
        // Create a map of personId -> balance for quick lookup
        val balanceMap = balances.associateBy { it.personId }
        
        // Map each person to a PersonWithBalance
        people.map { person ->
            // Get the balance for this person, or 0.0 if no transactions
            val balance = balanceMap[person.id]?.balance ?: 0.0
            PersonWithBalance(person, balance)
        }
    }
    .catch { e ->
        _error.value = "Failed to load people or balances: ${e.message}"
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // For backward compatibility
    val allPeople: StateFlow<List<Person>> = peopleWithBalances
        .map { it.map { pwb -> pwb.person } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Function to insert a new person
    fun insertPerson(name: String, notes: String?) {
        if (name.isBlank()) {
            _error.value = "Person name cannot be blank"
            return
        }
        
        viewModelScope.launch {
            try {
                val person = Person(name = name, notes = notes)
                personRepository.insertPerson(person)
            } catch (e: Exception) {
                _error.value = "Failed to insert person: ${e.message}"
            }
        }
    }

    // Function to update an existing person
    fun updatePerson(person: Person) {
        viewModelScope.launch {
            try {
                personRepository.updatePerson(person)
            } catch (e: Exception) {
                _error.value = "Failed to update person: ${e.message}"
            }
        }
    }
    
    // Function to delete a person
    fun deletePerson(person: Person) {
        viewModelScope.launch {
            try {
                personRepository.deletePerson(person)
            } catch (e: Exception) {
                _error.value = "Failed to delete person: ${e.message}"
            }
        }
    }
    
    // Clear error state
    fun clearError() {
        _error.value = null
    }

    // Factory class for creating the ViewModel with dependencies
    class Factory(
        private val personRepository: PersonRepository,
        private val transactionRepository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LedgerViewModel::class.java)) {
                return LedgerViewModel(personRepository, transactionRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 