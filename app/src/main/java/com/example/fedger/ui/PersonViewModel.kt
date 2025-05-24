package com.example.fedger.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fedger.FedgerApplication
import com.example.fedger.data.FedgerRepository
import com.example.fedger.model.Person
import com.example.fedger.model.Transaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlin.math.abs
import kotlinx.coroutines.delay

private const val TAG = "PersonViewModel"

// Define sort options as an enum
enum class PersonSortOption {
    NAME_ASC,
    NAME_DESC,
    BALANCE_HIGH_TO_LOW,
    BALANCE_LOW_TO_HIGH,
    LAST_ADDED
}

class PersonViewModel(private val repository: FedgerRepository) : ViewModel() {
    // Persons list - shared with proper lifecycle scope
    val persons = repository.allPersons.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000), // Keep data for 5 seconds after last subscriber
        emptyList()
    )
    
    // Selected person
    private val _selectedPerson = MutableStateFlow<Person?>(null)
    val selectedPerson: StateFlow<Person?> = _selectedPerson.asStateFlow()

    // Add sort option state
    private val _currentSortOption = MutableStateFlow(PersonSortOption.NAME_ASC)
    val currentSortOption: StateFlow<PersonSortOption> = _currentSortOption.asStateFlow()

    // Pagination related fields
    private val _pagedPersons = MutableStateFlow<List<Person>>(emptyList())
    val pagedPersons: StateFlow<List<Person>> = _pagedPersons.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _totalPersonCount = MutableStateFlow(0)
    val totalPersonCount: StateFlow<Int> = _totalPersonCount.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()
    
    // Transaction pagination related fields
    private val _pagedTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val pagedTransactions: StateFlow<List<Transaction>> = _pagedTransactions.asStateFlow()
    
    private val _isLoadingTransactions = MutableStateFlow(false)
    val isLoadingTransactions: StateFlow<Boolean> = _isLoadingTransactions.asStateFlow()
    
    private val _totalTransactionCount = MutableStateFlow(0)
    val totalTransactionCount: StateFlow<Int> = _totalTransactionCount.asStateFlow()
    
    private val _currentTransactionPage = MutableStateFlow(0)
    val currentTransactionPage: StateFlow<Int> = _currentTransactionPage.asStateFlow()
    
    private val _hasMoreTransactions = MutableStateFlow(true)
    val hasMoreTransactions: StateFlow<Boolean> = _hasMoreTransactions.asStateFlow()
    
    // Active person ID for loading transactions
    private val _currentPersonId = MutableStateFlow<Int?>(null)
    
    private val PAGE_SIZE = 10
    private val TRANSACTION_PAGE_SIZE = 15
    
    // Transactions with proper lifecycle management
    val allTransactions = repository.allTransactions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    
    // Map of person ID to calculated balance
    private val _balances = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val balances: StateFlow<Map<Int, Double>> = _balances.asStateFlow()
    
    // Total money summary
    private val _totalBalance = MutableStateFlow(TotalBalanceSummary(0.0, 0.0))
    val totalBalance: StateFlow<TotalBalanceSummary> = _totalBalance.asStateFlow()
    
    // Error state for UI to observe with timeout handling
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Add a new state for search results vs regular list
    private val _showingSearchResults = MutableStateFlow(false)
    val showingSearchResults: StateFlow<Boolean> = _showingSearchResults.asStateFlow()
    
    // Add a backup of the regular list
    private var _regularPersonsList = listOf<Person>()
    
    // Add import/export related state and methods
    private val _importExportState = MutableStateFlow<ImportExportState>(ImportExportState.Idle)
    val importExportState: StateFlow<ImportExportState> = _importExportState.asStateFlow()
    
    // Job for tracking error timeout
    private var errorTimeoutJob: Job? = null
    
    // Error timeout duration in milliseconds (default: 5 seconds)
    private val ERROR_TIMEOUT_DURATION = 5000L
    
    // Enhanced exception handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        val errorMessage = "Error: ${throwable.localizedMessage ?: "Unknown error occurred"}"
        setError(errorMessage)
        _isLoading.value = false
        _isLoadingTransactions.value = false
        Log.e(TAG, "Exception caught in ViewModel", throwable)
    }
    
    // Helper method to run coroutines with proper error handling
    private fun launchWithErrorHandling(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            try {
                block()
            } catch (e: Exception) {
                setError("Error: ${e.localizedMessage ?: "Unknown error occurred"}")
                Log.e(TAG, "Error in ViewModel operation", e)
            }
        }
    }
    
    init {
        // Calculate balances whenever transactions change
        allTransactions.onEach { transactions ->
            val newBalances = transactions.groupBy { it.personId }
                .mapValues { (_, personTransactions) ->
                    personTransactions.sumOf { if (it.isCredit) it.amount else -it.amount }
                }
            _balances.value = newBalances
            
            // Calculate total balance summary
            updateTotalBalanceSummary()
            
            // Re-sort the list if we're sorting by balance
            if (_currentSortOption.value == PersonSortOption.BALANCE_HIGH_TO_LOW || 
                _currentSortOption.value == PersonSortOption.BALANCE_LOW_TO_HIGH) {
                sortPersonList(_currentSortOption.value)
            }
        }.launchIn(viewModelScope)
        
        // Also update total summary when balances change
        balances.onEach { _ ->
            updateTotalBalanceSummary()
        }.launchIn(viewModelScope)
        
        // Load initial data and monitor total count
        loadInitialData()
        
        // Monitor total person count
        viewModelScope.launch {
            searchQuery.collectLatest { query ->
                if (query.isEmpty()) {
                    repository.getPersonCount().collectLatest { count ->
                        _totalPersonCount.value = count
                        _hasMoreData.value = _pagedPersons.value.size < count
                    }
                } else {
                    repository.getSearchPersonCount(query).collectLatest { count ->
                        _totalPersonCount.value = count
                        _hasMoreData.value = _pagedPersons.value.size < count
                    }
                }
            }
        }
        
        // Monitor current person ID for transaction pagination
        viewModelScope.launch {
            _currentPersonId.collectLatest { personId ->
                if (personId != null) {
                    repository.getTransactionCountForPerson(personId).collectLatest { count ->
                        _totalTransactionCount.value = count
                        _hasMoreTransactions.value = _pagedTransactions.value.size < count
                    }
                }
            }
        }
    }
    
    private fun loadInitialData() {
        _isLoading.value = true
        _currentPage.value = 0
        _hasMoreData.value = true
        _showingSearchResults.value = false
        
        viewModelScope.launch {
            try {
                // Get the total count for pagination
                val allPersons = repository.allPersons.first()
                _totalPersonCount.value = allPersons.size
                
                // Always apply current sort option to the initial data
                val initialData = allPersons.take(PAGE_SIZE)
                val sortedInitialData = sortPersons(initialData, _currentSortOption.value)
                
                // Update the UI list
                _pagedPersons.value = sortedInitialData
                _regularPersonsList = sortedInitialData
                
                // Determine if there's more data to load
                _hasMoreData.value = allPersons.size > PAGE_SIZE
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error loading initial data", e)
                _error.value = "Error loading data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadNextPage() {
        // Skip if showing search results
        if (_showingSearchResults.value) {
            return
        }
        
        // Skip if already loading or no more data
        if (!_hasMoreData.value || _isLoading.value) {
            Log.d(TAG, "Skipping loadNextPage: hasMoreData=${_hasMoreData.value}, isLoading=${_isLoading.value}")
            return
        }
        
        viewModelScope.launch(exceptionHandler) {
            val nextPage = _currentPage.value + 1
            val offset = nextPage * PAGE_SIZE
            Log.d(TAG, "Loading next page: $nextPage (offset: $offset)")
            _isLoading.value = true
            
            try {
                val newPersons = repository.getPagedPersons(PAGE_SIZE, offset).first()
                Log.d(TAG, "Loaded next page: ${newPersons.size} items")
                if (newPersons.isNotEmpty()) {
                    // Sort new persons according to current sort option
                    val sortedNewPersons = sortPersons(newPersons, _currentSortOption.value)
                    
                    // When using sorted lists, we need to re-sort the combined list
                    val combinedList = _pagedPersons.value + sortedNewPersons
                    _pagedPersons.value = sortPersons(combinedList, _currentSortOption.value)
                    
                    // Also update the regular list backup
                    _regularPersonsList = _pagedPersons.value
                    _currentPage.value = nextPage
                    _hasMoreData.value = newPersons.size >= PAGE_SIZE
                } else {
                    _hasMoreData.value = false
                    Log.d(TAG, "No more data available")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading next page", e)
                _error.value = "Error loading more data: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "Current data size: ${_pagedPersons.value.size}, hasMoreData: ${_hasMoreData.value}")
            }
        }
    }
    
    // Transaction pagination methods
    fun loadInitialTransactions(personId: Int) {
        // Set current person ID without waiting for data loading
        _currentPersonId.value = personId
        
        // If we already have data for this person, don't reset it
        if (_currentPersonId.value == personId && _pagedTransactions.value.isNotEmpty()) {
            return
        }
        
        viewModelScope.launch(exceptionHandler) {
            Log.d(TAG, "Loading initial transactions for person $personId")
            _isLoadingTransactions.value = true
            _pagedTransactions.value = emptyList()
            _currentTransactionPage.value = 0
            
            try {
                val transactions = repository.getPagedTransactionsForPerson(personId, TRANSACTION_PAGE_SIZE, 0).first()
                Log.d(TAG, "Loaded initial transactions: ${transactions.size} items")
                _pagedTransactions.value = transactions
                _hasMoreTransactions.value = transactions.size >= TRANSACTION_PAGE_SIZE
            } catch (e: Exception) {
                Log.e(TAG, "Error loading initial transactions", e)
                _error.value = "Error loading transactions: ${e.message}"
            } finally {
                _isLoadingTransactions.value = false
                Log.d(TAG, "Initial transactions loading complete, hasMoreTransactions: ${_hasMoreTransactions.value}")
            }
        }
    }
    
    fun loadNextTransactionPage() {
        val personId = _currentPersonId.value ?: return
        
        if (!_hasMoreTransactions.value || _isLoadingTransactions.value) {
            Log.d(TAG, "Skipping loadNextTransactionPage: hasMoreTransactions=${_hasMoreTransactions.value}, isLoadingTransactions=${_isLoadingTransactions.value}")
            return
        }
        
        viewModelScope.launch(exceptionHandler) {
            val nextPage = _currentTransactionPage.value + 1
            val offset = nextPage * TRANSACTION_PAGE_SIZE
            Log.d(TAG, "Loading next transaction page: $nextPage (offset: $offset)")
            _isLoadingTransactions.value = true
            
            try {
                val newTransactions = repository.getPagedTransactionsForPerson(personId, TRANSACTION_PAGE_SIZE, offset).first()
                Log.d(TAG, "Loaded next transaction page: ${newTransactions.size} items")
                
                if (newTransactions.isNotEmpty()) {
                    _pagedTransactions.value = _pagedTransactions.value + newTransactions
                    _currentTransactionPage.value = nextPage
                    _hasMoreTransactions.value = newTransactions.size >= TRANSACTION_PAGE_SIZE
                } else {
                    _hasMoreTransactions.value = false
                    Log.d(TAG, "No more transaction data available")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading next transaction page", e)
                _error.value = "Error loading more transactions: ${e.message}"
            } finally {
                _isLoadingTransactions.value = false
                Log.d(TAG, "Current transaction data size: ${_pagedTransactions.value.size}, hasMoreTransactions: ${_hasMoreTransactions.value}")
            }
        }
    }
    
    fun refreshTransactions() {
        val personId = _currentPersonId.value
        
        viewModelScope.launch(exceptionHandler) {
            if (personId == null) {
                // If no specific person is selected, just refresh all transactions
                Log.d(TAG, "No person selected for transaction refresh, skipping")
                return@launch
            }
            
            Log.d(TAG, "Forcing transaction refresh for person $personId")
            _isLoadingTransactions.value = true
            
            try {
                // Always reset page counters
                _currentTransactionPage.value = 0
                
                // Keep existing transactions until we get new ones to avoid UI flicker
                val existingTransactions = _pagedTransactions.value
                
                // Fetch fresh data directly from repository
                val transactions = repository.getPagedTransactionsForPerson(personId, TRANSACTION_PAGE_SIZE, 0).first()
                Log.d(TAG, "Refreshed transactions: ${transactions.size} items")
                
                // Only update the UI if we actually have new data
                if (transactions.isNotEmpty() || existingTransactions.isEmpty()) {
                    _pagedTransactions.value = transactions
                } else if (existingTransactions.isNotEmpty() && transactions.isEmpty()) {
                    // Special case: If we had transactions before but now the list is empty,
                    // verify with the transaction count before clearing the list
                    val count = repository.getTransactionCountForPerson(personId).first()
                    if (count == 0) {
                        _pagedTransactions.value = emptyList()
                    }
                }
                
                _hasMoreTransactions.value = transactions.size >= TRANSACTION_PAGE_SIZE
                
                // Also refresh transaction count
                repository.getTransactionCountForPerson(personId).first().let { count ->
                    _totalTransactionCount.value = count
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing transactions", e)
                _error.value = "Error refreshing transactions: ${e.message}"
            } finally {
                _isLoadingTransactions.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        val trimmedQuery = query.trim().lowercase()
        
        if (_searchQuery.value == trimmedQuery) return
        
        Log.d(TAG, "Setting search query: '$trimmedQuery'")
        
        // Special handling for clearing search
        if (trimmedQuery.isEmpty() && _showingSearchResults.value) {
            // If we were showing search results, restore the regular list
            _searchQuery.value = ""
            _showingSearchResults.value = false
            _pagedPersons.value = _regularPersonsList
            _isLoading.value = false
            return
        }
        
        // Update the search query
        _searchQuery.value = trimmedQuery
        
        // Check if we are initiating a new search
        if (trimmedQuery.isNotEmpty() && !_showingSearchResults.value) {
            // Backup the current regular list before showing search results
            _regularPersonsList = _pagedPersons.value
            _showingSearchResults.value = true
        }
        
        // Skip search if query is empty (already handled the clearing case above)
        if (trimmedQuery.isEmpty()) return
        
        // Load all persons and filter in memory for the search
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get all persons and filter in memory
                val allPersons = repository.allPersons.first()
                var filteredPersons = allPersons.filter { person ->
                    person.name.lowercase().contains(trimmedQuery) ||
                    person.phoneNumber.lowercase().contains(trimmedQuery) ||
                    person.address.lowercase().contains(trimmedQuery)
                }.sortedWith(compareBy(
                    // Sort by how closely they match the query
                    { !(it.name.lowercase() == trimmedQuery) },
                    { !(it.name.lowercase().startsWith(trimmedQuery)) },
                    { !(it.name.lowercase().contains(" $trimmedQuery")) },
                    { it.name.lowercase() }
                ))
                
                // Apply additional sort if needed
                filteredPersons = sortPersons(filteredPersons, _currentSortOption.value)
                
                Log.d(TAG, "Filtered ${filteredPersons.size} persons from ${allPersons.size} total for query '$trimmedQuery'")
                _pagedPersons.value = filteredPersons
                _hasMoreData.value = false // No pagination for search results
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error in search", e)
                _error.value = "Error searching: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshData() {
        loadInitialData()
    }
    
    fun addPerson(person: Person) {
        viewModelScope.launch(exceptionHandler) {
            try {
                repository.insertPerson(person)
                // Refresh data after adding a person
                refreshData()
                // Clear any previous errors on success
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to add person: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun deletePerson(person: Person) {
        launchWithErrorHandling {
            repository.deletePerson(person)
            _error.value = null // Clear error state if operation succeeds
        }
    }

    fun selectPerson(person: Person) {
        _selectedPerson.value = person
        // Load initial transactions for this person
        loadInitialTransactions(person.id)
    }

    fun getPersonById(id: Int): Flow<Person?> {
        return repository.getPersonById(id)
            .catch { e ->
                _error.value = "Failed to get person: ${e.localizedMessage ?: "Unknown error"}"
                emit(null)
            }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch(exceptionHandler) {
            try {
                // Save the transaction to the database - repository now handles balance updates
                repository.insertTransaction(transaction)
                Log.d(TAG, "Transaction added for person ${transaction.personId}")
                
                // The repository now handles balance updates in a transaction
                // We just need to update our local state
                
                // Set the currentPersonId if not set already
                if (_currentPersonId.value == null) {
                    _currentPersonId.value = transaction.personId
                }
                
                // Get the updated person to ensure consistent state in the UI
                repository.getPersonById(transaction.personId).first()?.let { updatedPerson ->
                    // Update the balances map with the updated balance from the database
                    _balances.update { currentBalances ->
                        currentBalances.toMutableMap().apply {
                            this[transaction.personId] = updatedPerson.balance
                        }
                    }
                    
                    // Update the person in the list
                    updatePersonInList(updatedPerson)
                }
                
                // Force a refresh of transactions list
                refreshTransactions()
                
                // Also update the total balance summary
                updateTotalBalanceSummary()
                
                // Clear any previous errors on success
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add transaction", e)
                _error.value = "Failed to add transaction: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }
    
    // Helper function to update a person in the pagination list
    private fun updatePersonInList(updatedPerson: Person) {
        val currentList = _pagedPersons.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedPerson.id }
        if (index >= 0) {
            currentList[index] = updatedPerson
            _pagedPersons.value = currentList
        }
    }
    
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch(exceptionHandler) {
            try {
                // First remove from the UI list to avoid void spaces
                val currentList = _pagedTransactions.value.toMutableList()
                val transactionIndex = currentList.indexOfFirst { it.id == transaction.id }
                
                if (transactionIndex >= 0) {
                    currentList.removeAt(transactionIndex)
                    _pagedTransactions.value = currentList
                }
                
                // Delete transaction - repository now handles balance updates
                repository.deleteTransaction(transaction)
                Log.d(TAG, "Transaction deleted for person ${transaction.personId}")
                
                // Update transaction count immediately for better UI responsiveness
                _totalTransactionCount.value = _totalTransactionCount.value - 1
                
                // Get the updated person to ensure consistent state in the UI
                repository.getPersonById(transaction.personId).first()?.let { updatedPerson ->
                    // Update the balances map with the updated balance from the database
                    _balances.update { currentBalances ->
                        currentBalances.toMutableMap().apply {
                            this[transaction.personId] = updatedPerson.balance
                        }
                    }
                    
                    // Update the person in the list
                    updatePersonInList(updatedPerson)
                }
                
                // Force a refresh of transactions (after a short delay to let the UI update)
                viewModelScope.launch {
                    delay(300) // Short delay for better UI experience
                    refreshTransactions()
                }
                
                // Also update the total balance summary
                updateTotalBalanceSummary()
                
                // Clear any previous errors on success
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete transaction", e)
                _error.value = "Failed to delete transaction: ${e.localizedMessage ?: "Unknown error"}"
                
                // On error, force a complete refresh to ensure UI consistency
                refreshTransactions()
            }
        }
    }

    // Add a function to verify all balances
    fun verifyAllBalances() {
        viewModelScope.launch(exceptionHandler) {
            try {
                _isLoading.value = true
                
                val fixedCount = repository.verifyAllBalances()
                
                if (fixedCount > 0) {
                    _error.value = "Fixed $fixedCount inconsistent balances"
                    
                    // Refresh the data to show the corrected balances
                    refreshData()
                    
                    // Also update the total balance summary
                    updateTotalBalanceSummary()
                } else {
                    _error.value = "All balances are correct"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to verify balances", e)
                _error.value = "Failed to verify balances: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Add a function to get a person with live balance (useful for critical screens)
    fun getPersonWithLiveBalance(personId: Int): Flow<Person?> {
        return repository.getPersonWithLiveBalance(personId)
    }

    // Add a function to update an existing transaction
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch(exceptionHandler) {
            try {
                repository.updateTransaction(transaction)
                Log.d(TAG, "Transaction updated for person ${transaction.personId}")
                
                // Update person's balance through the repository for consistency
                val personId = transaction.personId
                repository.recalculatePersonBalance(personId)
                
                // After recalculation, fetch the updated balance from the database
                repository.getPersonById(personId).first()?.let { updatedPerson ->
                    // Update the balances map with the correctly calculated balance
                    _balances.update { currentBalances ->
                        currentBalances.toMutableMap().apply {
                            this[personId] = updatedPerson.balance
                        }
                    }
                    
                    // Update the person in the list to reflect the new balance
                    updatePersonInList(updatedPerson)
                }
                
                // Force a refresh of transactions
                refreshTransactions()
                
                // Also update the total balance summary
                updateTotalBalanceSummary()
                
                // Clear any previous errors on success
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update transaction", e)
                _error.value = "Failed to update transaction: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun getTransactionsForPerson(personId: Int): Flow<List<Transaction>> {
        return repository.getTransactionsForPerson(personId)
            .catch { e ->
                _error.value = "Failed to get transactions: ${e.localizedMessage ?: "Unknown error"}"
                emit(emptyList())
            }
    }

    fun getBalanceForPerson(personId: Int): Flow<Double> {
        return repository.getTransactionsForPerson(personId)
            .map { transactions ->
                transactions.sumOf { if (it.isCredit) it.amount else -it.amount }
            }
            .catch { e ->
                _error.value = "Failed to calculate balance: ${e.localizedMessage ?: "Unknown error"}"
                emit(0.0)
            }
    }
    
    // Error handling with timeout
    private fun setError(errorMessage: String?) {
        // Cancel any existing timeout job
        errorTimeoutJob?.cancel()
        
        // Set the error message
        _error.value = errorMessage
        
        // If error is not null, start a timeout to clear it automatically
        if (errorMessage != null) {
            errorTimeoutJob = viewModelScope.launch {
                delay(ERROR_TIMEOUT_DURATION)
                _error.value = null
            }
        }
    }
    
    // Clear error immediately (can be called manually if needed)
    fun clearError() {
        errorTimeoutJob?.cancel()
        _error.value = null
    }
    
    // Get balance for a person synchronously (for UI display)
    fun getCurrentBalanceForPerson(personId: Int): Double {
        return _balances.value[personId] ?: 0.0
    }

    // Check if a person's balance is settled (zero)
    fun isPersonBalanceSettled(personId: Int): Boolean {
        val balance = _balances.value[personId] ?: 0.0
        return balance == 0.0
    }

    // Update total balance summary
    private fun updateTotalBalanceSummary() {
        val currentBalances = _balances.value
        
        var totalIAmOwed = 0.0  // Money others owe me (positive balances)
        var totalIOwed = 0.0    // Money I owe others (negative balances)
        
        currentBalances.values.forEach { balance ->
            if (balance > 0) {
                totalIAmOwed += balance
            } else if (balance < 0) {
                totalIOwed += abs(balance)
            }
        }
        
        _totalBalance.value = TotalBalanceSummary(
            totalOwedToMe = totalIAmOwed,
            totalIOwed = totalIOwed
        )
    }

    // Data class to hold balance summary information
    data class TotalBalanceSummary(
        val totalOwedToMe: Double, // Money others owe me
        val totalIOwed: Double     // Money I owe others
    )

    class Factory(private val repository: FedgerRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PersonViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PersonViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    fun clearSelectedPerson() {
        _selectedPerson.value = null
        _currentPersonId.value = null
        _pagedTransactions.value = emptyList()
    }

    /**
     * Export all data to a file
     */
    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch(exceptionHandler) {
            _importExportState.value = ImportExportState.Loading("Exporting data...")
            
            try {
                val application = context.applicationContext as FedgerApplication
                val result = application.dataManager.exportData(context, uri)
                
                result.fold(
                    onSuccess = { success: Boolean ->
                        _importExportState.value = ImportExportState.Success("Export completed successfully!")
                        // Reset after a delay
                        delay(3000)
                        _importExportState.value = ImportExportState.Idle
                    },
                    onFailure = { e: Throwable ->
                        _importExportState.value = ImportExportState.Error("Export failed: ${e.localizedMessage ?: "Unknown error"}")
                        // Reset after a delay
                        delay(5000)
                        _importExportState.value = ImportExportState.Idle
                    }
                )
            } catch (e: Exception) {
                _importExportState.value = ImportExportState.Error("Export failed: ${e.localizedMessage ?: "Unknown error"}")
                // Reset after a delay
                delay(5000)
                _importExportState.value = ImportExportState.Idle
            }
        }
    }
    
    /**
     * Import data from a file
     */
    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch(exceptionHandler) {
            _importExportState.value = ImportExportState.Loading("Importing data...")
            
            try {
                val application = context.applicationContext as FedgerApplication
                val result = application.dataManager.importData(context, uri)
                
                result.fold(
                    onSuccess = { triple: Triple<Boolean, Int, Int> ->
                        val (success, personsCount, transactionsCount) = triple
                        if (success) {
                            _importExportState.value = ImportExportState.Success(
                                "Import completed successfully!\nImported $personsCount contacts and $transactionsCount transactions."
                            )
                            // Refresh data
                            refreshData()
                            // Reset after a delay
                            delay(3000)
                            _importExportState.value = ImportExportState.Idle
                        } else {
                            _importExportState.value = ImportExportState.Error("Import failed.")
                            // Reset after a delay
                            delay(5000)
                            _importExportState.value = ImportExportState.Idle
                        }
                    },
                    onFailure = { e: Throwable ->
                        _importExportState.value = ImportExportState.Error("Import failed: ${e.localizedMessage ?: "Unknown error"}")
                        // Reset after a delay
                        delay(5000)
                        _importExportState.value = ImportExportState.Idle
                    }
                )
            } catch (e: Exception) {
                _importExportState.value = ImportExportState.Error("Import failed: ${e.localizedMessage ?: "Unknown error"}")
                // Reset after a delay
                delay(5000)
                _importExportState.value = ImportExportState.Idle
            }
        }
    }

    // New function to set sort option and re-sort the list
    fun setSortOption(sortOption: PersonSortOption) {
        if (_currentSortOption.value == sortOption) return
        
        Log.d(TAG, "Changing sort option from ${_currentSortOption.value} to $sortOption")
        _currentSortOption.value = sortOption
        
        // Instead of calling sortPersonList, directly sort the current list
        // to ensure immediate UI update regardless of loading state
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Sort the current visible list
                val sortedList = sortPersons(_pagedPersons.value, sortOption)
                _pagedPersons.value = sortedList
                
                // If we're showing search results, store the sort setting but don't modify _regularPersonsList
                if (!_showingSearchResults.value && _pagedPersons.value.isNotEmpty()) {
                    // Sort the backup list if we're not showing search results
                    _regularPersonsList = sortPersons(_regularPersonsList, sortOption)
                } else {
                    // If we are showing search results, make sure the search query is re-applied
                    // with the new sort option when we clear the search
                    Log.d(TAG, "Sort option changed while showing search results")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Safe sorting function to avoid concurrent modification
    private fun sortPersons(persons: List<Person>, sortOption: PersonSortOption): List<Person> {
        // Create a new list to avoid concurrent modification
        val personsCopy = persons.toList()
        
        return when (sortOption) {
            PersonSortOption.NAME_ASC -> 
                personsCopy.sortedBy { it.name.lowercase() }
                
            PersonSortOption.NAME_DESC -> 
                personsCopy.sortedByDescending { it.name.lowercase() }
                
            PersonSortOption.BALANCE_HIGH_TO_LOW -> 
                personsCopy.sortedByDescending { balances.value[it.id] ?: it.balance }
                
            PersonSortOption.BALANCE_LOW_TO_HIGH -> 
                personsCopy.sortedBy { balances.value[it.id] ?: it.balance }
                
            PersonSortOption.LAST_ADDED -> 
                personsCopy.sortedByDescending { it.id }
        }
    }

    // Update sort implementation to avoid concurrent modification
    fun sortPersonList(sortOption: PersonSortOption) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val currentList = if (_showingSearchResults.value) {
                    // Use toList() to create a copy before sorting
                    repository.searchPagedPersons(_searchQuery.value, 1000, 0).first().toList()
                } else {
                    // Use toList() to create a copy before sorting
                    repository.getPagedPersons(1000, 0).first().toList()
                }
                
                val sortedList = sortPersons(currentList, sortOption)
                
                _pagedPersons.value = sortedList
                _currentSortOption.value = sortOption
            } catch (e: Exception) {
                _error.value = "Error sorting list: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Clean up resources when ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        errorTimeoutJob?.cancel()
    }
}

// Add this sealed class at the end of the file
sealed class ImportExportState {
    object Idle : ImportExportState()
    data class Loading(val message: String) : ImportExportState()
    data class Success(val message: String) : ImportExportState()
    data class Error(val message: String) : ImportExportState()
}
