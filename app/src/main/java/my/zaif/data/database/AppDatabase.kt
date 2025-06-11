package my.zaif.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import my.zaif.data.dao.EntityDao
import my.zaif.data.dao.PersonDao
import my.zaif.data.dao.StoredCredentialDao
import my.zaif.data.dao.TransactionDao
import my.zaif.data.entity.Entity
import my.zaif.data.entity.Person
import my.zaif.data.entity.StoredCredential
import my.zaif.data.entity.Transaction

@Database(
    entities = [Person::class, Transaction::class, Entity::class, StoredCredential::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun transactionDao(): TransactionDao
    abstract fun entityDao(): EntityDao
    abstract fun storedCredentialDao(): StoredCredentialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zaif_database"
                )
                    .fallbackToDestructiveMigration(true) // This will recreate tables if migrations aren't defined
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 