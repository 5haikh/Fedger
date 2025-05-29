package com.example.fedger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fedger.ui.PersonViewModel
import com.example.fedger.ui.components.AccentCard
import com.example.fedger.ui.components.EnhancedCard
// import com.example.fedger.ui.theme.* // Removed direct theme imports
import kotlin.math.abs
import androidx.navigation.NavController
// import com.example.fedger.ui.components.AppSwitcher // AppSwitcher removed as per previous instruction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceSummaryScreen(
    viewModel: PersonViewModel,
    onBackClick: () -> Unit,
    navController: NavController
) {
    // Observe total balance data
    val totalBalance by viewModel.totalBalance.collectAsState()
    
    // Get counts
    val contacts by viewModel.persons.collectAsState(initial = emptyList())
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    
    // Calculate stats
    val contactsWithPositiveBalance = contacts.count { contact -> 
        val balance = viewModel.getCurrentBalanceForPerson(contact.id)
        balance > 0
    }
    
    val contactsWithNegativeBalance = contacts.count { contact -> 
        val balance = viewModel.getCurrentBalanceForPerson(contact.id)
        balance < 0
    }
    
    val contactsWithZeroBalance = contacts.count { contact -> 
        val balance = viewModel.getCurrentBalanceForPerson(contact.id)
        balance == 0.0
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
                                    spotColor = MaterialTheme.colorScheme.secondary // Changed
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
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background) // Added background
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Balance Summary Header
            Text(
                text = "Balance Summary",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground, // Changed
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            // Main balance cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // "To Receive" card
                AccentCard(
                    modifier = Modifier.weight(1f),
                    accentColor = MaterialTheme.colorScheme.tertiary, // Changed (Placeholder for positive)
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "To Receive",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "₹${String.format("%.2f", totalBalance.totalOwedToMe)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.tertiary, // Changed
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // "To Pay" card
                AccentCard(
                    modifier = Modifier.weight(1f),
                    accentColor = MaterialTheme.colorScheme.error, // Changed
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "To Pay",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "₹${String.format("%.2f", totalBalance.totalIOwed)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error, // Changed
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Net Balance
            EnhancedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val netBalance = totalBalance.totalOwedToMe - totalBalance.totalIOwed
                    val balanceColor = when {
                        netBalance > 0 -> MaterialTheme.colorScheme.tertiary // Changed
                        netBalance < 0 -> MaterialTheme.colorScheme.error // Changed
                        else -> MaterialTheme.colorScheme.onSurfaceVariant // Changed
                    }
                    
                    Text(
                        text = when {
                            netBalance > 0 -> "+₹${String.format("%.2f", netBalance)}"
                            netBalance < 0 -> "-₹${String.format("%.2f", abs(netBalance))}"
                            else -> "₹0.00"
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = balanceColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when {
                            netBalance > 0 -> "Overall, you are owed money"
                            netBalance < 0 -> "Overall, you owe money"
                            else -> "Your accounts are balanced"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Changed
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Statistics
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground, // Changed
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Start
            )
            
            // Summary cards
            EnhancedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SummaryItem(
                        title = "Total Contacts",
                        value = contacts.size.toString(),
                        color = MaterialTheme.colorScheme.primary // Changed
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) // Changed
                    )
                    
                    SummaryItem(
                        title = "Total Transactions",
                        value = transactions.size.toString(),
                        color = MaterialTheme.colorScheme.secondary // Changed
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) // Changed
                    )
                    
                    SummaryItem(
                        title = "Contacts To Receive From",
                        value = contactsWithPositiveBalance.toString(),
                        color = MaterialTheme.colorScheme.tertiary // Changed
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) // Changed
                    )
                    
                    SummaryItem(
                        title = "Contacts To Pay",
                        value = contactsWithNegativeBalance.toString(),
                        color = MaterialTheme.colorScheme.error // Changed
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) // Changed
                    )
                    
                    SummaryItem(
                        title = "Settled Contacts",
                        value = contactsWithZeroBalance.toString(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SummaryItem(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface // Changed
        )
        
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.2f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
} 