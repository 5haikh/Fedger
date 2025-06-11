package my.zaif.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import my.zaif.data.entity.Transaction

/**
 * Data class to hold person ID and their balance
 */
data class PersonBalance(
    val personId: Long,
    val balance: Double
)

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE personId = :personId ORDER BY date DESC")
    fun getAllTransactionsForPerson(personId: Long): Flow<List<Transaction>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE personId = :personId")
    fun getBalanceForPerson(personId: Long): Flow<Double>
    
    /**
     * Gets balances for all people in one query
     */
    @Query("SELECT personId, COALESCE(SUM(amount), 0.0) as balance FROM transactions GROUP BY personId")
    fun getAllBalances(): Flow<List<PersonBalance>>
} 