package my.zaif.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import my.zaif.ZaifApplication
import my.zaif.components.ContentCard
import my.zaif.components.EmptyStateMessage
import my.zaif.components.PersonDialog
import my.zaif.components.ScreenTitle
import my.zaif.data.entity.Person
import my.zaif.navigation.NavigationRoutes
import my.zaif.ui.theme.Spacing
import my.zaif.viewmodel.LedgerViewModel
import my.zaif.viewmodel.PersonWithBalance
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(navController: NavController) {
    // Get the application context
    val context = LocalContext.current
    val application = context.applicationContext as ZaifApplication
    val haptic = LocalHapticFeedback.current
    
    // Initialize the ViewModel
    val viewModel: LedgerViewModel = viewModel(
        factory = LedgerViewModel.Factory(
            application.personRepository, 
            application.transactionRepository
        )
    )
    
    // Collect people list with balances from ViewModel
    val peopleWithBalances by viewModel.peopleWithBalances.collectAsState()
    
    // State for showing/hiding the add person dialog
    var showAddPersonDialog by remember { mutableStateOf(false) }
    
    // State for the person being edited (null if adding a new person)
    var editingPerson by remember { mutableStateOf<Person?>(null) }
    
    // State for showing delete confirmation dialog
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Person to be deleted
    var personToDelete by remember { mutableStateOf<Person?>(null) }
    
    // LazyList state for scrolling detection
    val listState = rememberLazyListState()
    
    // Currency format
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    
    Scaffold(
        topBar = {
            ScreenTitle(title = "Ledger")
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    editingPerson = null
                    showAddPersonDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { 
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Person",
                        modifier = Modifier.size(Spacing.iconSize)
                    )
                },
                text = { 
                    Text(
                        text = "Add Person",
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
            if (peopleWithBalances.isEmpty()) {
                // Show a message when there are no people
                EmptyStateMessage(
                    message = "No person added yet.\nTap the + button to add someone."
                )
            } else {
                // Show the list of people
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    items(
                        items = peopleWithBalances,
                        key = { it.person.id }
                    ) { personWithBalance ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(Spacing.animationDurationMedium)) + 
                                   slideInVertically(tween(Spacing.animationDurationMedium)) { it / 2 }
                        ) {
                            PersonItem(
                                personWithBalance = personWithBalance,
                                currencyFormat = currencyFormat,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    navController.navigate(NavigationRoutes.personDetail(personWithBalance.person.id))
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    personToDelete = personWithBalance.person
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
            if (showAddPersonDialog) {
                PersonDialog(
                    person = editingPerson,
                    onDismiss = { showAddPersonDialog = false },
                    onSave = { name, notes ->
                        if (editingPerson != null) {
                            // Update existing person
                            viewModel.updatePerson(editingPerson!!.copy(name = name, notes = notes))
                        } else {
                            // Add new person
                            viewModel.insertPerson(name, notes)
                        }
                        showAddPersonDialog = false
                    }
                )
            }
            
            // Show delete confirmation dialog
            if (showDeleteConfirmation && personToDelete != null) {
                AlertDialog(
                    onDismissRequest = { 
                        showDeleteConfirmation = false
                        personToDelete = null
                    },
                    title = { 
                        Text(
                            text = "Delete Person",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = { 
                        Text(
                            text = "Are you sure you want to delete ${personToDelete!!.name}? This action cannot be undone.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                personToDelete?.let { person ->
                                    viewModel.deletePerson(person)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                showDeleteConfirmation = false
                                personToDelete = null
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
                                personToDelete = null
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PersonItem(
    personWithBalance: PersonWithBalance,
    currencyFormat: NumberFormat,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val person = personWithBalance.person
    val balance = personWithBalance.balance
    
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
            // Avatar circle with first character
            Box(
                modifier = Modifier
                    .size(Spacing.avatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val initial = person.name.firstOrNull()?.uppercase() ?: "?"
                Text(
                    text = initial,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                )
            }
            
            // Person details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.contentPadding)
            ) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                person.notes?.let {
                    if (it.isNotBlank()) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = Spacing.extraSmall)
                        )
                    }
                }
            }
            
            // Balance display
            Spacer(modifier = Modifier.width(Spacing.contentPadding))
            
            // Show balance or "Settled" text
            if (Math.abs(balance) < 0.01) {
                // Balance is essentially zero, show "Settled"
                Text(
                    text = "Settled",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.Gray
                )
            } else {
                // Show balance with appropriate color
                val balanceColor = when {
                    balance > 0 -> Color.Green.copy(alpha = 0.8f)
                    else -> Color.Red.copy(alpha = 0.8f)
                }
                
                val balanceText = if (balance > 0) {
                    currencyFormat.format(balance)
                } else {
                    currencyFormat.format(-balance)
                }
                
                Text(
                    text = balanceText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = balanceColor
                )
            }
        }
    }
} 