package my.zaif.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import my.zaif.ZaifApplication
import my.zaif.navigation.NavigationRoutes
import my.zaif.components.ContentCard
import my.zaif.components.EmptyStateMessage
import my.zaif.components.EntityDialog
import my.zaif.components.EntityItem
import my.zaif.components.ScreenTitle
import my.zaif.data.entity.Entity
import my.zaif.ui.theme.Spacing
import my.zaif.viewmodel.CredentialsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CredentialsScreen(navController: NavController) {
    // Get the application context
    val context = LocalContext.current
    val application = context.applicationContext as ZaifApplication
    val haptic = LocalHapticFeedback.current
    
    // Initialize the ViewModel
    val viewModel: CredentialsViewModel = viewModel(
        factory = CredentialsViewModel.Factory(
            application.entityRepository
        )
    )
    
    // Collect entities list from ViewModel
    val entities by viewModel.allEntities.collectAsState()
    
    // State for showing/hiding the add entity dialog
    var showAddEntityDialog by remember { mutableStateOf(false) }
    
    // State for the entity being edited (null if adding a new entity)
    var editingEntity by remember { mutableStateOf<Entity?>(null) }
    
    // State for showing delete confirmation dialog
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Entity to be deleted
    var entityToDelete by remember { mutableStateOf<Entity?>(null) }
    
    // LazyList state for scrolling detection
    val listState = rememberLazyListState()
    
    Scaffold(
        topBar = {
            ScreenTitle(title = "Credentials")
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    editingEntity = null
                    showAddEntityDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { 
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Entity",
                        modifier = Modifier.size(Spacing.iconSize)
                    )
                },
                text = { 
                    Text(
                        text = "Add Entity",
                        fontWeight = FontWeight.Medium
                    )
                },
                expanded = true,
                modifier = Modifier.shadow(
                    elevation = Spacing.elevationMedium,
                    shape = RoundedCornerShape(Spacing.large),
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
            if (entities.isEmpty()) {
                // Show a message when there are no entities
                EmptyStateMessage(
                    message = "No entities added yet.\nTap the + button to add an entity."
                )
            } else {
                // Show the list of entities
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    items(
                        items = entities,
                        key = { it.id }
                    ) { entity ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(durationMillis = Spacing.animationDurationMedium, easing = FastOutSlowInEasing)) +
                                   slideInVertically(animationSpec = tween(durationMillis = Spacing.animationDurationMedium, easing = FastOutSlowInEasing)) { it / 2 }
                        ) {
                            EntityItem(
                                entity = entity,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    navController.navigate(NavigationRoutes.entityDetail(entity.id))
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    entityToDelete = entity
                                    showDeleteConfirmation = true
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                    
                    // Add some space at the bottom
                    item {
                        Spacer(modifier = Modifier.padding(bottom = 80.dp))
                    }
                }
            }
            
            // Show the add/edit dialog when needed
            if (showAddEntityDialog) {
                EntityDialog(
                    entity = editingEntity,
                    onDismiss = { showAddEntityDialog = false },
                    onSave = { entityName ->
                        if (editingEntity != null) {
                            // Update existing entity
                            viewModel.updateEntity(editingEntity!!.copy(entityName = entityName))
                        } else {
                            // Create new entity
                            viewModel.insertEntity(entityName)
                        }
                        showAddEntityDialog = false
                    }
                )
            }
            
            // Show delete confirmation dialog when needed
            if (showDeleteConfirmation && entityToDelete != null) {
                AlertDialog(
                    onDismissRequest = { 
                        showDeleteConfirmation = false
                        entityToDelete = null
                    },
                    title = { 
                        Text(
                            text = "Delete Entity",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = { 
                        Text(
                            text = "Are you sure you want to delete '${entityToDelete!!.entityName}'? This will also delete all associated credentials.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                entityToDelete?.let { entity ->
                                    viewModel.deleteEntity(entity)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                showDeleteConfirmation = false
                                entityToDelete = null
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
                                entityToDelete = null
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