package com.example.fedger.data

import androidx.room.*
import com.example.fedger.model.PasswordEntry
import com.example.fedger.model.PasswordEntryWithCredentials
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PasswordEntry): Long
    
    @Update
    suspend fun update(entry: PasswordEntry)
    
    @Delete
    suspend fun delete(entry: PasswordEntry)
    
    @Query("SELECT * FROM password_entries ORDER BY title ASC")
    fun getAllPasswordEntries(): Flow<List<PasswordEntry>>
    
    @Query("SELECT * FROM password_entries WHERE id = :id")
    fun getPasswordEntryById(id: Int): Flow<PasswordEntry?>
    
    @Transaction
    @Query("SELECT * FROM password_entries WHERE id = :id")
    fun getPasswordEntryWithCredentials(id: Int): Flow<PasswordEntryWithCredentials?>
    
    @Transaction
    @Query("SELECT * FROM password_entries ORDER BY title ASC")
    fun getAllPasswordEntriesWithCredentials(): Flow<List<PasswordEntryWithCredentials>>
    
    @Query("SELECT * FROM password_entries WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchPasswordEntries(query: String): Flow<List<PasswordEntry>>
    
    @Query("SELECT COUNT(*) FROM password_entries")
    fun getCount(): Flow<Int>
    
    @Query("SELECT DISTINCT category FROM password_entries WHERE category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
    
    @Query("SELECT * FROM password_entries WHERE category = :category ORDER BY title ASC")
    fun getPasswordEntriesByCategory(category: String): Flow<List<PasswordEntry>>
} 