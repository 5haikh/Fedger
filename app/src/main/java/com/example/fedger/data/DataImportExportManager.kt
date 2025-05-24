package com.example.fedger.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.fedger.model.Person
import com.example.fedger.model.Transaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Date

private const val TAG = "DataImportExportManager"

// Data classes for import/export operations
data class ExportData(
    val persons: List<Person> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val exportDate: Long = System.currentTimeMillis(),
    val version: Int = 1
)

class DataImportExportManager(private val repository: FedgerRepository) {
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    /**
     * Export all data to a JSON file at the specified URI
     */
    suspend fun exportData(context: Context, uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting data export to URI: $uri")
            
            // Get all data from the repository
            val persons = repository.allPersons.first()
            val transactions = repository.allTransactions.first()
            
            Log.d(TAG, "Preparing export with ${persons.size} persons and ${transactions.size} transactions")
            
            // Create export data object
            val exportData = ExportData(
                persons = persons,
                transactions = transactions,
                exportDate = System.currentTimeMillis()
            )
            
            // Convert to JSON
            val jsonData = gson.toJson(exportData)
            
            // Write to file
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = BufferedWriter(OutputStreamWriter(outputStream))
                writer.write(jsonData)
                writer.flush()
            } ?: throw Exception("Failed to open output stream")
            
            Log.d(TAG, "Data export completed successfully")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting data", e)
            Result.failure(e)
        }
    }
    
    /**
     * Import data from a JSON file at the specified URI
     * Returns a triple of (success, personsImported, transactionsImported)
     */
    suspend fun importData(context: Context, uri: Uri): Result<Triple<Boolean, Int, Int>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting data import from URI: $uri")
            
            // Read JSON from file
            val jsonData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.readText()
            } ?: throw Exception("Failed to open input stream")
            
            // Parse JSON
            val type = object : TypeToken<ExportData>() {}.type
            val importedData = gson.fromJson<ExportData>(jsonData, type)
            
            Log.d(TAG, "Parsed import data with ${importedData.persons.size} persons and ${importedData.transactions.size} transactions")
            
            // Use a transaction to ensure all operations complete together or none at all
            repository.runInTransaction {
                // First insert persons to ensure they exist before adding transactions
                val personIds = mutableMapOf<Int, Long>()
                importedData.persons.forEach { person ->
                    // Store the original ID to map transactions later
                    val newId = repository.insertPerson(person.copy(id = 0)) // Let Room assign new ID
                    personIds[person.id] = newId
                }
                
                // Now insert transactions, updating personId references to new IDs
                importedData.transactions.forEach { transaction ->
                    val newPersonId = personIds[transaction.personId]?.toInt() ?: transaction.personId
                    val newTransaction = transaction.copy(personId = newPersonId)
                    repository.insertTransaction(newTransaction)
                }
                
                // Update balances for all imported persons (using new IDs)
                personIds.values.forEach { newId ->
                    repository.recalculatePersonBalance(newId.toInt())
                }
            }
            
            Log.d(TAG, "Data import completed successfully")
            Result.success(Triple(true, importedData.persons.size, importedData.transactions.size))
        } catch (e: Exception) {
            Log.e(TAG, "Error importing data", e)
            Result.failure(e)
        }
    }
} 