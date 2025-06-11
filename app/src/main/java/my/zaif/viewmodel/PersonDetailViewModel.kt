package my.zaif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.zaif.data.entity.Person
import my.zaif.data.entity.Transaction
import my.zaif.data.repository.PersonRepository
import my.zaif.data.repository.TransactionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PersonDetailViewModel(
    private val personRepository: PersonRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    // Person data
    private val _person = MutableStateFlow<Person?>(null)
    val person: StateFlow<Person?> = _person
    
    // Current person ID
    private var currentPersonId: Long = -1
    
    // Transactions for the person
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions
    
    // Balance for the person
    private val _balance = MutableStateFlow<Double>(0.0)
    val balance: StateFlow<Double> = _balance
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // Jobs for cancellation
    private var transactionsJob: Job? = null
    private var balanceJob: Job? = null
    
    // Mutex for transaction synchronization
    private val transactionMutex = Mutex()
    
    // Load person data
    fun loadPerson(personId: Long) {
        currentPersonId = personId
        viewModelScope.launch {
            try {
                val loadedPerson = personRepository.getPersonById(personId)
                _person.value = loadedPerson
            } catch (e: Exception) {
                _error.value = "Failed to load person: ${e.message}"
            }
        }
    }
    
    // Load transactions for the person
    fun loadTransactions(personId: Long) {
        // Cancel previous jobs if they exist
        transactionsJob?.cancel()
        balanceJob?.cancel()
        
        // Collect transactions
        transactionsJob = viewModelScope.launch {
            transactionRepository.getAllTransactionsForPerson(personId)
                .catch { e ->
                    _error.value = "Failed to load transactions: ${e.message}"
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
                .collect {
                    _transactions.value = it
                    validateTransactionIntegrity(it)
                }
        }
        
        // Collect balance in a separate coroutine
        balanceJob = viewModelScope.launch {
            transactionRepository.getBalanceForPerson(personId)
                .catch { e ->
                    _error.value = "Failed to load balance: ${e.message}"
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = 0.0
                )
                .collect { balanceValue ->
                    _balance.value = balanceValue
                }
        }
    }
    
    // Refresh balance explicitly
    fun refreshBalance() {
        if (currentPersonId != -1L) {
            viewModelScope.launch {
                try {
                    // Use first() to get just one emission instead of collecting indefinitely
                    val balanceValue = transactionRepository.getBalanceForPerson(currentPersonId).stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(),
                        initialValue = 0.0
                    ).value
                    
                    _balance.value = balanceValue
                } catch (e: Exception) {
                    // Fallback in case of error
                    _error.value = "Failed to refresh balance: ${e.message}"
                    _balance.value = 0.0
                }
            }
        }
    }
    
    // Update person details
    fun updatePerson(person: Person) {
        viewModelScope.launch {
            try {
                personRepository.updatePerson(person)
                _person.value = person
            } catch (e: Exception) {
                _error.value = "Failed to update person: ${e.message}"
            }
        }
    }
    
    // Add a new transaction
    fun addTransaction(amount: Double, description: String, date: Long) {
        if (currentPersonId == -1L) return
        
        viewModelScope.launch {
            try {
                transactionMutex.withLock {
                    // Use the factory method for validation
                    val transaction = Transaction.create(
                        personId = currentPersonId,
                        amount = amount,
                        description = description,
                        date = date
                    )
                    transactionRepository.insertTransaction(transaction)
                    
                    // Verify the transaction was added correctly
                    val updatedTransactions = transactionRepository.getAllTransactionsForPerson(currentPersonId)
                        .stateIn(
                            scope = viewModelScope,
                            started = SharingStarted.WhileSubscribed(1000),
                            initialValue = emptyList()
                        ).value
                    
                    validateTransactionIntegrity(updatedTransactions)
                }
            } catch (e: Exception) {
                _error.value = "Failed to add transaction: ${e.message}"
            }
        }
    }
    
    // Delete a transaction
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionMutex.withLock {
                    transactionRepository.deleteTransaction(transaction)
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete transaction: ${e.message}"
            }
        }
    }
    
    // Validate transaction integrity
    private fun validateTransactionIntegrity(transactions: List<Transaction>) {
        // Calculate the sum manually to verify against the stored balance
        val calculatedSum = transactions.sumOf { it.amount }
        
        // If we have a significant difference, report an error
        if (calculatedSum != _balance.value && Math.abs(calculatedSum - _balance.value) > 0.001) {
            _error.value = "Data integrity issue detected: calculated balance doesn't match stored balance"
        }
    }
    
    // Clear error state
    fun clearError() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        transactionsJob?.cancel()
        balanceJob?.cancel()
    }
    
    // Factory class for creating the ViewModel with dependencies
    class Factory(
        private val personRepository: PersonRepository,
        private val transactionRepository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PersonDetailViewModel::class.java)) {
                return PersonDetailViewModel(personRepository, transactionRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 