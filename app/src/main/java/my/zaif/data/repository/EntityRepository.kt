package my.zaif.data.repository

import kotlinx.coroutines.flow.Flow
import my.zaif.data.dao.EntityDao
import my.zaif.data.entity.Entity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class EntityRepository(private val entityDao: EntityDao) {
    
    // Mutex for synchronizing entity operations
    private val entityMutex = Mutex()
    
    val allEntities: Flow<List<Entity>> = entityDao.getAllEntities()
    
    suspend fun insertEntity(entity: Entity): Long {
        if (entity.entityName.isBlank()) {
            throw IllegalArgumentException("Entity name cannot be blank")
        }
        
        return entityMutex.withLock {
            entityDao.insertEntity(entity)
        }
    }
    
    suspend fun updateEntity(entity: Entity) {
        if (entity.entityName.isBlank()) {
            throw IllegalArgumentException("Entity name cannot be blank")
        }
        
        entityMutex.withLock {
            // First verify the entity exists
            val existingEntity = entityDao.getEntityById(entity.id)
                ?: throw IllegalArgumentException("Cannot update non-existent entity with ID ${entity.id}")
                
            entityDao.updateEntity(entity)
        }
    }
    
    suspend fun deleteEntity(entity: Entity) {
        entityMutex.withLock {
            entityDao.deleteEntity(entity)
        }
    }
    
    suspend fun getEntityById(id: Long): Entity? {
        if (id <= 0) {
            throw IllegalArgumentException("Invalid entity ID: $id")
        }
        return entityDao.getEntityById(id)
    }
} 