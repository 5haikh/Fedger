package com.example.fedger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fedger.model.Credential
import com.example.fedger.model.PasswordEntry
import com.example.fedger.model.Person
import com.example.fedger.model.Transaction

@Database(
    entities = [
        Person::class,
        Transaction::class,
        PasswordEntry::class,
        Credential::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FedgerDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun transactionDao(): TransactionDao
    abstract fun passwordEntryDao(): PasswordEntryDao
    abstract fun credentialDao(): CredentialDao

    companion object {
        @Volatile
        private var INSTANCE: FedgerDatabase? = null

        fun getDatabase(context: Context): FedgerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FedgerDatabase::class.java,
                    "fedger_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
