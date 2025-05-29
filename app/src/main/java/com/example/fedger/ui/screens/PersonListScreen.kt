package com.example.fedger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import com.example.fedger.model.Person
import com.example.fedger.ui.PersonViewModel
import com.example.fedger.ui.PersonSortOption
// import com.example.fedger.ui.theme.* // Removed direct theme imports
import kotlinx.coroutines.flow.Flow
import kotlin.math.abs
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.TopAppBarDefaults // Added for TopAppBar colors
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.example.fedger.ui.components.ContentStateHandler
import com.example.fedger.ui.components.EmptyPersonListState
import com.example.fedger.ui.components.LoadingState
import com.example.fedger.ui.components.EnhancedCard
import com.example.fedger.ui.components.ItemAnimations
import com.example.fedger.ui.components.StyledLoadingIndicator
import com.example.fedger.ui.components.AccentCard
import com.example.fedger.ui.components.AppSwitcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.rememberDismissState
import androidx.compose.material.DismissState
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.navigation.NavController

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun PersonListScreen(
    onPersonClick: (Person) -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: (Person) -> Unit,
    onImportExportClick: () -> Unit,
    viewModel: PersonViewModel = viewModel(),
    navController: NavController
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var personToDelete by remember { mutableStateOf<Person?>(null) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val pagedPersons by viewModel.pagedPersons.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasMoreData by viewModel.hasMoreData.collectAsState()
    val showingSearchResults by viewModel.showingSearchResults.collectAsState()
    val currentSortOption by viewModel.currentSortOption.collectAsState()
    val lazyListState = rememberLazyListState()
    
    // Pull-to-refresh state
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            if (!refreshing && !isLoading) {
                refreshing = true
                viewModel.refreshData()
                // refreshing will be set to false when isLoading becomes false
            }
        }
    )
    
    // Sort dropdown
    var showSortDropdown by remember { mutableStateOf(false) }
    
    // Set refreshing to false when loading is complete
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            refreshing = false
        }
    }
    
    // Detect when we're near the end of the list to trigger loading more data
    LaunchedEffect(lazyListState) {
        snapshotFlow { 
            // Combine visible items with scroll position to detect end of list
            val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
            val lastIndex = lastVisibleItem?.index ?: 0
            val totalItemsCount = lazyListState.layoutInfo.totalItemsCount
            val isAtEnd = lastIndex >= totalItemsCount - 3 && !lazyListState.isScrollInProgress
            
            Triple(lastIndex, totalItemsCount, isAtEnd)
        }.collect { (lastIndex, total, isAtEnd) ->
            // Only load more if we're near the end, have more data, and aren't already loading
            if (isAtEnd && hasMoreData && !isLoading && total > 0 && lastIndex > 0) {
                viewModel.loadNextPage()
            }
        }
    }
    
    // Scroll to top when search query changes
    LaunchedEffect(searchQuery) {
        if (lazyListState.firstVisibleItemIndex > 0) {
            lazyListState.animateScrollToItem(0)
        }
    }
    
    // Observe search query changes from the UI and update the ViewModel
    fun updateSearchQuery(query: String) {
        // Simply pass the query to the ViewModel - it will handle all state changes
        viewModel.setSearchQuery(query)
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
                                    spotColor = MaterialTheme.colorScheme.secondary // Added spotColor
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
                actions = {
                    // App Switcher
                    AppSwitcher(
                        navController = navController,
                        currentApp = "Ledger"
                        // Tint for AppSwitcher will be handled by its own theming update
                    )
                    
                    // Import/Export button
                    IconButton(onClick = { onImportExportClick() }) {
                        Icon(
                            Icons.Default.ImportExport,
                            contentDescription = "Import/Export Data",
                            tint = MaterialTheme.colorScheme.onPrimary // Changed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Changed
                    titleContentColor = MaterialTheme.colorScheme.onPrimary, // Changed
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary // Changed
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background) // Added background
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
                    onValueChange = { updateSearchQuery(it) },
                    placeholder = { Text("Search contacts", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { updateSearchQuery("") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // Changed
                        focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                        cursorColor = MaterialTheme.colorScheme.primary, // Changed
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), // Changed
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), // Changed
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) // Changed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Sort options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sort by:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                    )
                    Surface(
                        tonalElevation = 4.dp,
                        shadowElevation = 8.dp,
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)), // Changed
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f), // Changed
                        modifier = Modifier
                            .height(40.dp)
                            .defaultMinSize(minWidth = 160.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showSortDropdown = true }
                            .semantics {
                                contentDescription = "Open sort options menu, currently sorted by: ${
                                    when(currentSortOption) {
                                        PersonSortOption.NAME_ASC -> "Name (A-Z)"
                                        PersonSortOption.NAME_DESC -> "Name (Z-A)"
                                        PersonSortOption.BALANCE_HIGH_TO_LOW -> "Balance (High to Low)"
                                        PersonSortOption.BALANCE_LOW_TO_HIGH -> "Balance (Low to High)"
                                        PersonSortOption.LAST_ADDED -> "Recently Added"
                                    }
                                }"
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = when(currentSortOption) {
                                    PersonSortOption.NAME_ASC -> "Name (A-Z)"
                                    PersonSortOption.NAME_DESC -> "Name (Z-A)"
                                    PersonSortOption.BALANCE_HIGH_TO_LOW -> "Balance (High to Low)"
                                    PersonSortOption.BALANCE_LOW_TO_HIGH -> "Balance (Low to High)"
                                    PersonSortOption.LAST_ADDED -> "Recently Added"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface, // Changed
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant, // Changed
                                modifier = Modifier.padding(start = 8.dp)
                            )
                                }
                        }
                    }
                    DropdownMenu(
                        expanded = showSortDropdown,
                        onDismissRequest = { showSortDropdown = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), shape = MaterialTheme.shapes.medium) // Changed
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)), shape = MaterialTheme.shapes.medium) // Changed
                            .shadow(8.dp, shape = MaterialTheme.shapes.medium)
                    ) {
                        SortOption(
                            title = "Name (A-Z)",
                            selected = currentSortOption == PersonSortOption.NAME_ASC,
                            onClick = {
                                viewModel.setSortOption(PersonSortOption.NAME_ASC)
                                showSortDropdown = false
                            }
                        )
                        SortOption(
                            title = "Name (Z-A)",
                            selected = currentSortOption == PersonSortOption.NAME_DESC,
                            onClick = {
                                viewModel.setSortOption(PersonSortOption.NAME_DESC)
                                showSortDropdown = false
                            }
                        )
                        SortOption(
                            title = "Balance (High to Low)",
                            selected = currentSortOption == PersonSortOption.BALANCE_HIGH_TO_LOW,
                            onClick = {
                                viewModel.setSortOption(PersonSortOption.BALANCE_HIGH_TO_LOW)
                                showSortDropdown = false
                            }
                        )
                        SortOption(
                            title = "Balance (Low to High)",
                            selected = currentSortOption == PersonSortOption.BALANCE_LOW_TO_HIGH,
                            onClick = {
                                viewModel.setSortOption(PersonSortOption.BALANCE_LOW_TO_HIGH)
                                showSortDropdown = false
                            }
                        )
                        SortOption(
                            title = "Recently Added",
                            selected = currentSortOption == PersonSortOption.LAST_ADDED,
                            onClick = {
                                viewModel.setSortOption(PersonSortOption.LAST_ADDED)
                                showSortDropdown = false
                            }
                        )
                        }
                    }
                }
                
                if (searchQuery.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Search results for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Use ContentStateHandler to handle different states
                ContentStateHandler(
                    isLoading = isLoading && pagedPersons.isEmpty(),
                    items = pagedPersons,
                    emptyContent = { 
                        if (searchQuery.isEmpty()) {
                            EmptyPersonListState(onAddClick = onAddClick)
                        } else {
                            EmptySearchState(query = searchQuery)
                        }
                    },
                    loadingContent = {
                        LoadingState(message = if (showingSearchResults) "Searching..." else "Loading contacts...")
                    }
                ) {
                    // This animates the whole list for smoother transitions
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        LazyColumn(
                            state = lazyListState,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(
                                items = pagedPersons,
                                key = { it.id }
                            ) { person ->
                                // Get the current calculated balance for this person
                                val calculatedBalance = viewModel.getCurrentBalanceForPerson(person.id)
                                
                                PersonItem(
                                    person = person,
                                    balance = calculatedBalance,
                                    onPersonClick = { onPersonClick(person) },
                                    onDeleteClick = { 
                                        personToDelete = person
                                        showDeleteDialog = true
                                    },
                                    modifier = Modifier.animateItemPlacement(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                )
                            }
                            
                            // Show loading indicator at bottom when loading more
                            if (isLoading && pagedPersons.isNotEmpty()) {
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
                        }
                    }
                }
            }
            
            // Styled pull-to-refresh indicator
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                backgroundColor = MaterialTheme.colorScheme.surface, // Changed
                contentColor = MaterialTheme.colorScheme.primary, // Changed
                modifier = Modifier.align(Alignment.TopCenter)
            )
            
            // Confirmation Dialog for deleting a person
            if (showDeleteDialog && personToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        personToDelete = null
                    },
                    title = {
                        Text(
                            text = "Delete Contact",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface // Changed
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to delete ${personToDelete?.name}? All associated transactions will also be deleted. This action cannot be undone.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                personToDelete?.let { onDeleteClick(it) }
                                showDeleteDialog = false
                                personToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error, // Changed
                                contentColor = MaterialTheme.colorScheme.onError // Changed
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = {
                                showDeleteDialog = false
                                personToDelete = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface // Changed
                            )
                        ) {
                            Text("Cancel")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), // Changed
                    shape = MaterialTheme.shapes.large
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PersonItem(
    person: Person,
    balance: Double,
    onPersonClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val dismissState = rememberDismissState(
        confirmStateChange = {
            if (it == DismissValue.DismissedToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDeleteClick()
            }
            false // Don't auto-dismiss, let the parent handle removal
        }
    )
    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
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
                    tint = MaterialTheme.colorScheme.error, // Changed
                    modifier = Modifier.scale(iconScale)
                )
            }
        },
        dismissContent = {
    EnhancedCard( // EnhancedCard itself now uses themed colors
        modifier = modifier.fillMaxWidth(),
        onClick = onPersonClick,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Avatar/initials circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            spotColor = MaterialTheme.colorScheme.primary // Added spotColor
                        )
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f), // Changed
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) // Changed
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = person.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer // Changed
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                                .padding(end = 8.dp)
                ) {
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface, // Changed
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val balanceColor = when {
                            balance > 0 -> MaterialTheme.colorScheme.tertiary // Changed
                            balance < 0 -> MaterialTheme.colorScheme.error // Changed
                            else -> MaterialTheme.colorScheme.onSurfaceVariant // Changed
                        }
                        Text(
                            text = when {
                                balance > 0 -> "To Receive: "
                                balance < 0 -> "To Pay: "
                                else -> "All settled"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, // Changed
                            maxLines = 1
                        )
                        if (balance != 0.0) {
                            Text(
                                text = "₹${String.format("%.2f", abs(balance))}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = balanceColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
                    // Removed IconButton for delete
            }
        }
    }
    )
}

@Composable
fun EmptySearchState(query: String) {
    // Using our common empty state component from the EmptyStates file
    com.example.fedger.ui.components.EmptySearchState(query = query)
}

@Composable
private fun SortOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val backgroundColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) // Changed
        isPressed -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.10f) // Changed
        else -> Color.Transparent
    }
    val border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null // Changed
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        backgroundColor,
                        shape = MaterialTheme.shapes.medium
                    )
                    .then(if (border != null) Modifier.border(border, shape = MaterialTheme.shapes.medium) else Modifier)
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .then(Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    ))
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface, // Changed
                    modifier = Modifier.weight(1f)
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, // Changed
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        onClick = onClick,
        modifier = Modifier
            .semantics {
            contentDescription = "$title${if (selected) ", selected" else ""}"
        }
    )
}
