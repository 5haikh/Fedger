package my.zaif.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import my.zaif.data.entity.CredentialType
import my.zaif.data.entity.StoredCredential
import my.zaif.ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoredCredentialItem(
    credential: StoredCredential,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    ContentCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Spacing.screenHorizontalPadding, 
                vertical = Spacing.screenVerticalPadding / 2
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = 3
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardInnerPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Credential type icon with circle background
            Box(
                modifier = Modifier
                    .size(Spacing.avatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val (icon, contentDescription) = when (credential.credentialType) {
                    CredentialType.USERNAME_PASSWORD -> Icons.Default.Password to "Username and Password"
                    CredentialType.PASSWORD_ONLY -> Icons.Default.Key to "Password Only"
                    CredentialType.PIN_ONLY -> Icons.Default.Pin to "PIN Only"
                    CredentialType.CUSTOM -> Icons.Default.TextFields to "Custom Field"
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(Spacing.iconSize)
                )
            }
            
            // Credential details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.contentPadding)
            ) {
                Text(
                    text = credential.credentialLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = getCredentialTypeText(credential.credentialType),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Spacing.extraSmall)
                )
                
                // Show username if present
                credential.username?.let {
                    if (it.isNotBlank() && credential.credentialType == CredentialType.USERNAME_PASSWORD) {
                        Text(
                            text = "Username: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = Spacing.extraSmall)
                        )
                    }
                }
                
                // Show custom field key if present
                credential.customFieldKey?.let {
                    if (it.isNotBlank() && credential.credentialType == CredentialType.CUSTOM) {
                        Text(
                            text = "Field: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = Spacing.extraSmall)
                        )
                    }
                }
                
                // Show password/value if visible
                if (passwordVisible && credential.passwordValue != null) {
                    val label = when (credential.credentialType) {
                        CredentialType.USERNAME_PASSWORD -> "Password: "
                        CredentialType.PASSWORD_ONLY -> "Password: "
                        CredentialType.PIN_ONLY -> "PIN: "
                        CredentialType.CUSTOM -> "${credential.customFieldKey ?: "Value"}: "
                    }
                    
                    Text(
                        text = "$label${credential.passwordValue}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = Spacing.extraSmall)
                    )
                }
            }
            
            // Password visibility toggle
            IconButton(
                onClick = { passwordVisible = !passwordVisible }
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun getCredentialTypeText(credentialType: CredentialType): String {
    return when (credentialType) {
        CredentialType.USERNAME_PASSWORD -> "Username & Password"
        CredentialType.PASSWORD_ONLY -> "Password Only"
        CredentialType.PIN_ONLY -> "PIN Code"
        CredentialType.CUSTOM -> "Custom Field"
    }
} 