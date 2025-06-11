package my.zaif.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entities")
data class Entity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityName: String
) 