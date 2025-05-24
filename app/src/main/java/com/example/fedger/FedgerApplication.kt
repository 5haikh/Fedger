package com.example.fedger

import android.app.Application
import android.util.Log
import com.example.fedger.data.DataImportExportManager
import com.example.fedger.data.FedgerDatabase
import com.example.fedger.data.FedgerRepository
import com.example.fedger.data.PasswordImportExportManager
import com.example.fedger.data.PasswordRepository

class FedgerApplication : Application() {
    // Database and repositories
    private val database by lazy { FedgerDatabase.getDatabase(this) }
    val repository by lazy { FedgerRepository(database.personDao(), database.transactionDao(), database) }
    val dataManager by lazy { DataImportExportManager(repository) }
    
    // Password manager repository
    val passwordRepository by lazy { 
        PasswordRepository(database.passwordEntryDao(), database.credentialDao(), database) 
    }
    
    // Password import/export manager
    val passwordDataManager by lazy { 
        PasswordImportExportManager(passwordRepository, database.credentialDao()) 
    }
    
    companion object {
        private const val TAG = "FedgerApplication"
    }
}
