package my.zaif.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import my.zaif.data.entity.StoredCredential

@Dao
interface StoredCredentialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: StoredCredential): Long

    @Update
    suspend fun updateCredential(credential: StoredCredential)

    @Delete
    suspend fun deleteCredential(credential: StoredCredential)

    @Query("SELECT * FROM stored_credentials WHERE entityId = :entityId ORDER BY credentialLabel ASC")
    fun getCredentialsForEntity(entityId: Long): Flow<List<StoredCredential>>

    @Query("SELECT * FROM stored_credentials WHERE id = :credentialId")
    suspend fun getCredentialById(credentialId: Long): StoredCredential?
} 