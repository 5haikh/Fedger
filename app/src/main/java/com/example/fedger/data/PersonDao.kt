package com.example.fedger.data

import androidx.room.*
import com.example.fedger.model.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM persons")
    fun getAllPersons(): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE id = :id")
    fun getPersonById(id: Int): Flow<Person?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person): Long

    @Delete
    suspend fun deletePerson(person: Person)
    
    @Update
    suspend fun updatePerson(person: Person)
    
    @Query("SELECT * FROM persons LIMIT :limit OFFSET :offset")
    fun getPagedPersons(limit: Int, offset: Int): Flow<List<Person>>
    
    @Query("SELECT COUNT(*) FROM persons")
    fun getPersonCount(): Flow<Int>
    
    @Query("SELECT * FROM persons WHERE " +
           "LOWER(name) LIKE '%' || LOWER(:query) || '%' OR " +
           "LOWER(phoneNumber) LIKE '%' || LOWER(:query) || '%' OR " +
           "LOWER(address) LIKE '%' || LOWER(:query) || '%' " +
           "ORDER BY " +
           "CASE WHEN LOWER(name) = LOWER(:query) THEN 0 " +
           "WHEN LOWER(name) LIKE LOWER(:query) || '%' THEN 1 " +
           "WHEN LOWER(name) LIKE '% ' || LOWER(:query) || '%' THEN 2 " +
           "WHEN LOWER(name) LIKE '%' || LOWER(:query) THEN 3 " +
           "WHEN LOWER(name) LIKE '%' || LOWER(:query) || '%' THEN 4 " +
           "WHEN LOWER(phoneNumber) LIKE LOWER(:query) || '%' THEN 5 " +
           "WHEN LOWER(phoneNumber) LIKE '%' || LOWER(:query) || '%' THEN 6 " +
           "WHEN LOWER(address) LIKE '%' || LOWER(:query) || '%' THEN 7 " +
           "ELSE 8 END, name COLLATE NOCASE ASC " +
           "LIMIT :limit OFFSET :offset")
    fun searchPagedPersons(query: String, limit: Int, offset: Int): Flow<List<Person>>
    
    @Query("SELECT COUNT(*) FROM persons WHERE " +
           "LOWER(name) LIKE '%' || LOWER(:query) || '%' OR " +
           "LOWER(phoneNumber) LIKE '%' || LOWER(:query) || '%' OR " +
           "LOWER(address) LIKE '%' || LOWER(:query) || '%'")
    fun getSearchPersonCount(query: String): Flow<Int>
    
    @Query("SELECT * FROM persons WHERE id IN (:ids)")
    fun getPersonsByIds(ids: List<Int>): Flow<List<Person>>
    
    @Query("SELECT * FROM persons WHERE LOWER(name) = LOWER(:name)")
    fun getPersonByExactName(name: String): Flow<List<Person>>

    
}

