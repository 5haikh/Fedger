package com.example.fedger.data

import androidx.room.*
import com.example.fedger.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE personId = :personId ORDER BY date DESC")
    fun getTransactionsForPerson(personId: Int): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    // Get transaction by ID
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Int): Flow<Transaction?>
    
    // Delete all transactions for a person
    @Query("DELETE FROM transactions WHERE personId = :personId")
    suspend fun deleteAllForPerson(personId: Int)
    
    // Calculate sum of all transactions for a person
    @Query("SELECT SUM(CASE WHEN isCredit = 1 THEN amount ELSE -amount END) FROM transactions WHERE personId = :personId")
    suspend fun calculateBalanceForPerson(personId: Int): Double?
    
    // Pagination support for transactions
    @Query("SELECT * FROM transactions WHERE personId = :personId ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getPagedTransactionsForPerson(personId: Int, limit: Int, offset: Int): Flow<List<Transaction>>
    
    @Query("SELECT COUNT(*) FROM transactions WHERE personId = :personId")
    fun getTransactionCountForPerson(personId: Int): Flow<Int>
    
    // Get recent transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<Transaction>>
}
