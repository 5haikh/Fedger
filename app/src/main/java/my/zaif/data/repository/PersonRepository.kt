package my.zaif.data.repository

import kotlinx.coroutines.flow.Flow
import my.zaif.data.dao.PersonDao
import my.zaif.data.entity.Person
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PersonRepository(private val personDao: PersonDao) {
    
    // Mutex for synchronizing person operations
    private val personMutex = Mutex()
    
    val allPeople: Flow<List<Person>> = personDao.getAllPeople()
    
    suspend fun insertPerson(person: Person): Long {
        if (person.name.isBlank()) {
            throw IllegalArgumentException("Person name cannot be blank")
        }
        
        return personMutex.withLock {
            personDao.insertPerson(person)
        }
    }
    
    suspend fun updatePerson(person: Person) {
        if (person.name.isBlank()) {
            throw IllegalArgumentException("Person name cannot be blank")
        }
        
        personMutex.withLock {
            // First verify the person exists
            val existingPerson = personDao.getPersonById(person.id)
                ?: throw IllegalArgumentException("Cannot update non-existent person with ID ${person.id}")
                
            personDao.updatePerson(person)
        }
    }
    
    suspend fun deletePerson(person: Person) {
        personMutex.withLock {
            personDao.deletePerson(person)
        }
    }
    
    suspend fun getPersonById(id: Long): Person? {
        if (id <= 0) {
            throw IllegalArgumentException("Invalid person ID: $id")
        }
        return personDao.getPersonById(id)
    }
} 