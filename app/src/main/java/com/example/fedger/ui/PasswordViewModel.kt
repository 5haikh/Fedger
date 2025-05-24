package com.example.fedger.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fedger.FedgerApplication
import com.example.fedger.data.EncryptionUtil
import com.example.fedger.data.PasswordRepository
import com.example.fedger.model.Credential
import com.example.fedger.model.PasswordEntry
import com.example.fedger.model.PasswordEntryWithCredentials
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.SecureRandom
import kotlin.random.Random

/**
 * ViewModel for password management
 */
class PasswordViewModel(private val repository: PasswordRepository) : ViewModel() {
    
    private val TAG = "PasswordViewModel"
    
    // All password entries with improved sharing strategy
    val passwordEntries = repository.allPasswordEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Currently selected entry
    private val _selectedEntry = MutableStateFlow<PasswordEntry?>(null)
    val selectedEntry: StateFlow<PasswordEntry?> = _selectedEntry.asStateFlow()
    
    // Credentials for selected entry
    private val _selectedEntryCredentials = MutableStateFlow<List<Credential>>(emptyList())
    val selectedEntryCredentials: StateFlow<List<Credential>> = _selectedEntryCredentials.asStateFlow()
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Search results
    private val _searchResults = MutableStateFlow<List<PasswordEntry>>(emptyList())
    val searchResults: StateFlow<List<PasswordEntry>> = _searchResults.asStateFlow()
    
    // Categories with improved sharing strategy
    val categories = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Selected category
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    // Filtered entries by category
    private val _filteredEntries = MutableStateFlow<List<PasswordEntry>>(emptyList())
    val filteredEntries: StateFlow<List<PasswordEntry>> = _filteredEntries.asStateFlow()
    
    // Error state with timeout handling
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Job for tracking error timeout
    private var errorTimeoutJob: Job? = null
    
    // Error timeout duration in milliseconds (default: 5 seconds)
    private val ERROR_TIMEOUT_DURATION = 5000L
    
    // Add import/export related state
    private val _importExportState = MutableStateFlow<ImportExportState>(ImportExportState.Idle)
    val importExportState: StateFlow<ImportExportState> = _importExportState.asStateFlow()
    
    // Password generation related state
    private val _passwordLength = MutableStateFlow(12)
    val passwordLength: StateFlow<Int> = _passwordLength.asStateFlow()
    
    private val _useUppercase = MutableStateFlow(true)
    val useUppercase: StateFlow<Boolean> = _useUppercase.asStateFlow()
    
    private val _useLowercase = MutableStateFlow(true)
    val useLowercase: StateFlow<Boolean> = _useLowercase.asStateFlow()
    
    private val _useNumbers = MutableStateFlow(true)
    val useNumbers: StateFlow<Boolean> = _useNumbers.asStateFlow()
    
    private val _useSpecialChars = MutableStateFlow(true)
    val useSpecialChars: StateFlow<Boolean> = _useSpecialChars.asStateFlow()
    
    private val _generatedPassword = MutableStateFlow("")
    val generatedPassword: StateFlow<String> = _generatedPassword.asStateFlow()
    
    // Coroutine exception handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        setError("Error: ${throwable.localizedMessage ?: "Unknown error occurred"}")
    }
    
    init {
        // Monitor search query changes
        viewModelScope.launch {
            searchQuery.collectLatest { query ->
                if (query.isNotEmpty()) {
                    repository.searchPasswordEntries(query).collect { results ->
                        _searchResults.value = results
                    }
                } else {
                    _searchResults.value = emptyList()
                }
            }
        }
        
        // Monitor category filter changes
        viewModelScope.launch {
            selectedCategory.collectLatest { category ->
                if (category != null) {
                    repository.getPasswordEntriesByCategory(category).collect { entries ->
                        _filteredEntries.value = entries
                    }
                } else {
                    _filteredEntries.value = emptyList()
                }
            }
        }
    }
    
    // Password Entry operations
    fun selectEntry(entry: PasswordEntry) {
        _selectedEntry.value = entry
        loadCredentialsForEntry(entry.id)
    }
    
    fun clearSelectedEntry() {
        _selectedEntry.value = null
        _selectedEntryCredentials.value = emptyList()
    }
    
    private fun loadCredentialsForEntry(entryId: Int) {
        viewModelScope.launch(exceptionHandler) {
            repository.getCredentialsForEntry(entryId).collect { credentials ->
                _selectedEntryCredentials.value = credentials
            }
        }
    }
    
    fun getEntryWithCredentials(entryId: Int): Flow<PasswordEntryWithCredentials?> {
        return repository.getPasswordEntryWithCredentials(entryId)
    }
    
    fun addPasswordEntry(entry: PasswordEntry) {
        viewModelScope.launch(exceptionHandler) {
            repository.addPasswordEntry(entry)
        }
    }
    
    fun updatePasswordEntry(entry: PasswordEntry) {
        viewModelScope.launch(exceptionHandler) {
            repository.updatePasswordEntry(entry)
        }
    }
    
    fun deletePasswordEntry(entry: PasswordEntry) {
        viewModelScope.launch(exceptionHandler) {
            repository.deletePasswordEntry(entry)
        }
    }
    
    // Save or update password entry with credentials
    fun savePasswordEntryWithCredentials(entry: PasswordEntry, credentials: List<Credential>) {
        viewModelScope.launch(exceptionHandler) {
            if (entry.id == 0) {
                repository.savePasswordEntryWithCredentials(entry, credentials)
            } else {
                repository.updatePasswordEntryWithCredentials(entry, credentials)
            }
        }
    }
    
    // Credential operations
    fun addCredential(credential: Credential) {
        viewModelScope.launch(exceptionHandler) {
            repository.addCredential(credential)
        }
    }
    
    fun updateCredential(credential: Credential) {
        viewModelScope.launch(exceptionHandler) {
            repository.updateCredential(credential)
        }
    }
    
    fun deleteCredential(credential: Credential) {
        viewModelScope.launch(exceptionHandler) {
            repository.deleteCredential(credential)
        }
    }
    
    // Search operations
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun clearSearchQuery() {
        _searchQuery.value = ""
    }
    
    // Category operations
    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }
    
    // Error handling
    fun setError(message: String) {
        _error.value = message
        
        // Cancel any existing timeout job
        errorTimeoutJob?.cancel()
        
        // Create a new timeout job to auto-clear the error
        errorTimeoutJob = viewModelScope.launch {
            delay(ERROR_TIMEOUT_DURATION)
            clearError()
        }
    }
    
    fun clearError() {
        _error.value = null
        errorTimeoutJob?.cancel()
        errorTimeoutJob = null
    }
    
    // Decrypt credential value helper
    fun decryptCredentialValue(credential: Credential): String {
        return try {
            repository.decryptCredentialValue(credential)
        } catch (e: Exception) {
            setError("Decryption failed: ${e.message}")
            // If decryption fails, show error and return placeholder
            "••••••••" // Return masked placeholder instead of the raw value
        }
    }
    
    /**
     * Try to recover all credentials that might have encryption issues
     * @return The number of credentials that were successfully recovered
     */
    fun attemptCredentialRecovery(): Flow<Int> = flow {
        _importExportState.value = ImportExportState.Loading("Attempting credential recovery...")
        var recovered = 0
        
        try {
            // Enable recovery mode in EncryptionUtil
            EncryptionUtil.enableRecoveryMode(true)
            
            // Get all entries with credentials
            val allEntries = repository.allPasswordEntriesWithCredentials.first()
            
            // Process each credential
            allEntries.forEach { entryWithCreds ->
                entryWithCreds.credentials.forEach { credential ->
                    try {
                        // Try to decrypt with new recovery methods
                        val decryptedValue = repository.decryptCredentialValue(credential)
                        
                        // If we get here, the credential was successfully decrypted
                        // No need to update it
                    } catch (e: Exception) {
                        // Recovery needed for this credential
                        val originalValue = credential.value
                        
                        // Try recovery method
                        val recoveredValue = EncryptionUtil.tryDecrypt(originalValue)
                        
                        if (recoveredValue != null) {
                            // We recovered the value, re-encrypt with primary method
                            val updatedCredential = credential.copy(
                                value = EncryptionUtil.encrypt(recoveredValue)
                            )
                            repository.updateCredential(updatedCredential)
                            recovered++
                        }
                    }
                }
            }
            
            // Disable recovery mode
            EncryptionUtil.enableRecoveryMode(false)
            
            emit(recovered)
            
            if (recovered > 0) {
                _importExportState.value = ImportExportState.Success("Recovery completed. Fixed $recovered credentials.")
            } else {
                _importExportState.value = ImportExportState.Success("Recovery scan completed. No issues found.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Recovery failed", e)
            _importExportState.value = ImportExportState.Error("Recovery failed: ${e.message}")
            emit(0)
        }
    }
    
    /**
     * Check if encryption system is healthy
     * @return true if the encryption system is working properly
     */
    fun checkEncryptionHealth(): Boolean {
        return try {
            // Check if the primary key is available
            val primaryKeyAvailable = EncryptionUtil.isPrimaryKeyAvailable()
            
            if (!primaryKeyAvailable) {
                setError("Warning: Primary encryption key not available. Using backup system.")
                return false
            }
            
            // Test encryption/decryption
            val testValue = "test_encryption_${System.currentTimeMillis()}"
            val encrypted = EncryptionUtil.encrypt(testValue)
            val decrypted = EncryptionUtil.decrypt(encrypted)
            
            return testValue == decrypted
        } catch (e: Exception) {
            setError("Encryption system issue detected: ${e.message}")
            false
        }
    }
    
    /**
     * Reset the encryption key as a last resort
     * WARNING: This may make existing encrypted data unreadable unless backup system works
     */
    fun resetEncryptionKey(): Boolean {
        val result = EncryptionUtil.resetEncryptionKey()
        if (result) {
            setError("Encryption key has been reset. Recovery mode enabled.")
            // Enable recovery mode automatically after reset
            EncryptionUtil.enableRecoveryMode(true)
        } else {
            setError("Failed to reset encryption key.")
        }
        return result
    }
    
    /**
     * Export passwords to a file
     */
    fun exportPasswords(context: Context, uri: Uri, useDecryptedValues: Boolean = false) {
        viewModelScope.launch(exceptionHandler) {
            _importExportState.value = ImportExportState.Loading("Exporting password data...")
            
            try {
                val application = context.applicationContext as FedgerApplication
                val result = application.passwordDataManager.exportData(
                    context, 
                    uri, 
                    useDecryptedValues
                )
                
                result.fold(
                    onSuccess = { success: Boolean ->
                        _importExportState.value = ImportExportState.Success(
                            if (useDecryptedValues) {
                                "Password export completed with PLAINTEXT VALUES! Secure this file carefully!"
                            } else {
                                "Password export completed successfully with encrypted values!"
                            }
                        )
                        // Reset after a delay
                        delay(3000)
                        _importExportState.value = ImportExportState.Idle
                    },
                    onFailure = { e: Throwable ->
                        _importExportState.value = ImportExportState.Error("Password export failed: ${e.localizedMessage ?: "Unknown error"}")
                        // Reset after a delay
                        delay(5000)
                        _importExportState.value = ImportExportState.Idle
                    }
                )
            } catch (e: Exception) {
                _importExportState.value = ImportExportState.Error("Password export failed: ${e.localizedMessage ?: "Unknown error"}")
                // Reset after a delay
                delay(5000)
                _importExportState.value = ImportExportState.Idle
            }
        }
    }
    
    /**
     * Import passwords from a file
     */
    fun importPasswords(context: Context, uri: Uri, importDecryptedValues: Boolean = true) {
        viewModelScope.launch(exceptionHandler) {
            _importExportState.value = ImportExportState.Loading("Importing password data...")
            
            try {
                val application = context.applicationContext as FedgerApplication
                val result = application.passwordDataManager.importData(
                    context, 
                    uri, 
                    importDecryptedValues
                )
                
                result.fold(
                    onSuccess = { triple: Triple<Boolean, Int, Int> ->
                        val (success, entriesCount, credentialsCount) = triple
                        if (success) {
                            _importExportState.value = ImportExportState.Success(
                                "Password import completed successfully!\nImported $entriesCount passwords and $credentialsCount credentials."
                            )
                            // Reset after a delay
                            delay(3000)
                            _importExportState.value = ImportExportState.Idle
                        } else {
                            _importExportState.value = ImportExportState.Error("Password import failed.")
                            // Reset after a delay
                            delay(5000)
                            _importExportState.value = ImportExportState.Idle
                        }
                    },
                    onFailure = { e: Throwable ->
                        _importExportState.value = ImportExportState.Error("Password import failed: ${e.localizedMessage ?: "Unknown error"}")
                        // Reset after a delay
                        delay(5000)
                        _importExportState.value = ImportExportState.Idle
                    }
                )
            } catch (e: Exception) {
                _importExportState.value = ImportExportState.Error("Password import failed: ${e.localizedMessage ?: "Unknown error"}")
                // Reset after a delay
                delay(5000)
                _importExportState.value = ImportExportState.Idle
            }
        }
    }
    
    // Password generation
    fun setPasswordLength(length: Int) {
        _passwordLength.value = length
    }
    
    fun setUseUppercase(use: Boolean) {
        _useUppercase.value = use
    }
    
    fun setUseLowercase(use: Boolean) {
        _useLowercase.value = use
    }
    
    fun setUseNumbers(use: Boolean) {
        _useNumbers.value = use
    }
    
    fun setUseSpecialChars(use: Boolean) {
        _useSpecialChars.value = use
    }
    
    fun generatePassword() {
        val length = passwordLength.value
        val useUppercase = useUppercase.value
        val useLowercase = useLowercase.value
        val useNumbers = useNumbers.value
        val useSpecialChars = useSpecialChars.value
        
        val upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowerCaseChars = "abcdefghijklmnopqrstuvwxyz"
        val numberChars = "0123456789"
        val specialChars = "!@#$%^&*()_-+=<>?/[]{}|"
        
        var allowedChars = ""
        
        if (useUppercase) allowedChars += upperCaseChars
        if (useLowercase) allowedChars += lowerCaseChars
        if (useNumbers) allowedChars += numberChars
        if (useSpecialChars) allowedChars += specialChars
        
        // Fallback to lowercase if nothing selected
        if (allowedChars.isEmpty()) allowedChars = lowerCaseChars
        
        val password = StringBuilder(length)
        val secureRandom = SecureRandom()
        
        for (i in 0 until length) {
            val randomIndex = secureRandom.nextInt(allowedChars.length)
            password.append(allowedChars[randomIndex])
        }
        
        _generatedPassword.value = password.toString()
    }
    
    /**
     * Factory for creating PasswordViewModel with repository
     */
    class Factory(private val repository: PasswordRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PasswordViewModel::class.java)) {
                return PasswordViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    
    // Override onCleared to clean up resources explicitly
    override fun onCleared() {
        super.onCleared()
        errorTimeoutJob?.cancel()
    }
} 