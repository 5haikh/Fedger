package my.zaif.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
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
import my.zaif.components.EmptyStateMessage
import my.zaif.components.PersonDialog
import my.zaif.components.TransactionDialog
import my.zaif.data.entity.Person
import my.zaif.data.entity.Transaction
import my.zaif.ui.theme.Spacing
import my.zaif.viewmodel.PersonDetailViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PersonDetailScreen(
    personId: Long,
    navController: NavController
) {
    // Get the application context
    val context = LocalContext.current
    val application = context.applicationContext as ZaifApplication
    val haptic = LocalHapticFeedback.current
    
    // Initialize the ViewModel
    val viewModel: PersonDetailViewModel = viewModel(
        factory = PersonDetailViewModel.Factory(
            application.personRepository,
            application.transactionRepository
        )
    )
    
    // Load person data
    LaunchedEffect(personId) {
        viewModel.loadPerson(personId)
        viewModel.loadTransactions(personId)
    }
    
    // Collect state from ViewModel
    val person by viewModel.person.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val balance by viewModel.balance.collectAsState()
    
    // State for showing/hiding dialogs
    var showEditPersonDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showDeleteTransactionDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    
    // Format currency
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    
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
                        text = person?.name ?: "Person Details",
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
                            showEditPersonDialog = true
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                            .padding(end = Spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Person",
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
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showAddTransactionDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { 
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        modifier = Modifier.size(Spacing.iconSize)
                    )
                },
                text = { 
                    Text(
                        text = "Add Transaction",
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
            if (person == null) {
                // Show loading or error state
                EmptyStateMessage(message = "Loading person details...")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = rememberLazyListState()
                ) {
                    // Person summary card
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(durationMillis = Spacing.animationDurationMedium, easing = FastOutSlowInEasing)) +
                                   slideInVertically(animationSpec = tween(durationMillis = Spacing.animationDurationMedium, easing = FastOutSlowInEasing)) { it / 2 }
                        ) {
                            PersonSummaryCard(
                                person = person!!,
                                balance = balance,
                                currencyFormat = currencyFormat,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = Spacing.screenHorizontalPadding,
                                        vertical = Spacing.screenVerticalPadding
                                    )
                            )
                        }
                    }
                    
                    // Transactions header
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(durationMillis = Spacing.animationDurationMedium, easing = FastOutSlowInEasing)) +
                                   slideInVertically(animationSpec = tween(durationMillis = Spacing.animationDurationMedium, easing = FastOutSlowInEasing)) { it / 2 }
                        ) {
                            Text(
                                text = "Transactions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    horizontal = Spacing.screenHorizontalPadding,
                                    vertical = Spacing.contentItemSpacing
                                )
                            )
                        }
                    }
                    
                    // Transactions list or empty state
                    if (transactions.isEmpty()) {
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(durationMillis = Spacing.animationDurationMedium, easing = FastOutSlowInEasing)) +
                                       slideInVertically(animationSpec = tween(durationMillis = Spacing.animationDurationMedium, easing = FastOutSlowInEasing)) { it / 2 }
                            ) {
                                Text(
                                    text = "No transactions yet.\nTap the + button to add a transaction.",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.large)
                                )
                            }
                        }
                    } else {
                        items(
                            items = transactions,
                            key = { it.id }
                        ) { transaction ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(durationMillis = Spacing.animationDurationMedium, easing = FastOutSlowInEasing)) +
                                       slideInVertically(animationSpec = tween(durationMillis = Spacing.animationDurationMedium, easing = FastOutSlowInEasing)) { it / 2 }
                            ) {
                                TransactionItem(
                                    transaction = transaction,
                                    currencyFormat = currencyFormat,
                                    onLongClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        transactionToDelete = transaction
                                        showDeleteTransactionDialog = true
                                    },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                        
                        // Add some space at the bottom
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
            
            // Show edit person dialog when needed
            if (showEditPersonDialog && person != null) {
                PersonDialog(
                    person = person,
                    onDismiss = { showEditPersonDialog = false },
                    onSave = { name, notes ->
                        viewModel.updatePerson(person!!.copy(name = name, notes = notes))
                        showEditPersonDialog = false
                    }
                )
            }
            
            // Show add transaction dialog when needed
            if (showAddTransactionDialog && person != null) {
                TransactionDialog(
                    onDismiss = { showAddTransactionDialog = false },
                    onSave = { amount, description, date ->
                        viewModel.addTransaction(amount, description, date)
                        // Ensure balance is refreshed after adding transaction
                        viewModel.refreshBalance()
                        showAddTransactionDialog = false
                    }
                )
            }
            
            // Show delete transaction confirmation dialog
            if (showDeleteTransactionDialog && transactionToDelete != null) {
                AlertDialog(
                    onDismissRequest = { 
                        showDeleteTransactionDialog = false
                        transactionToDelete = null
                    },
                    title = { 
                        Text(
                            text = "Delete Transaction",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = { 
                        Text(
                            text = "Are you sure you want to delete this transaction? This action cannot be undone.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                transactionToDelete?.let { transaction ->
                                    viewModel.deleteTransaction(transaction)
                                    // Ensure balance is refreshed after deleting transaction
                                    viewModel.refreshBalance()
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                showDeleteTransactionDialog = false
                                transactionToDelete = null
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
                                showDeleteTransactionDialog = false
                                transactionToDelete = null
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
fun PersonSummaryCard(
    person: Person,
    balance: Double,
    currencyFormat: NumberFormat,
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
                    .size(Spacing.largeAvatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val initial = person.name.firstOrNull()?.uppercase() ?: "?"
                Text(
                    text = initial,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            // Person name
            Text(
                text = person.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Spacing.medium)
            )
            
            // Person notes
            person.notes?.let {
                if (it.isNotBlank()) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(
                            top = Spacing.small,
                            start = Spacing.medium,
                            end = Spacing.medium
                        )
                    )
                }
            }
            
            // Balance
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            val balanceColor = when {
                balance > 0 -> Color.Green.copy(alpha = 0.8f)
                balance < 0 -> Color.Red.copy(alpha = 0.8f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            
            val balanceText = when {
                balance > 0 -> "They owe you ${currencyFormat.format(balance)}"
                balance < 0 -> "You owe them ${currencyFormat.format(-balance)}"
                else -> "All settled up"
            }
            
            Text(
                text = balanceText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = balanceColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    currencyFormat: NumberFormat,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(transaction.date))
    
    val isPositive = transaction.amount > 0
    val amountColor = if (isPositive) Color.Green.copy(alpha = 0.8f) else Color.Red.copy(alpha = 0.8f)
    val amountText = currencyFormat.format(if (isPositive) transaction.amount else -transaction.amount)
    val transactionType = if (isPositive) "You gave" else "You received"
    
    ContentCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Spacing.screenHorizontalPadding, 
                vertical = Spacing.screenVerticalPadding / 2
            )
            .combinedClickable(
                onClick = { /* No action on click */ },
                onLongClick = onLongClick
            ),
        elevation = 2
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardInnerPadding)
        ) {
            // Transaction amount and type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transactionType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium,
                    color = amountColor,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Transaction description
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = Spacing.small)
            )
            
            // Transaction date
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 