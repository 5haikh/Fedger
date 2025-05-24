package com.example.fedger.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a password entry which can contain multiple credentials
 */
@Entity(tableName = "password_entries")
data class PasswordEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 