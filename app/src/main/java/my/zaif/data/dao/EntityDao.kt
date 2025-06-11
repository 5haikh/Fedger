package my.zaif.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import my.zaif.data.entity.Entity

@Dao
interface EntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntity(entity: Entity): Long

    @Update
    suspend fun updateEntity(entity: Entity)

    @Delete
    suspend fun deleteEntity(entity: Entity)

    @Query("SELECT * FROM entities ORDER BY entityName ASC")
    fun getAllEntities(): Flow<List<Entity>>

    @Query("SELECT * FROM entities WHERE id = :entityId")
    suspend fun getEntityById(entityId: Long): Entity?
} 