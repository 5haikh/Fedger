package com.example.fedger.ui.screens

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.fedger.model.Person
import com.example.fedger.model.Transaction
import com.example.fedger.ui.PersonViewModel
import com.example.fedger.ui.theme.MediumPurple
import com.example.fedger.ui.theme.TextRed
import com.example.fedger.ui.theme.TextWhite
import com.example.fedger.ui.theme.CardBackground
import com.example.fedger.ui.theme.HighContrastGrey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
// Import our new UI components
import com.example.fedger.ui.components.ContentStateHandler
import com.example.fedger.ui.components.EmptyTransactionsState
import com.example.fedger.ui.components.LoadingState
import com.example.fedger.ui.components.EnhancedCard
import com.example.fedger.ui.components.TransactionCard
import com.example.fedger.ui.components.GradientSurface
import com.example.fedger.ui.components.StyledLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun PersonDetailsScreen(
    personId: Int,
    viewModel: PersonViewModel,
    onBackClick: () -> Unit,
    onAddTransactionClick: () -> Unit
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
    
    fun onDeleteTransaction(transaction: Transaction) {
        viewModel.deleteTransaction(transaction)
        // Force a balance update after transaction is deleted
        balanceVersion++
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Back to Persons", color = TextWhite) },
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
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    color = TextWhite,
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
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = if (totalTransactionCount > 0) 
                            "${totalTransactionCount} ${if (totalTransactionCount == 1) "transaction" else "transactions"}" 
                        else "",
                        color = TextWhite.copy(alpha = 0.6f),
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
                            key = { transaction -> transaction.id } // Explicitly use named parameter
                        ) { transaction ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically(),
                                modifier = Modifier.animateItemPlacement(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
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
                backgroundColor = CardBackground,
                contentColor = MediumPurple
            )
            
            // Show delete transaction confirmation dialog
            if (showDeleteDialog && transactionToDelete != null) {
                AlertDialog(
                    onDismissRequest = { 
                        showDeleteDialog = false 
                        transactionToDelete = null
                    },
                    title = { Text("Delete Transaction", color = TextWhite) },
                    text = { 
                        Text(
                            "Are you sure you want to delete this transaction?",
                            color = HighContrastGrey,
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                transactionToDelete?.let { onDeleteTransaction(it) }
                                showDeleteDialog = false
                                transactionToDelete = null
                            }
                        ) {
                            Text("Delete", color = TextRed)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                transactionToDelete = null
                            }
                        ) {
                            Text("Cancel", color = TextWhite)
                        }
                    },
                    containerColor = CardBackground
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
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = person.phoneNumber,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextWhite.copy(alpha = 0.7f)
                    )
                }
                
                Button(
                    onClick = onAddTransactionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MediumPurple),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.semantics {
                        contentDescription = "Add new transaction with ${person.name}"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        tint = TextWhite
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Transaction", color = TextWhite)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Balance section with gradient background
            GradientSurface(
                modifier = Modifier.fillMaxWidth(),
                startColor = CardBackground.copy(alpha = 0.5f),
                endColor = CardBackground.copy(alpha = 0.3f)
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
                            color = TextWhite.copy(alpha = 0.8f)
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
                                balance > 0 -> Color.Green.copy(alpha = 0.8f)
                                balance < 0 -> TextRed
                                else -> TextWhite.copy(alpha = 0.8f)
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (balance == 0.0) "₹0.00" else "₹${String.format("%.2f", abs(balance))}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = when {
                            balance > 0 -> Color.Green.copy(alpha = 0.8f)
                            balance < 0 -> TextRed
                            else -> TextWhite
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    
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
                    val amountColor = if (isExpense) TextRed else Color.Green.copy(alpha = 0.8f)
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
                    color = TextWhite.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = dateFormatter.format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWhite.copy(alpha = 0.6f)
                )
            }
            
            // Delete button with improved interaction
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(40.dp)
                    .semantics {
                        contentDescription = "Delete this transaction"
                        role = Role.Button
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = TextRed.copy(alpha = 0.7f)
                )
            }
        }
    }
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
