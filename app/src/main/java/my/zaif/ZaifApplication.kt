package my.zaif

import android.app.Application
import my.zaif.data.database.AppDatabase
import my.zaif.data.repository.EntityRepository
import my.zaif.data.repository.PersonRepository
import my.zaif.data.repository.StoredCredentialRepository
import my.zaif.data.repository.TransactionRepository

class ZaifApplication : Application() {
    // Database instance
    val database by lazy { AppDatabase.getDatabase(this) }
    
    // Repository instances
    val personRepository by lazy { PersonRepository(database.personDao()) }
    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val entityRepository by lazy { EntityRepository(database.entityDao()) }
    val storedCredentialRepository by lazy { StoredCredentialRepository(database.storedCredentialDao()) }
} 