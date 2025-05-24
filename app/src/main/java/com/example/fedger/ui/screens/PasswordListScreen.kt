package com.example.fedger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fedger.model.PasswordEntry
import com.example.fedger.ui.PasswordViewModel
import com.example.fedger.ui.components.EnhancedCard
import com.example.fedger.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavController
import com.example.fedger.ui.components.AppSwitcher

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun PasswordListScreen(
    viewModel: PasswordViewModel,
    onPasswordEntryClick: (PasswordEntry) -> Unit,
    onAddEntryClick: () -> Unit,
    onImportExportClick: () -> Unit,
    navController: NavController
) {
    val passwordEntries by viewModel.passwordEntries.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    
    val isSearchActive = searchQuery.isNotEmpty()
    val displayEntries = if (isSearchActive) searchResults else passwordEntries
    
    // Pull-to-refresh state
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            refreshing = true
            viewModel.clearSearchQuery()
            refreshing = false
        }
    )
    
    // Clear search when entering the screen
    LaunchedEffect(Unit) {
        viewModel.clearSearchQuery()
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
                                    shape = CircleShape
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
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Icon",
                                tint = TextWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Password Manager",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepPurple,
                    titleContentColor = TextWhite,
                    actionIconContentColor = TextWhite
                ),
                actions = {
                    // App Switcher
                    AppSwitcher(
                        navController = navController,
                        currentApp = "Password Manager"
                    )
                    
                    // Import/Export button
                    IconButton(onClick = { onImportExportClick() }) {
                        Icon(
                            Icons.Default.ImportExport,
                            contentDescription = "Import/Export Passwords",
                            tint = TextWhite
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepPurple)
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    placeholder = { Text("Search passwords...", color = TextGrey) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = LightPurple)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearchQuery() }) {
                                Icon(
                                    Icons.Default.Clear, 
                                    contentDescription = "Clear search",
                                    tint = LightPurple
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedBorderColor = MediumPurple,
                        unfocusedBorderColor = LightPurple.copy(alpha = 0.5f),
                        cursorColor = MediumPurple
                    ),
                    shape = MaterialTheme.shapes.medium
                )
                
                if (displayEntries.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = LightPurple
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isSearchActive) 
                                    "No results for \"$searchQuery\"" 
                                else 
                                    "No passwords yet",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextWhite
                            )
                            if (!isSearchActive) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Add your first password by tapping the + button",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextGrey
                                )
                            }
                        }
                    }
                } else {
                    // Password list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = displayEntries,
                            key = { it.id }
                        ) { entry ->
                            PasswordEntryItem(
                                entry = entry,
                                onClick = { onPasswordEntryClick(entry) }
                            )
                        }
                        // Add some space at the bottom
                        item {
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                }
            }
            
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = SurfaceLight,
                contentColor = MediumPurple
            )
        }
    }
}

@Composable
fun PasswordEntryItem(
    entry: PasswordEntry,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    EnhancedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MediumPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = LightPurple
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val supportingText = if (entry.description.isNotEmpty()) {
                    entry.description
                } else if (entry.category.isNotEmpty()) {
                    entry.category
                } else {
                    "Last updated: ${dateFormat.format(Date(entry.updatedAt))}"
                }
                
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Trailing icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = LightPurple
            )
        }
    }
} 