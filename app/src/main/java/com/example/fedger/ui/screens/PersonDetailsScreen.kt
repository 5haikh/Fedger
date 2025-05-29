package com.example.fedger.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.pullrefresh.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.fedger.model.Person
import com.example.fedger.model.Transaction
import com.example.fedger.ui.PersonViewModel
import com.example.fedger.ui.components.*
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.draw.scale
import androidx.navigation.NavController
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissValue
import androidx.compose.material.DismissDirection
import androidx.compose.material.rememberDismissState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun PersonDetailsScreen(
    personId: Int,
    viewModel: PersonViewModel,
    onBackClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    navController: NavController
) {
    val personState by viewModel.getPersonById(personId).collectAsState(initial = null)
    val person = personState ?: return
    
    // Initialize transactions after the screen is displayed, with a slight delay to ensure smooth transition
    LaunchedEffect(personId) {
        // Set the current person ID immediately to start data fetching
        viewModel.loadInitialTransactions(personId)
    }
    
    // Use paginated transactions instead of all transactions
    val pagedTransactions by viewModel.pagedTransactions.collectAsState()
    val isLoadingTransactions by viewModel.isLoadingTransactions.collectAsState()
    val hasMoreTransactions by viewModel.hasMoreTransactions.collectAsState()
    val totalTransactionCount by viewModel.totalTransactionCount.collectAsState()
    val lazyListState = rememberLazyListState()
    
    // Pull-to-refresh state for transactions
    var refreshingTransactions by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshingTransactions,
        onRefresh = {
            if (!refreshingTransactions && !isLoadingTransactions) {
                refreshingTransactions = true
                viewModel.refreshTransactions()
            }
        }
    )
    
    // Set refreshing to false when loading is complete
    LaunchedEffect(isLoadingTransactions) {
        if (!isLoadingTransactions) {
            refreshingTransactions = false
        }
    }
    
    // Detect when we're near the end of the list to trigger loading more transactions
    LaunchedEffect(lazyListState) {
        snapshotFlow { 
            val layoutInfo = lazyListState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
            val lastIndex = lastVisibleItem?.index ?: 0
            val totalItemsCount = layoutInfo.totalItemsCount
            val isAtEnd = lastIndex >= totalItemsCount - 3 && !lazyListState.isScrollInProgress
            
            Triple(lastIndex, totalItemsCount, isAtEnd)
        }.collect { (lastIndex, total, isAtEnd) ->
            if (isAtEnd && hasMoreTransactions && !isLoadingTransactions && total > 0 && lastIndex > 0) {
                viewModel.loadNextTransactionPage()
            }
        }
    }
    
    // Add a state to track balance updates
    var balanceVersion by remember { mutableStateOf(0) }
    
    // Also observe the balances map from the ViewModel
    val balances by viewModel.balances.collectAsState()
    
    // Get current balance with priority to the live balances map
    val currentBalance = balances[personId] ?: remember(personId, balanceVersion) { 
        viewModel.getCurrentBalanceForPerson(personId) 
    }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    
    // Consolidate multiple refresh triggers into a single LaunchedEffect
    // This reduces database calls and prevents overlapping refreshes
    LaunchedEffect(personId, totalTransactionCount, balances[personId]) {
        // Only refresh if we need to (not already loading and we have an actual change)
        if (!isLoadingTransactions) {
            Log.d("PersonDetailsScreen", "Refreshing transactions due to changes in counts or balances")
            viewModel.refreshTransactions()
        }
    }
    
    // Add this extra LaunchedEffect for getting verified balance:
    LaunchedEffect(personId) {
        // Use our new method to get the person with guaranteed accurate balance
        viewModel.getPersonWithLiveBalance(personId).collect { updatedPerson ->
            // The repository already updated the actual database - no need to do anything here
            // This collect just ensures we're subscribed to any balance recalculations
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    spotColor = MaterialTheme.colorScheme.secondary // Added spotColor for consistency
                                )
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary, // Changed
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f) // Changed
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "₹",
                                color = MaterialTheme.colorScheme.onPrimary, // Changed
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Fedger",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary, // Changed
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate back to contacts list"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary // Changed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Changed
                    titleContentColor = MaterialTheme.colorScheme.onPrimary, // Changed
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary // Changed
                ),
                actions = {
                    // App Switcher removed as requested
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background) // Added background
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Person Details",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground, // Changed
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
                
                // Person Info Card
                PersonInfoCard(
                    person = person,
                    balance = currentBalance,
                    onAddTransactionClick = onAddTransactionClick
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground, // Changed
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = if (totalTransactionCount > 0)
                            "${totalTransactionCount} ${if (totalTransactionCount == 1) "transaction" else "transactions"}"
                        else "",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), // Changed
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Use ContentStateHandler to manage the different states
                ContentStateHandler(
                    isLoading = isLoadingTransactions && pagedTransactions.isEmpty(),
                    items = pagedTransactions,
                    emptyContent = {
                        EmptyTransactionsState(onAddClick = onAddTransactionClick)
                    },
                    loadingContent = {
                        LoadingState(message = "Loading transactions...")
                    }
                ) {
                    LazyColumn(
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = pagedTransactions,
                            key = { transaction -> transaction.id }
                        ) { transaction ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                TransactionItem(
                                    transaction = transaction,
                                    onDeleteClick = {
                                        transactionToDelete = transaction
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                        
                        // Show loading indicator when loading more transactions
                        if (isLoadingTransactions && pagedTransactions.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    StyledLoadingIndicator(size = 0.8f)
                                }
                            }
                        }
                        
                        // Add spacing at the bottom
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
            
            PullRefreshIndicator(
                refreshing = refreshingTransactions,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                contentColor = MaterialTheme.colorScheme.primary // Changed
            )
            
            // Show delete transaction confirmation dialog
            if (showDeleteDialog && transactionToDelete != null) {
                val context = LocalContext.current
                
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        transactionToDelete = null
                    },
                    title = { Text("Delete Transaction", color = MaterialTheme.colorScheme.onSurface) }, // Changed
                    text = {
                        Text(
                            "Are you sure you want to delete this transaction?",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, // Changed
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                // Handle deletion right here inside the dialog onClick
                                transactionToDelete?.let { transaction ->
                                    // Create toast message
                                    val amount = transaction.amount
                                    val isCredit = transaction.isCredit
                                    val formattedAmount = String.format("₹%.2f", amount)
                                    
                                    // Show feedback toast
                                    val message = if (isCredit) {
                                        "Removed \"to receive\" of $formattedAmount"
                                    } else {
                                        "Removed \"to pay\" of $formattedAmount"
                                    }
                                    
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    
                                    // Then delete the transaction
                                    viewModel.deleteTransaction(transaction)
                                    
                                    // Force balance update
                                    balanceVersion++
                                }
                                showDeleteDialog = false
                                transactionToDelete = null
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error) // Changed
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                transactionToDelete = null
                            }
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurface) // Changed
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp) // Changed
                )
            }
        }
    }
}

@Composable
fun PersonInfoCard(
    person: Person,
    balance: Double,
    onAddTransactionClick: () -> Unit
) {
    val context = LocalContext.current
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Person name and details section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface, // Changed
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = person.phoneNumber,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Changed
                    )
                }
                
                Button(
                    onClick = onAddTransactionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Changed
                        contentColor = MaterialTheme.colorScheme.onPrimary // Changed
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // Changed
                            shape = MaterialTheme.shapes.medium
                        )
                        .semantics {
                            contentDescription = "Add new transaction with ${person.name}"
                        },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        tint = MaterialTheme.colorScheme.onPrimary // Changed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Transaction", color = MaterialTheme.colorScheme.onPrimary) // Changed
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Balance section with gradient background
            GradientSurface( // GradientSurface itself now uses themed colors by default
                modifier = Modifier.fillMaxWidth(),
                startColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // Example themed gradient
                endColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)   // Example themed gradient
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Current Balance",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) // Changed
                        )
                        
                        val balanceText = when {
                            balance > 0 -> "To Receive"
                            balance < 0 -> "To Pay"
                            else -> "All Settled"
                        }
                        
                        Text(
                            text = balanceText,
                            style = MaterialTheme.typography.titleMedium,
                            color = when {
                                balance > 0 -> MaterialTheme.colorScheme.tertiary // Changed
                                balance < 0 -> MaterialTheme.colorScheme.error // Changed
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) // Changed
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (balance == 0.0) "₹0.00" else "₹${String.format("%.2f", abs(balance))}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = when {
                            balance > 0 -> MaterialTheme.colorScheme.tertiary // Changed
                            balance < 0 -> MaterialTheme.colorScheme.error // Changed
                            else -> MaterialTheme.colorScheme.onSurface // Changed
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { /* Keep this empty until viewModel is available */ },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary // Changed
                    ),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Verify Balances",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Verify",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val haptic = LocalHapticFeedback.current
    
    val dismissState = rememberDismissState { dismissValue ->
        if (dismissValue == DismissValue.DismissedToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDeleteClick()
            }
        false
        }

    SwipeToDismiss(
        state = dismissState,
        background = {
            val progress = dismissState.progress.fraction
            val iconScale = 1f + 0.6f * progress.coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.scale(iconScale)
                )
            }
        },
        dismissContent = {
            TransactionCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { /* Do nothing on click, just display details */ },
                isHighlighted = false
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Transaction amount and type
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isExpense = !transaction.isCredit
                            val amountColor = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary // Changed
                            val amountPrefix = if (isExpense) "-" else "+"
                            Text(
                                text = "$amountPrefix₹${String.format("%.2f", abs(transaction.amount))}",
                                style = MaterialTheme.typography.titleLarge,
                                color = amountColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = transaction.description.ifEmpty { "No description" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), // Changed
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dateFormatter.format(Date(transaction.date)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Changed
                        )
                    }
                    // Removed IconButton for delete
                }
            }
        },
        directions = setOf(DismissDirection.EndToStart)
    )
}

/**
 * Returns a human-readable string representing how long ago the timestamp was
 */
private fun getTimeAgoString(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val timeDiff = currentTime - timestamp
    
    // Convert to seconds, minutes, hours, and days
    val secondsAgo = timeDiff / 1000
    val minutesAgo = secondsAgo / 60
    val hoursAgo = minutesAgo / 60
    val daysAgo = hoursAgo / 24
    
    return when {
        secondsAgo < 60 -> "Just now"
        minutesAgo < 60 -> "$minutesAgo ${if (minutesAgo == 1L) "minute" else "minutes"} ago"
        hoursAgo < 24 -> "$hoursAgo ${if (hoursAgo == 1L) "hour" else "hours"} ago"
        daysAgo < 7 -> "$daysAgo ${if (daysAgo == 1L) "day" else "days"} ago"
        daysAgo < 30 -> "${daysAgo / 7} ${if (daysAgo / 7 == 1L) "week" else "weeks"} ago"
        daysAgo < 365 -> "${daysAgo / 30} ${if (daysAgo / 30 == 1L) "month" else "months"} ago"
        else -> "${daysAgo / 365} ${if (daysAgo / 365 == 1L) "year" else "years"} ago"
    }
}
