package com.example.fedger.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single credential belonging to a password entry
 * This is completely generic and can represent any type of credential
 */
@Entity(
    tableName = "credentials",
    foreignKeys = [
        ForeignKey(
            entity = PasswordEntry::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["entryId"])
    ]
)
data class Credential(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val entryId: Int,
    val label: String,  // User-defined label (e.g., "Username", "PIN", "Security Question")
    val value: String,  // The actual sensitive data (will be encrypted)
    val notes: String = "",
    val isProtected: Boolean = true, // Whether to hide/mask the value by default
    val position: Int = 0, // For ordering credentials within an entry
    val type: String = CredentialType.CUSTOM.name, // Type of credential (USERNAME_PASSWORD, PASSWORD_ONLY, PIN, CUSTOM)
    val displayName: String = "", // Optional custom display name that overrides default type name
    val createdAt: Long = System.currentTimeMillis()
) 