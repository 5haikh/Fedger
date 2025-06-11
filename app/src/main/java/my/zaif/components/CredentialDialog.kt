package my.zaif.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import my.zaif.data.entity.CredentialType
import my.zaif.data.entity.StoredCredential
import my.zaif.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialDialog(
    credential: StoredCredential? = null,
    onDismiss: () -> Unit,
    onSave: (
        credentialLabel: String,
        credentialType: CredentialType,
        username: String?,
        passwordValue: String?,
        customFieldKey: String?
    ) -> Unit
) {
    var credentialLabel by remember { mutableStateOf(credential?.credentialLabel ?: "") }
    var credentialType by remember { mutableStateOf(credential?.credentialType ?: CredentialType.USERNAME_PASSWORD) }
    var username by remember { mutableStateOf(credential?.username ?: "") }
    var passwordValue by remember { mutableStateOf(credential?.passwordValue ?: "") }
    var customFieldKey by remember { mutableStateOf(credential?.customFieldKey ?: "") }
    
    var isLabelError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }
    var isUsernameError by remember { mutableStateOf(false) }
    var isCustomKeyError by remember { mutableStateOf(false) }
    
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        // Request focus on the label field when dialog appears
        focusRequester.requestFocus()
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) + 
                   scaleIn(spring(stiffness = Spring.StiffnessLow), initialScale = 0.95f)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(Spacing.dialogWidth)
                    .padding(
                        horizontal = Spacing.screenHorizontalPadding, 
                        vertical = Spacing.screenVerticalPadding
                    )
                    .shadow(
                        elevation = Spacing.dialogElevation,
                        shape = RoundedCornerShape(Spacing.dialogCornerRadius),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                shape = RoundedCornerShape(Spacing.dialogCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = Spacing.dialogElevation
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(Spacing.contentGroupSpacing)
                ) {
                    Text(
                        text = if (credential == null) "Add New Credential" else "Edit Credential",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Credential Label
                    OutlinedTextField(
                        value = credentialLabel,
                        onValueChange = { 
                            credentialLabel = it
                            isLabelError = it.isBlank()
                        },
                        label = { Text("Credential Label") },
                        placeholder = { Text("Enter a label for this credential") },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Label, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        isError = isLabelError,
                        supportingText = {
                            if (isLabelError) {
                                Text(
                                    text = "Label cannot be empty",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        shape = RoundedCornerShape(Spacing.cardCornerRadius),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    // Credential Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = getCredentialTypeText(credentialType),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Credential Type") },
                            leadingIcon = { 
                                Icon(
                                    getCredentialTypeIcon(credentialType), 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                ) 
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(Spacing.cardCornerRadius),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CredentialType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(getCredentialTypeText(type)) },
                                    leadingIcon = { 
                                        Icon(
                                            getCredentialTypeIcon(type), 
                                            contentDescription = null
                                        ) 
                                    },
                                    onClick = {
                                        credentialType = type
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Dynamic fields based on credential type
                    when (credentialType) {
                        CredentialType.USERNAME_PASSWORD -> {
                            // Username field
                            OutlinedTextField(
                                value = username,
                                onValueChange = { 
                                    username = it
                                    isUsernameError = it.isBlank()
                                },
                                label = { Text("Username") },
                                placeholder = { Text("Enter username") },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Person, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    ) 
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = isUsernameError,
                                supportingText = {
                                    if (isUsernameError) {
                                        Text(
                                            text = "Username cannot be empty",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(Spacing.cardCornerRadius),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            
                            // Password field
                            OutlinedTextField(
                                value = passwordValue,
                                onValueChange = { 
                                    passwordValue = it
                                    isPasswordError = it.isBlank()
                                },
                                label = { Text("Password") },
                                placeholder = { Text("Enter password") },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Password, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    ) 
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = isPasswordError,
                                supportingText = {
                                    if (isPasswordError) {
                                        Text(
                                            text = "Password cannot be empty",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(Spacing.cardCornerRadius),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        CredentialType.PASSWORD_ONLY -> {
                            // Password field
                            OutlinedTextField(
                                value = passwordValue,
                                onValueChange = { 
                                    passwordValue = it
                                    isPasswordError = it.isBlank()
                                },
                                label = { Text("Password") },
                                placeholder = { Text("Enter password") },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Key, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    ) 
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = isPasswordError,
                                supportingText = {
                                    if (isPasswordError) {
                                        Text(
                                            text = "Password cannot be empty",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(Spacing.cardCornerRadius),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        CredentialType.PIN_ONLY -> {
                            // PIN field
                            OutlinedTextField(
                                value = passwordValue,
                                onValueChange = { 
                                    // Only allow numeric input
                                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                                        passwordValue = it
                                    }
                                    isPasswordError = it.isBlank()
                                },
                                label = { Text("PIN") },
                                placeholder = { Text("Enter PIN code") },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Pin, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    ) 
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) "Hide PIN" else "Show PIN",
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                isError = isPasswordError,
                                supportingText = {
                                    if (isPasswordError) {
                                        Text(
                                            text = "PIN cannot be empty",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(Spacing.cardCornerRadius),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        CredentialType.CUSTOM -> {
                            // Custom field key
                            OutlinedTextField(
                                value = customFieldKey,
                                onValueChange = { 
                                    customFieldKey = it
                                    isCustomKeyError = it.isBlank()
                                },
                                label = { Text("Field Name") },
                                placeholder = { Text("Enter field name (e.g., API Key)") },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Label, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    ) 
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = isCustomKeyError,
                                supportingText = {
                                    if (isCustomKeyError) {
                                        Text(
                                            text = "Field name cannot be empty",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(Spacing.cardCornerRadius),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            
                            // Custom field value
                            OutlinedTextField(
                                value = passwordValue,
                                onValueChange = { 
                                    passwordValue = it
                                    isPasswordError = it.isBlank()
                                },
                                label = { Text("Field Value") },
                                placeholder = { Text("Enter field value") },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.TextFields, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    ) 
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) "Hide value" else "Show value",
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = isPasswordError,
                                supportingText = {
                                    if (isPasswordError) {
                                        Text(
                                            text = "Field value cannot be empty",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(Spacing.cardCornerRadius),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(Spacing.small))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(Spacing.cardCornerRadius)
                        ) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Button(
                            onClick = { 
                                // Validate fields based on credential type
                                when (credentialType) {
                                    CredentialType.USERNAME_PASSWORD -> {
                                        isLabelError = credentialLabel.isBlank()
                                        isUsernameError = username.isBlank()
                                        isPasswordError = passwordValue.isBlank()
                                        
                                        if (!isLabelError && !isUsernameError && !isPasswordError) {
                                            onSave(credentialLabel, credentialType, username, passwordValue, null)
                                        }
                                    }
                                    CredentialType.PASSWORD_ONLY -> {
                                        isLabelError = credentialLabel.isBlank()
                                        isPasswordError = passwordValue.isBlank()
                                        
                                        if (!isLabelError && !isPasswordError) {
                                            onSave(credentialLabel, credentialType, null, passwordValue, null)
                                        }
                                    }
                                    CredentialType.PIN_ONLY -> {
                                        isLabelError = credentialLabel.isBlank()
                                        isPasswordError = passwordValue.isBlank()
                                        
                                        if (!isLabelError && !isPasswordError) {
                                            onSave(credentialLabel, credentialType, null, passwordValue, null)
                                        }
                                    }
                                    CredentialType.CUSTOM -> {
                                        isLabelError = credentialLabel.isBlank()
                                        isCustomKeyError = customFieldKey.isBlank()
                                        isPasswordError = passwordValue.isBlank()
                                        
                                        if (!isLabelError && !isCustomKeyError && !isPasswordError) {
                                            onSave(credentialLabel, credentialType, null, passwordValue, customFieldKey)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(Spacing.cardCornerRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = Spacing.cardElevation,
                                pressedElevation = 1.dp
                            )
                        ) {
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
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

@Composable
private fun getCredentialTypeIcon(credentialType: CredentialType) = when (credentialType) {
    CredentialType.USERNAME_PASSWORD -> Icons.Default.Password
    CredentialType.PASSWORD_ONLY -> Icons.Default.Key
    CredentialType.PIN_ONLY -> Icons.Default.Pin
    CredentialType.CUSTOM -> Icons.Default.TextFields
} 