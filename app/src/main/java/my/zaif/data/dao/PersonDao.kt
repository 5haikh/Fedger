package my.zaif.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import my.zaif.data.entity.Person

@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person): Long

    @Update
    suspend fun updatePerson(person: Person)

    @Delete
    suspend fun deletePerson(person: Person)

    @Query("SELECT * FROM people ORDER BY name ASC")
    fun getAllPeople(): Flow<List<Person>>

    @Query("SELECT * FROM people WHERE id = :personId")
    suspend fun getPersonById(personId: Long): Person?
} 