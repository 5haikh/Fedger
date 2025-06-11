package my.zaif.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import my.zaif.ZaifApplication
import my.zaif.components.ContentCard
import my.zaif.components.CredentialDialog
import my.zaif.components.EmptyStateMessage
import my.zaif.components.SectionTitle
import my.zaif.components.StoredCredentialItem
import my.zaif.data.entity.Entity
import my.zaif.data.entity.StoredCredential
import my.zaif.ui.theme.Spacing
import my.zaif.viewmodel.EntityDetailViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EntityDetailScreen(entityId: Long, navController: NavController) {
    // Get the application context
    val context = LocalContext.current
    val application = context.applicationContext as ZaifApplication
    val haptic = LocalHapticFeedback.current
    
    // Initialize the ViewModel
    val viewModel: EntityDetailViewModel = viewModel(
        factory = EntityDetailViewModel.Factory(
            entityId,
            application.entityRepository,
            application.storedCredentialRepository
        )
    )
    
    // Collect entity and credentials from ViewModel
    val entity by viewModel.entity.collectAsState()
    val credentials by viewModel.credentials.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // State for entity name editing
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    
    // State for showing/hiding the add credential dialog
    var showAddCredentialDialog by remember { mutableStateOf(false) }
    
    // State for the credential being edited (null if adding a new credential)
    var editingCredential by remember { mutableStateOf<StoredCredential?>(null) }
    
    // State for showing delete confirmation dialog
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Credential to be deleted
    var credentialToDelete by remember { mutableStateOf<StoredCredential?>(null) }
    
    // LazyList state for scrolling detection
    val listState = rememberLazyListState()
    
    // Update edited name when entity changes
    if (entity != null && editedName.isEmpty()) {
        editedName = entity!!.entityName
    }
    
    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Back button
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.align(Alignment.CenterStart)
                            .padding(start = Spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Title
                    Text(
                        text = entity?.entityName ?: "Entity Details",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.medium)
                    )
                    
                    // Edit button
                    IconButton(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isEditingName = true 
                            editedName = entity?.entityName ?: ""
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                            .padding(end = Spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Entity",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    editingCredential = null
                    showAddCredentialDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { 
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Credential",
                        modifier = Modifier.size(Spacing.iconSize)
                    )
                },
                text = { 
                    Text(
                        text = "Add Credential",
                        fontWeight = FontWeight.Medium
                    )
                },
                expanded = true,
                modifier = Modifier.shadow(
                    elevation = Spacing.elevationMedium,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (entity == null) {
                // Show loading or error state
                if (error != null) {
                    EmptyStateMessage(message = error ?: "Unknown error")
                } else {
                    EmptyStateMessage(message = "Loading entity details...")
                }
            } else {
                // Show entity details and credentials
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    // Entity summary card
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(Spacing.animationDurationMedium)) + 
                                   slideInVertically(tween(Spacing.animationDurationMedium)) { it / 2 }
                        ) {
                            EntitySummaryCard(
                                entity = entity!!,
                                credentialCount = credentials.size,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = Spacing.screenHorizontalPadding,
                                        vertical = Spacing.screenVerticalPadding
                                    )
                            )
                        }
                    }
                    
                    // Entity name editing section (only shown when editing)
                    if (isEditingName) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = Spacing.screenHorizontalPadding,
                                        vertical = Spacing.screenVerticalPadding
                                    ),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = Spacing.cardElevation
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.medium)
                                ) {
                                    Text(
                                        text = "Entity Name",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = Spacing.small)
                                    )
                                    
                                    OutlinedTextField(
                                        value = editedName,
                                        onValueChange = { editedName = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                            cursorColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = Spacing.small),
                                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = { 
                                                isEditingName = false 
                                                entity?.let { currentEntity ->
                                                    editedName = currentEntity.entityName
                                                }
                                            }
                                        ) {
                                            Text("Cancel")
                                        }
                                        
                                        TextButton(
                                            onClick = { 
                                                if (editedName.isNotBlank()) {
                                                    viewModel.updateEntityName(editedName)
                                                    isEditingName = false
                                                }
                                            }
                                        ) {
                                            Text("Save")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Credentials section header
                    item {
                        SectionTitle(title = "Credentials")
                    }
                    
                    // Show credentials or empty state
                    if (credentials.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.extraLarge),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No credentials added yet.\nTap the + button to add a credential.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(
                            items = credentials,
                            key = { it.id }
                        ) { credential ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(Spacing.animationDurationMedium)) + 
                                       slideInVertically(tween(Spacing.animationDurationMedium)) { it / 2 }
                            ) {
                                StoredCredentialItem(
                                    credential = credential,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        editingCredential = credential
                                        showAddCredentialDialog = true
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        credentialToDelete = credential
                                        showDeleteConfirmation = true
                                    },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                    
                    // Add some space at the bottom
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
            
            // Show the add/edit credential dialog when needed
            if (showAddCredentialDialog) {
                CredentialDialog(
                    credential = editingCredential,
                    onDismiss = { showAddCredentialDialog = false },
                    onSave = { credentialLabel, credentialType, username, passwordValue, customFieldKey ->
                        if (editingCredential != null) {
                            // Update existing credential
                            val updatedCredential = editingCredential!!.copy(
                                credentialLabel = credentialLabel,
                                credentialType = credentialType,
                                username = username,
                                passwordValue = passwordValue,
                                customFieldKey = customFieldKey
                            )
                            viewModel.updateCredential(updatedCredential)
                        } else {
                            // Add new credential
                            viewModel.addCredential(
                                credentialLabel = credentialLabel,
                                credentialType = credentialType,
                                username = username,
                                passwordValue = passwordValue,
                                customFieldKey = customFieldKey
                            )
                        }
                        showAddCredentialDialog = false
                    }
                )
            }
            
            // Show delete credential confirmation dialog when needed
            if (showDeleteConfirmation && credentialToDelete != null) {
                AlertDialog(
                    onDismissRequest = { 
                        showDeleteConfirmation = false
                        credentialToDelete = null
                    },
                    title = { 
                        Text(
                            text = "Delete Credential",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = { 
                        Text(
                            text = "Are you sure you want to delete '${credentialToDelete!!.credentialLabel}'? This action cannot be undone.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                credentialToDelete?.let { credential ->
                                    viewModel.deleteCredential(credential)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                showDeleteConfirmation = false
                                credentialToDelete = null
                            }
                        ) {
                            Text(
                                text = "Delete",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showDeleteConfirmation = false
                                credentialToDelete = null
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EntitySummaryCard(
    entity: Entity,
    credentialCount: Int,
    modifier: Modifier = Modifier
) {
    ContentCard(
        modifier = modifier,
        elevation = 4
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardInnerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar circle with first character
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val initial = entity.entityName.firstOrNull()?.uppercase() ?: "?"
                Text(
                    text = initial,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            // Entity name
            Text(
                text = entity.entityName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Spacing.medium)
            )
            
            // Credential count
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            val credentialText = when (credentialCount) {
                0 -> "No credentials stored"
                1 -> "1 credential stored"
                else -> "$credentialCount credentials stored"
            }
            
            Text(
                text = credentialText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
} 