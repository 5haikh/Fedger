package com.example.fedger.data

import androidx.room.withTransaction
import com.example.fedger.model.Person
import com.example.fedger.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import android.util.Log

class FedgerRepository(
    private val personDao: PersonDao,
    private val transactionDao: TransactionDao,
    private val database: FedgerDatabase
) {
    private val TAG = "FedgerRepository"
    
    val allPersons: Flow<List<Person>> = personDao.getAllPersons()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun insertPerson(person: Person) = personDao.insertPerson(person)

    suspend fun updatePerson(person: Person) = personDao.updatePerson(person)

    suspend fun deletePerson(person: Person) {
        // Use transaction to ensure we clean up properly
        database.withTransaction {
            // Delete all transactions for this person first
            transactionDao.deleteAllForPerson(person.id)
            // Then delete the person
            personDao.deletePerson(person)
        }
    }

    fun getPersonById(id: Int) = personDao.getPersonById(id)

    fun getTransactionsForPerson(personId: Int) = transactionDao.getTransactionsForPerson(personId)

    /**
     * Insert a transaction and update the person's balance in a single atomic transaction
     */
    suspend fun insertTransaction(transaction: Transaction) {
        database.withTransaction {
            // Insert the transaction first
            val transactionId = transactionDao.insertTransaction(transaction)
            
            // Get the person
            val person = personDao.getPersonById(transaction.personId).first()
            
            // Update person balance
            person?.let {
                val newBalance = if (transaction.isCredit) {
                    it.balance + transaction.amount
                } else {
                    it.balance - transaction.amount
                }
                
                val updatedPerson = it.copy(balance = newBalance)
                personDao.updatePerson(updatedPerson)
                
                Log.d(TAG, "Transaction added, updated balance for person ${person.id} from ${person.balance} to $newBalance")
            }
        }
    }

    /**
     * Update a transaction and recalculate the person's balance
     */
    suspend fun updateTransaction(transaction: Transaction) {
        database.withTransaction {
            // First get the original transaction to calculate the balance difference
            val originalTransaction = transactionDao.getTransactionById(transaction.id).first()
            
            // Update the transaction
            transactionDao.updateTransaction(transaction)
            
            // Recalculate and update the person's balance
            originalTransaction?.let {
                // Calculate the balance change from old transaction to new one
                var balanceChange = 0.0
                
                // Remove the effect of the old transaction
                balanceChange -= if (it.isCredit) it.amount else -it.amount
                
                // Add the effect of the new transaction
                balanceChange += if (transaction.isCredit) transaction.amount else -transaction.amount
                
                // Get the person and update balance
                val person = personDao.getPersonById(transaction.personId).first()
                person?.let { p ->
                    val newBalance = p.balance + balanceChange
                    val updatedPerson = p.copy(balance = newBalance)
                    personDao.updatePerson(updatedPerson)
                    
                    Log.d(TAG, "Transaction updated, changed balance for person ${p.id} by $balanceChange to $newBalance")
                }
            }
        }
    }

    /**
     * Delete a transaction and update the person's balance
     */
    suspend fun deleteTransaction(transaction: Transaction) {
        database.withTransaction {
            // Delete the transaction
            transactionDao.deleteTransaction(transaction)
            
            // Update the person's balance
            val person = personDao.getPersonById(transaction.personId).first()
            person?.let {
                val balanceAdjustment = if (transaction.isCredit) -transaction.amount else transaction.amount
                val newBalance = it.balance + balanceAdjustment
                
                val updatedPerson = it.copy(balance = newBalance)
                personDao.updatePerson(updatedPerson)
                
                Log.d(TAG, "Transaction deleted, adjusted balance for person ${person.id} by $balanceAdjustment to $newBalance")
            }
        }
    }
    
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
     * This is useful for ensuring data consistency if balances get out of sync
     */
    suspend fun recalculatePersonBalance(personId: Int): Double {
        return database.withTransaction {
            // Get all transactions for this person
            val transactions = getTransactionsForPerson(personId).first()
            val calculatedBalance = transactions.sumOf { if (it.isCredit) it.amount else -it.amount }
            
            // Get the person and check if balance needs updating
            val person = getPersonById(personId).first()
            if (person != null && person.balance != calculatedBalance) {
                Log.d(TAG, "Recalculated balance for person $personId: ${person.balance} -> $calculatedBalance")
                val updatedPerson = person.copy(balance = calculatedBalance)
                personDao.updatePerson(updatedPerson)
            }
            
            calculatedBalance
        }
    }
    
    /**
     * Verify and fix all person balances in the database
     * @return The number of balances that were corrected
     */
    suspend fun verifyAllBalances(): Int {
        var fixedCount = 0
        
        // Get all persons
        val persons = allPersons.first()
        
        for (person in persons) {
            val currentBalance = person.balance
            val calculatedBalance = recalculatePersonBalance(person.id)
            
            if (currentBalance != calculatedBalance) {
                fixedCount++
            }
        }
        
        return fixedCount
    }
    
    /**
     * Get a flow of the current person's data with real-time balance
     * @return Flow of Person with live-calculated balance from transactions
     */
    fun getPersonWithLiveBalance(personId: Int): Flow<Person?> {
        // Get the person as a base
        return personDao.getPersonById(personId).map { person ->
            person?.let {
                // Calculate current balance from transactions
                val transactions = transactionDao.getTransactionsForPerson(personId).first()
                val liveBalance = transactions.sumOf { tx -> 
                    if (tx.isCredit) tx.amount else -tx.amount 
                }
                
                // If balance isn't matching what's stored, update the database
                if (it.balance != liveBalance) {
                    val updatedPerson = it.copy(balance = liveBalance)
                    personDao.updatePerson(updatedPerson)
                    updatedPerson
                } else {
                    it
                }
            }
        }
    }
}
