package my.zaif.data.entity

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
    indices = [Index("personId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personId: Long,
    val amount: Double,
    val description: String,
    val date: Long
) {
    init {
        require(!amount.isNaN()) { "Transaction amount cannot be NaN" }
        require(!amount.isInfinite()) { "Transaction amount cannot be infinite" }
    }
    
    companion object {
        // Factory method to create valid transactions
        fun create(personId: Long, amount: Double, description: String, date: Long): Transaction {
            // Additional validation can be added here
            if (description.isBlank()) {
                throw IllegalArgumentException("Transaction description cannot be empty")
            }
            if (date <= 0) {
                throw IllegalArgumentException("Transaction date must be a valid timestamp")
            }
            return Transaction(personId = personId, amount = amount, description = description, date = date)
        }
    }
} 