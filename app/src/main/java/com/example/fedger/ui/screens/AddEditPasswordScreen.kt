package com.example.fedger.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fedger.model.Credential
import com.example.fedger.model.CredentialType
import com.example.fedger.model.PasswordEntry
import com.example.fedger.ui.PasswordViewModel
import com.example.fedger.ui.components.EnhancedCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Main screen for adding/editing password entries
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPasswordScreen(
    viewModel: PasswordViewModel,
    entryId: Int = 0,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val isEditMode = entryId != 0
    val coroutineScope = rememberCoroutineScope()
    
    // Top-level state for the screen
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var credentials by rememberSaveable { mutableStateOf(emptyList<CredentialState>()) }
    
    // Flag to track initial loading
    var isLoaded by rememberSaveable { mutableStateOf(false) }
    
    // Load existing data or initialize with defaults
    LaunchedEffect(entryId) {
        if (!isLoaded) {
            if (isEditMode) {
                viewModel.getEntryWithCredentials(entryId).collect { entryWithCredentials ->
                    entryWithCredentials?.let { entry ->
                        title = entry.entry.title
                        description = entry.entry.description
                        category = entry.entry.category
                        
                        // Convert and decrypt credentials
                        credentials = entry.credentials.map { credential ->
                            val decryptedValue = try {
                                viewModel.decryptCredentialValue(credential)
                            } catch (e: Exception) {
                                credential.value
                            }
                            
                            // Parse username/password format if needed
                            val type = try {
                                CredentialType.valueOf(credential.type)
                            } catch (e: Exception) {
                                CredentialType.CUSTOM
                            }
                            
                            if (type == CredentialType.USERNAME_PASSWORD) {
                                val parts = decryptedValue.split(":", limit = 2)
                                if (parts.size == 2) {
                                    CredentialState(
                                        type = type,
                                        id = credential.id,
                                        entryId = credential.entryId,
                                        label = credential.label,
                                        displayName = credential.displayName,
                                        value = parts[1],
                                        username = parts[0],
                                        isProtected = credential.isProtected,
                                        position = credential.position
                                    )
                                } else {
                                    CredentialState(
                                        type = type,
                                        id = credential.id,
                                        entryId = credential.entryId,
                                        label = credential.label,
                                        displayName = credential.displayName,
                                        value = decryptedValue,
                                        isProtected = credential.isProtected,
                                        position = credential.position
                                    )
                                }
                            } else {
                                CredentialState(
                                    type = type,
                                    id = credential.id,
                                    entryId = credential.entryId,
                                    label = credential.label,
                                    displayName = credential.displayName,
                                    value = decryptedValue,
                                    isProtected = credential.isProtected,
                                    position = credential.position
                                )
                            }
                        }
                    }
                }
            } else {
                // Add a default credential for new entries
                credentials = listOf(
                    CredentialState(
                        type = CredentialType.PASSWORD_ONLY,
                        label = "Password",
                        position = 0
                    )
                )
            }
            isLoaded = true
        }
    }
    
    // Check if save is enabled
    val isSaveEnabled = title.isNotBlank() && credentials.any { it.value.isNotBlank() }
    
    // UI Setup
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Password" else "Add Password",
                        color = MaterialTheme.colorScheme.onPrimary, // Changed
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Changed
                    titleContentColor = MaterialTheme.colorScheme.onPrimary, // Changed
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary, // Changed
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary // Changed for navigation icon
                ),
                navigationIcon = {
                    IconButton(onClick = onCancelClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.onPrimary // Changed
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Changed
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 80.dp) // Add padding for the save button
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Password Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground // Changed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Title field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Changed
                            cursorColor = MaterialTheme.colorScheme.primary // Changed
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Description field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Changed
                            cursorColor = MaterialTheme.colorScheme.primary // Changed
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Category field
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (Optional)", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Changed
                            cursorColor = MaterialTheme.colorScheme.primary // Changed
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Credentials section header with add button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Credentials",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground // Changed
                        )
                        
                        ElevatedButton(
                            onClick = {
                                // Add new credential to list
                                credentials = credentials + CredentialState(
                                    type = CredentialType.CUSTOM,
                                    label = "Custom",
                                    position = credentials.size
                                )
                            },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary, // Changed
                                contentColor = MaterialTheme.colorScheme.onPrimary // Changed
                            ),
                            elevation = ButtonDefaults.elevatedButtonElevation(
                                defaultElevation = 2.dp
                            ),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Credential",
                                tint = MaterialTheme.colorScheme.onPrimary // Changed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Field",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // List of credential items
                itemsIndexed(
                    items = credentials,
                    key = { _, credential -> credential.uniqueId }
                ) { index, credential ->
                    CredentialItemNew(
                        state = credential,
                        totalCredentials = credentials.size,
                        onUpdate = { updatedState ->
                            credentials = credentials.toMutableList().apply {
                                this[index] = updatedState
                            }
                        },
                        onDelete = {
                            if (credentials.size > 1) {
                                credentials = credentials.toMutableList().apply {
                                    removeAt(index)
                                }
                            }
                        },
                        viewModel = viewModel
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
            
            // Save button at the bottom of the screen
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background) // Changed
                    .padding(16.dp)
            ) {
                ElevatedButton(
                    onClick = {
                        val entry = if (isEditMode) {
                            PasswordEntry(
                                id = entryId,
                                title = title,
                                description = description,
                                category = category,
                                updatedAt = System.currentTimeMillis()
                            )
                        } else {
                            PasswordEntry(
                                title = title,
                                description = description,
                                category = category
                            )
                        }
                        
                        // Convert CredentialState objects to Credential objects for database
                        val updatedCredentials = credentials.mapIndexed { index, credentialState ->
                            // Combine username and value for USERNAME_PASSWORD type
                            val finalValue = if (credentialState.type == CredentialType.USERNAME_PASSWORD) {
                                "${credentialState.username}:${credentialState.value}"
                            } else {
                                credentialState.value
                            }
                            
                            Credential(
                                id = credentialState.id,
                                entryId = entryId,
                                label = credentialState.label,
                                displayName = credentialState.displayName,
                                value = finalValue,
                                isProtected = credentialState.isProtected,
                                type = credentialState.type.name,
                                position = index
                            )
                        }
                        
                        viewModel.savePasswordEntryWithCredentials(entry, updatedCredentials)
                        onSaveClick()
                    },
                    enabled = isSaveEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // Changed
                            shape = MaterialTheme.shapes.medium
                        ),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Changed
                        contentColor = MaterialTheme.colorScheme.onPrimary, // Changed
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), // Changed
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) // Changed
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 4.dp
                    ),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.onPrimary // Changed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Password",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// Data class to represent the state of a credential item in the UI
data class CredentialState(
    val type: CredentialType = CredentialType.CUSTOM,
    val id: Int = 0,
    val entryId: Int = 0,
    val label: String = "",
    val displayName: String = "",
    val value: String = "",
    val username: String = "",  // Only used for USERNAME_PASSWORD type
    val isProtected: Boolean = true,
    val position: Int = 0,
    val uniqueId: String = java.util.UUID.randomUUID().toString() // Unique ID for Compose key
)

// Updated credential item component with improved state management
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CredentialItemNew(
    state: CredentialState,
    totalCredentials: Int,
    onUpdate: (CredentialState) -> Unit,
    onDelete: () -> Unit,
    viewModel: PasswordViewModel
) {
    // Local state to handle current edits only
    var localState by remember(state.uniqueId) { mutableStateOf(state) }
    
    // UI state
    var showPassword by remember { mutableStateOf(false) }
    var showGenerator by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    
    // Focus state
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Create dropdown items from credential types
    val credentialTypes = CredentialType.values().map { it.displayName }
    
    // Track if any input field has focus (when implementing focus state)
    var anyFieldHasFocus by remember { mutableStateOf(false) }
    
    // Update parent directly without delay - we'll use onFocusChanged instead
    fun updateParent() {
        onUpdate(localState)
    }
    
    // When any input field changes value
    fun updateLocalAndParent(newState: CredentialState) {
        localState = newState
        // Update parent immediately - no delay needed
        onUpdate(newState)
    }
    
    // Synchronize with parent when component key changes
    LaunchedEffect(state.uniqueId) {
        localState = state
    }
    
    EnhancedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Type selection dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = localState.type.displayName,
                    onValueChange = {}, // Read-only field
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Field Type", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Changed
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                    ),
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select Type",
                            modifier = Modifier.clickable { typeMenuExpanded = true },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                        )
                    }
                )
                
                // Invisible clickable overlay to open dropdown
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { typeMenuExpanded = true }
                )
                
                DropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)) // Changed
                ) {
                    credentialTypes.forEach { typeName ->
                        DropdownMenuItem(
                            onClick = {
                                val newType = CredentialType.fromDisplayName(typeName)
                                
                                // Handle type change, reset fields if needed
                                val updatedState = if (newType == CredentialType.USERNAME_PASSWORD &&
                                    localState.type != CredentialType.USERNAME_PASSWORD
                                ) {
                                    // Reset fields when changing to username/password
                                    localState.copy(
                                        type = newType,
                                        username = "",
                                        value = ""
                                    )
                                } else {
                                    localState.copy(type = newType)
                                }
                                
                                // Update both local and parent state
                                updateLocalAndParent(updatedState)
                                typeMenuExpanded = false
                            },
                            text = { Text(typeName, color = MaterialTheme.colorScheme.onSurface) } // Changed
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Custom display name field
            OutlinedTextField(
                value = localState.displayName,
                onValueChange = {
                    updateLocalAndParent(localState.copy(displayName = it))
                },
                label = { Text("Display Name (optional)", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        anyFieldHasFocus = focusState.isFocused
                    },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                    focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Changed
                    cursorColor = MaterialTheme.colorScheme.primary // Changed
                ),
                placeholder = { Text("Custom name for this credential field", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) } // Changed
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Render fields according to selected type
            when (localState.type) {
                CredentialType.USERNAME_PASSWORD -> {
                    // Username field
                    OutlinedTextField(
                        value = localState.username,
                        onValueChange = {
                            updateLocalAndParent(localState.copy(username = it))
                        },
                        label = { Text("Username", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Changed
                            cursorColor = MaterialTheme.colorScheme.primary // Changed
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Password field
                    OutlinedTextField(
                        value = localState.value,
                        onValueChange = {
                            updateLocalAndParent(localState.copy(value = it))
                        },
                        label = { Text("Password", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (!localState.isProtected || showPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Changed
                            cursorColor = MaterialTheme.colorScheme.primary // Changed
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword)
                                        "Hide Password"
                                    else
                                        "Show Password",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                                )
                            }
                        }
                    )
                }
                CredentialType.PIN -> {
                    // PIN field
                    OutlinedTextField(
                        value = localState.value,
                        onValueChange = {
                            // Only allow digits for PIN
                            if (it.all { char -> char.isDigit() }) {
                                updateLocalAndParent(localState.copy(value = it))
                            }
                        },
                        label = { Text("PIN", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (!localState.isProtected || showPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Changed
                            cursorColor = MaterialTheme.colorScheme.primary // Changed
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword)
                                        "Hide PIN"
                                    else
                                        "Show PIN",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                                )
                            }
                        }
                    )
                }
                CredentialType.PASSWORD_ONLY -> {
                    // Password only field
                    OutlinedTextField(
                        value = localState.value,
                        onValueChange = {
                            updateLocalAndParent(localState.copy(value = it))
                        },
                        label = { Text("Password", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (!localState.isProtected || showPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Changed
                            cursorColor = MaterialTheme.colorScheme.primary // Changed
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword)
                                        "Hide Password"
                                    else
                                        "Show Password",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                                )
                            }
                        }
                    )
                }
                else -> { // Custom
                    // Label field
                    OutlinedTextField(
                        value = localState.label,
                        onValueChange = {
                            updateLocalAndParent(localState.copy(label = it))
                        },
                        label = { Text("Label", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Changed
                            cursorColor = MaterialTheme.colorScheme.primary // Changed
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Value field
                    OutlinedTextField(
                        value = localState.value,
                        onValueChange = {
                            updateLocalAndParent(localState.copy(value = it))
                        },
                        label = { Text("Value", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (!localState.isProtected || showPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                            focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Changed
                            cursorColor = MaterialTheme.colorScheme.primary // Changed
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword)
                                        "Hide Value"
                                    else
                                        "Show Value",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                                )
                            }
                        }
                    )
                }
            }
            
            // Bottom row with mask checkbox and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = localState.isProtected,
                        onCheckedChange = {
                            updateLocalAndParent(localState.copy(isProtected = it))
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary, // Changed
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant, // Changed
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary // Changed
                        )
                    )
                    Text("Mask value", color = MaterialTheme.colorScheme.onSurface) // Changed
                }
                
                if (totalCredentials > 1) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Credential",
                            tint = MaterialTheme.colorScheme.error // Changed
                        )
                    }
                }
            }
            
            // Password generator button - only show for password fields
            if (localState.type == CredentialType.USERNAME_PASSWORD ||
                localState.type == CredentialType.PASSWORD_ONLY) {
                ElevatedButton(
                    onClick = { showGenerator = !showGenerator },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f), // Changed
                        contentColor = MaterialTheme.colorScheme.onSecondary // Changed
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 2.dp
                    ),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Password,
                        contentDescription = "Generate Password",
                        tint = MaterialTheme.colorScheme.onSecondary // Changed
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Generate Password",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (showGenerator) {
                    PasswordGeneratorDialog(
                        viewModel = viewModel,
                        onDismiss = { showGenerator = false },
                        onPasswordGenerated = { generatedPassword ->
                            updateLocalAndParent(localState.copy(value = generatedPassword))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isProtected: Boolean,
    showPassword: Boolean,
    onToggleShowPassword: (Boolean) -> Unit,
    label: String,
    onFocusChange: (Boolean) -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                onFocusChange(!it.isFocused)
            },
        singleLine = true,
        visualTransformation = if (!isProtected || showPassword)
            VisualTransformation.None
        else
            PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
            focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Changed
            cursorColor = MaterialTheme.colorScheme.primary // Changed
        ),
        trailingIcon = {
            IconButton(onClick = { onToggleShowPassword(!showPassword) }) {
                Icon(
                    imageVector = if (showPassword)
                        Icons.Default.Visibility
                    else
                        Icons.Default.VisibilityOff,
                    contentDescription = if (showPassword)
                        "Hide Password"
                    else
                        "Show Password",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                )
            }
        }
    )
}

// Password generator dialog
@Composable
fun PasswordGeneratorDialog(
    viewModel: PasswordViewModel,
    onDismiss: () -> Unit,
    onPasswordGenerated: (String) -> Unit
) {
    val passwordLength by viewModel.passwordLength.collectAsStateWithLifecycle()
    val useUppercase by viewModel.useUppercase.collectAsStateWithLifecycle()
    val useLowercase by viewModel.useLowercase.collectAsStateWithLifecycle()
    val useNumbers by viewModel.useNumbers.collectAsStateWithLifecycle()
    val useSpecialChars by viewModel.useSpecialChars.collectAsStateWithLifecycle()
    val generatedPassword by viewModel.generatedPassword.collectAsStateWithLifecycle()
    
    // Generate a password when the dialog is opened
    LaunchedEffect(Unit) {
        viewModel.generatePassword()
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp), // Changed
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary), // Changed
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Password,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary, // Changed
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Password Generator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface // Changed
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Generated Password section
                Text(
                    text = "Generated Password",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary // Changed
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Password display with refresh button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) // Changed
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), // Changed
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = generatedPassword,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface // Changed
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Refresh button
                    IconButton(
                        onClick = { viewModel.generatePassword() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary) // Changed
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Generate New Password",
                            tint = MaterialTheme.colorScheme.onPrimary // Changed
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Password length section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LinearScale,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, // Changed
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Password Length: $passwordLength",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface // Changed
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Min/max values and slider
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "8",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Changed
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    
                    Text(
                        text = "32",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Changed
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
                
                Slider(
                    value = passwordLength.toFloat(),
                    onValueChange = { viewModel.setPasswordLength(it.toInt()) },
                    valueRange = 8f..32f,
                    steps = 23,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary, // Changed
                        activeTrackColor = MaterialTheme.colorScheme.primary, // Changed
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) // Changed
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Character types section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, // Changed
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Character Types",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface // Changed
                    )
                }
                
                // Character type options
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) // Changed
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Uppercase
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = useUppercase,
                                onCheckedChange = { viewModel.setUseUppercase(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary, // Changed
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                                )
                            )
                            
                            Text(
                                "Uppercase (A-Z)",
                                color = MaterialTheme.colorScheme.onSurface, // Changed
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                "ABCDEF",
                                color = MaterialTheme.colorScheme.primary, // Changed
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        // Lowercase
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = useLowercase,
                                onCheckedChange = { viewModel.setUseLowercase(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary, // Changed
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                                )
                            )
                            
                            Text(
                                "Lowercase (a-z)",
                                color = MaterialTheme.colorScheme.onSurface, // Changed
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                "abcdef",
                                color = MaterialTheme.colorScheme.primary, // Changed
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        // Numbers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = useNumbers,
                                onCheckedChange = { viewModel.setUseNumbers(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary, // Changed
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                                )
                            )
                            
                            Text(
                                "Numbers (0-9)",
                                color = MaterialTheme.colorScheme.onSurface, // Changed
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                "012345",
                                color = MaterialTheme.colorScheme.primary, // Changed
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        // Special characters
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = useSpecialChars,
                                onCheckedChange = { viewModel.setUseSpecialChars(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary, // Changed
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                                )
                            )
                            
                            Text(
                                "Special characters",
                                color = MaterialTheme.colorScheme.onSurface, // Changed
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                "!@#$%&",
                                color = MaterialTheme.colorScheme.primary, // Changed
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface // Changed
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) // Changed
                    ) {
                        Text("Cancel")
                    }
                    
                    // Use button with checkmark icon
                    Button(
                        onClick = {
                            onPasswordGenerated(generatedPassword)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary // Changed
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary // Changed
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Use")
                    }
                }
            }
        }
    }
}