package com.example.fedger.data

import androidx.room.*
import com.example.fedger.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE personId = :personId")
    fun getTransactionsForPerson(personId: Int): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    // Pagination support for transactions
    @Query("SELECT * FROM transactions WHERE personId = :personId ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getPagedTransactionsForPerson(personId: Int, limit: Int, offset: Int): Flow<List<Transaction>>
    
    @Query("SELECT COUNT(*) FROM transactions WHERE personId = :personId")
    fun getTransactionCountForPerson(personId: Int): Flow<Int>
}
