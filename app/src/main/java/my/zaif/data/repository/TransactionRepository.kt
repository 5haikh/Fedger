package my.zaif.data.repository

import kotlinx.coroutines.flow.Flow
import my.zaif.data.dao.PersonBalance
import my.zaif.data.dao.TransactionDao
import my.zaif.data.entity.Transaction
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.first

class TransactionRepository(private val transactionDao: TransactionDao) {
    
    // Mutex for synchronizing transaction operations
    private val transactionMutex = Mutex()
    
    fun getAllTransactionsForPerson(personId: Long): Flow<List<Transaction>> {
        return transactionDao.getAllTransactionsForPerson(personId)
    }
    
    fun getBalanceForPerson(personId: Long): Flow<Double> {
        return transactionDao.getBalanceForPerson(personId)
    }
    
    fun getAllBalances(): Flow<List<PersonBalance>> {
        return transactionDao.getAllBalances()
    }
    
    suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionMutex.withLock {
            val result = transactionDao.insertTransaction(transaction)
            
            // Verify data integrity after insertion
            verifyDataIntegrity(transaction.personId)
            
            result
        }
    }
    
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionMutex.withLock {
            transactionDao.deleteTransaction(transaction)
            
            // Verify data integrity after deletion
            verifyDataIntegrity(transaction.personId)
        }
    }
    
    /**
     * Verifies the data integrity by comparing the sum of all transactions
     * with the calculated balance from the database.
     */
    private suspend fun verifyDataIntegrity(personId: Long) {
        val transactions = getAllTransactionsForPerson(personId).first()
        val calculatedSum = transactions.sumOf { it.amount }
        val storedBalance = getBalanceForPerson(personId).first()
        
        // Check if the calculated sum matches the stored balance with a small tolerance for floating point errors
        if (Math.abs(calculatedSum - storedBalance) > 0.001) {
            throw IllegalStateException("Data integrity violation: calculated sum ($calculatedSum) doesn't match stored balance ($storedBalance)")
        }
    }
} 