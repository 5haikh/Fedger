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
// import com.example.fedger.ui.theme.* // Removed direct theme imports
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavController
import androidx.compose.material3.TopAppBarDefaults // Added for TopAppBar colors
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
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Icon",
                                tint = MaterialTheme.colorScheme.onPrimary, // Changed
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Password Manager",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary, // Changed
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Changed
                    titleContentColor = MaterialTheme.colorScheme.onPrimary, // Changed
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary, // Changed
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary // Changed (though no nav icon here)
                ),
                actions = {
                    // App Switcher
                    AppSwitcher(
                        navController = navController,
                        currentApp = "Password Manager"
                        // Tint for AppSwitcher will be handled by its own theming update
                    )
                    
                    // Import/Export button
                    IconButton(onClick = { onImportExportClick() }) {
                        Icon(
                            Icons.Default.ImportExport,
                            contentDescription = "Import/Export Passwords",
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
                    placeholder = { Text("Search passwords...", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) // Changed
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearchQuery() }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                        focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Changed
                        cursorColor = MaterialTheme.colorScheme.primary // Changed
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
                                tint = MaterialTheme.colorScheme.secondary // Changed
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isSearchActive)
                                    "No results for \"$searchQuery\""
                                else
                                    "No passwords yet",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onBackground // Changed
                            )
                            if (!isSearchActive) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Add your first password by tapping the + button",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant // Changed
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
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant, // Changed
                contentColor = MaterialTheme.colorScheme.primary // Changed
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
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)), // Changed
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer // Changed
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
                    color = MaterialTheme.colorScheme.onSurface, // Changed
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Changed
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Trailing icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant // Changed
            )
        }
    }
}