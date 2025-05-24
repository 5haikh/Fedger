package com.example.fedger.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relationship class for retrieving a password entry with all its credentials
 */
data class PasswordEntryWithCredentials(
    @Embedded val entry: PasswordEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "entryId"
    )
    val credentials: List<Credential>
) 