package com.example.fedger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fedger.model.Person
import com.example.fedger.model.Transaction

@Database(entities = [Person::class, Transaction::class], version = 1, exportSchema = false)
abstract class FedgerDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: FedgerDatabase? = null

        fun getDatabase(context: Context): FedgerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FedgerDatabase::class.java,
                    "fedger_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
