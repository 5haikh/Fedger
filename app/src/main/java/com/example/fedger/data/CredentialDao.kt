package com.example.fedger.data

import androidx.room.*
import com.example.fedger.model.Credential
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(credential: Credential): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(credentials: List<Credential>): List<Long>
    
    @Update
    suspend fun update(credential: Credential)
    
    @Update
    suspend fun updateAll(credentials: List<Credential>)
    
    @Delete
    suspend fun delete(credential: Credential)
    
    @Query("SELECT * FROM credentials WHERE entryId = :entryId ORDER BY position ASC")
    fun getCredentialsByEntryId(entryId: Int): Flow<List<Credential>>
    
    @Query("DELETE FROM credentials WHERE entryId = :entryId")
    suspend fun deleteAllForEntry(entryId: Int)
    
    @Query("SELECT * FROM credentials WHERE id = :id")
    fun getCredentialById(id: Int): Flow<Credential?>
    
    @Query("SELECT * FROM credentials")
    suspend fun getAllCredentials(): List<Credential>
} 