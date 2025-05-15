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
import com.example.fedger.ui.theme.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceSummaryScreen(
    viewModel: PersonViewModel,
    onBackClick: () -> Unit
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
                                    spotColor = PurpleHighlight
                                )
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MediumPurple,
                                            MediumPurple.copy(alpha = 0.9f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "₹",
                                color = TextWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Fedger",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepPurple,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Balance Summary Header
            Text(
                text = "Balance Summary",
                style = MaterialTheme.typography.headlineMedium,
                color = TextWhite,
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
                    accentColor = TextGreen,
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
                            color = TextGrey
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "₹${String.format("%.2f", totalBalance.totalOwedToMe)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // "To Pay" card
                AccentCard(
                    modifier = Modifier.weight(1f),
                    accentColor = TextRed,
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
                            color = TextGrey
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "₹${String.format("%.2f", totalBalance.totalIOwed)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextRed,
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
                        color = TextGrey
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val netBalance = totalBalance.totalOwedToMe - totalBalance.totalIOwed
                    val balanceColor = when {
                        netBalance > 0 -> TextGreen
                        netBalance < 0 -> TextRed
                        else -> TextGrey
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
                        color = TextGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Statistics
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite,
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
                        color = PurpleHighlight
                    )
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = SurfaceLight.copy(alpha = 0.3f)
                    )
                    
                    SummaryItem(
                        title = "Total Transactions",
                        value = transactions.size.toString(),
                        color = AccentAmber
                    )
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = SurfaceLight.copy(alpha = 0.3f)
                    )
                    
                    SummaryItem(
                        title = "Contacts To Receive From",
                        value = contactsWithPositiveBalance.toString(),
                        color = TextGreen
                    )
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = SurfaceLight.copy(alpha = 0.3f)
                    )
                    
                    SummaryItem(
                        title = "Contacts To Pay",
                        value = contactsWithNegativeBalance.toString(),
                        color = TextRed
                    )
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = SurfaceLight.copy(alpha = 0.3f)
                    )
                    
                    SummaryItem(
                        title = "Settled Contacts",
                        value = contactsWithZeroBalance.toString(),
                        color = TextGrey
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
            color = TextWhite
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