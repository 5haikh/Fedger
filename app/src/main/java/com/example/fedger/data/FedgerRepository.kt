package com.example.fedger.data

import androidx.room.withTransaction
import com.example.fedger.model.Person
import com.example.fedger.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FedgerRepository(
    private val personDao: PersonDao,
    private val transactionDao: TransactionDao,
    private val database: FedgerDatabase
) {
    val allPersons: Flow<List<Person>> = personDao.getAllPersons()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun insertPerson(person: Person) = personDao.insertPerson(person)

    suspend fun deletePerson(person: Person) = personDao.deletePerson(person)

    fun getPersonById(id: Int) = personDao.getPersonById(id)

    fun getTransactionsForPerson(personId: Int) = transactionDao.getTransactionsForPerson(personId)

    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)
    
    // Pagination support
    fun getPagedPersons(limit: Int, offset: Int): Flow<List<Person>> = 
        personDao.getPagedPersons(limit, offset)
    
    fun getPersonCount(): Flow<Int> = personDao.getPersonCount()
    
    fun searchPagedPersons(query: String, limit: Int, offset: Int): Flow<List<Person>> =
        personDao.searchPagedPersons(query, limit, offset)
    
    fun getSearchPersonCount(query: String): Flow<Int> = 
        personDao.getSearchPersonCount(query)
        
    // Transaction pagination support
    fun getPagedTransactionsForPerson(personId: Int, limit: Int, offset: Int): Flow<List<Transaction>> =
        transactionDao.getPagedTransactionsForPerson(personId, limit, offset)
        
    fun getTransactionCountForPerson(personId: Int): Flow<Int> =
        transactionDao.getTransactionCountForPerson(personId)
        
    // Additional search support
    fun getPersonByExactName(name: String): Flow<List<Person>> =
        personDao.getPersonByExactName(name)
        
    fun getPersonsByIds(ids: List<Int>): Flow<List<Person>> =
        personDao.getPersonsByIds(ids)
    
    /**
     * Run multiple database operations in a single transaction.
     * If any operation fails, all operations will be rolled back.
     */
    suspend fun runInTransaction(block: suspend () -> Unit) {
        database.withTransaction {
            block()
        }
    }
    
    /**
     * Recalculate and update the balance for a person based on their transactions
     */
    suspend fun recalculatePersonBalance(personId: Int) {
        val transactions = getTransactionsForPerson(personId).first()
        val newBalance = transactions.sumOf { if (it.isCredit) it.amount else -it.amount }
        
        val person = getPersonById(personId).first() ?: return
        val updatedPerson = person.copy(balance = newBalance)
        personDao.updatePerson(updatedPerson)
    }
}
