package my.zaif.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stored_credentials",
    foreignKeys = [
        ForeignKey(
            entity = my.zaif.data.entity.Entity::class,
            parentColumns = ["id"],
            childColumns = ["entityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("entityId")]
)
data class StoredCredential(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityId: Long,
    val credentialLabel: String,
    val credentialType: CredentialType,
    val username: String? = null,
    val passwordValue: String? = null,
    val customFieldKey: String? = null
)

enum class CredentialType {
    USERNAME_PASSWORD,
    PASSWORD_ONLY,
    PIN_ONLY,
    CUSTOM
} 