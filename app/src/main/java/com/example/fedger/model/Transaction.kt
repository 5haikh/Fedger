package com.example.fedger.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["personId"])
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val personId: Int,
    val amount: Double,
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val isCredit: Boolean // true for "To Receive" (they owe us), false for "To Pay" (we owe them)
)
