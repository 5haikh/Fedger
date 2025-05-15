package com.example.fedger

import android.app.Application
import com.example.fedger.data.DataImportExportManager

class FedgerApplication : Application() {
    val database by lazy { com.example.fedger.data.FedgerDatabase.getDatabase(this) }
    val repository by lazy { 
        com.example.fedger.data.FedgerRepository(
            database.personDao(),
            database.transactionDao(),
            database
        )
    }
    
    // Initialize the data import/export manager
    val dataManager by lazy { 
        com.example.fedger.data.DataImportExportManager(repository)
    }
}
