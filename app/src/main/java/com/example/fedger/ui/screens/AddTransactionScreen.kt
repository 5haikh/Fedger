package com.example.fedger.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import com.example.fedger.model.Person
import com.example.fedger.model.Transaction
import com.example.fedger.ui.PersonViewModel
import com.example.fedger.ui.components.EnhancedCard
// import com.example.fedger.ui.theme.* // Removed direct theme imports
import androidx.compose.foundation.border

/**
 * Unified AddTransactionScreen that handles both specific-person and global transactions
 *
 * @param preselectedPerson Optional person that is pre-selected (for person-specific transactions)
 * @param viewModel The PersonViewModel (required for global mode to fetch persons)
 * @param onTransactionAdded Callback when a transaction is added
 * @param onBackClick Callback when back button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    preselectedPerson: Person? = null,
    viewModel: PersonViewModel? = null,
    onTransactionAdded: (Transaction) -> Unit,
    onBackClick: () -> Unit
) {
    // Mode can be determined by whether we have a preselected person
    val isGlobalMode = preselectedPerson == null
    
    // If we're not in global mode, we must have a preselected person
    if (!isGlobalMode && preselectedPerson == null) {
        throw IllegalArgumentException("preselectedPerson cannot be null when not in global mode")
    }
    
    // If we're in global mode, we must have a viewModel
    if (isGlobalMode && viewModel == null) {
        throw IllegalArgumentException("viewModel cannot be null when in global mode")
    }
    
    // State for form fields
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("sent") } // Default to "To Receive" mode
    var selectedPersonId by remember { mutableStateOf<Int?>(preselectedPerson?.id) }
    
    // For global mode: track contacts and dropdown state
    val allContacts = if (isGlobalMode) {
        viewModel?.persons?.collectAsState(initial = emptyList())?.value ?: emptyList()
    } else {
        preselectedPerson?.let { listOf(it) } ?: emptyList()
    }
    
    var expanded by remember { mutableStateOf(false) }
    val selectedContact = if (isGlobalMode) {
        allContacts.find { it.id == selectedPersonId }
    } else {
        preselectedPerson
    }
    
    // Make sure we have the latest data in global mode
    if (isGlobalMode) {
        LaunchedEffect(Unit) {
            viewModel?.refreshData()
        }
    }
    
    // Validation states
    var amountError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var contactError by remember { mutableStateOf<String?>(null) }
    
    // Add validation touch state to track if fields have been interacted with
    var amountTouched by remember { mutableStateOf(false) }
    var descriptionTouched by remember { mutableStateOf(false) }
    
    // Scroll state for the form
    val scrollState = rememberScrollState()
    
    // Validation functions
    fun validateAmount(value: String): String? {
        return when {
            value.isBlank() -> "Amount is required"
            else -> try {
                val amountValue = value.toDouble()
                if (amountValue <= 0) "Amount must be greater than 0" else null
            } catch (e: NumberFormatException) {
                "Enter a valid amount"
            }
        }
    }
    
    fun validateDescription(value: String): String? {
        return when {
            value.isBlank() -> "Description is required"
            value.length < 3 -> "Description must be at least 3 characters"
            else -> null
        }
    }
    
    // Unified validate function
    fun validate(): Boolean {
        // Always mark fields as touched when submitting
        amountTouched = true
        descriptionTouched = true
        
        // Validate all fields
        val amountValidation = validateAmount(amount)
        val descriptionValidation = validateDescription(description)
        
        // Update error states
        amountError = amountValidation
        descriptionError = descriptionValidation
        
        // Contact validation only needed in global mode
        if (isGlobalMode && selectedPersonId == null) {
            contactError = "Please select a contact"
            return false
        }
        
        // Return true if all validations pass
        return amountValidation == null && descriptionValidation == null
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
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate back"
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
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background) // Added background
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Transaction",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground, // Changed
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            
            // Main content card
            EnhancedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = 4.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Transaction Details Header
                        Text(
                            text = "Transaction Details",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface, // Changed
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = if (isGlobalMode)
                                "Add a new transaction with any contact."
                            else
                                "Add a new transaction with ${preselectedPerson!!.name}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                        )
                        
                        // Contact Selection - Only shown in global mode
                        if (isGlobalMode) {
                            Text(
                                text = "Select Contact",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface, // Changed
                                fontWeight = FontWeight.Medium
                            )
                            
                            // Create a simple dropdown menu system
                            var showContactPicker by remember { mutableStateOf(false) }
                            var contactSearchQuery by remember { mutableStateOf("") }
                            
                            // The contact selection field
                            OutlinedTextField(
                                value = selectedContact?.name ?: "",
                                onValueChange = { /* Read only */ },
                                placeholder = { Text("Select a contact", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // Changed
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                                    cursorColor = MaterialTheme.colorScheme.primary, // Changed
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), // Changed
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), // Changed
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // Changed
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) // Changed
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showContactPicker = true }
                                    .semantics {
                                        contentDescription = "Contact selection field"
                                        role = Role.Button
                                    },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showContactPicker = true }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Show contacts",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                                        )
                                    }
                                },
                                isError = contactError != null,
                                supportingText = {
                                    if (contactError != null) {
                                        Text(
                                            text = contactError!!,
                                            color = MaterialTheme.colorScheme.error, // Changed
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    } else if (selectedContact != null) {
                                        Text(
                                            text = "Selected: ${selectedContact.name}",
                                            color = MaterialTheme.colorScheme.tertiary, // Changed (Placeholder for success)
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                shape = MaterialTheme.shapes.medium,
                                enabled = false
                            )
                            
                            // Full screen contact picker dialog
                            if (showContactPicker) {
                                AlertDialog(
                                    onDismissRequest = { showContactPicker = false },
                                    title = {
                                        Text(
                                            "Select Contact",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface // Changed
                                        )
                                    },
                                    text = {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 350.dp)
                                        ) {
                                            // Search field
                                            OutlinedTextField(
                                                value = contactSearchQuery,
                                                onValueChange = { contactSearchQuery = it },
                                                placeholder = { Text("Search contacts", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // Changed
                                                    focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                                                    cursorColor = MaterialTheme.colorScheme.primary, // Changed
                                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // Changed
                                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) // Changed
                                                ),
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = "Search contacts",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 8.dp),
                                                singleLine = true,
                                                shape = MaterialTheme.shapes.small
                                            )
                                            
                                            // Divider
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // Changed
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            
                                            // Filter contacts
                                            val filteredContacts = allContacts.filter { person -> 
                                                person.name.contains(contactSearchQuery, ignoreCase = true) || 
                                                person.phoneNumber.contains(contactSearchQuery, ignoreCase = true)
                                            }
                                            
                                            // Contact list
                                            if (filteredContacts.isEmpty()) {
                                                Text(
                                                    text = "No contacts found",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Changed
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    textAlign = TextAlign.Center
                                                )
                                            } else {
                                                Column(
                                                    modifier = Modifier
                                                        .verticalScroll(rememberScrollState())
                                                ) {
                                                    filteredContacts.forEach { person ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    selectedPersonId = person.id
                                                                    contactError = null
                                                                    showContactPicker = false
                                                                }
                                                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                                                .background(
                                                                    if (selectedPersonId == person.id)
                                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) // Changed
                                                                    else
                                                                        Color.Transparent,
                                                                    shape = MaterialTheme.shapes.small
                                                                ),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            // Avatar
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(40.dp)
                                                                    .padding(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)), // Changed
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = person.name.firstOrNull()?.toString() ?: "?",
                                                                    color = MaterialTheme.colorScheme.onSecondaryContainer, // Changed
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                            
                                                            // Contact details
                                                            Column(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .padding(start = 8.dp)
                                                            ) {
                                                                Text(
                                                                    text = person.name,
                                                                    color = MaterialTheme.colorScheme.onSurface, // Changed
                                                                    style = MaterialTheme.typography.bodyLarge,
                                                                    fontWeight = if (selectedPersonId == person.id)
                                                                        FontWeight.Bold else FontWeight.Normal
                                                                )
                                                                if (person.phoneNumber.isNotEmpty()) {
                                                                    Text(
                                                                        text = person.phoneNumber,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Changed
                                                                        style = MaterialTheme.typography.bodySmall
                                                                    )
                                                                }
                                                            }
                                                            
                                                            // Selected check mark
                                                            if (selectedPersonId == person.id) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = "Selected",
                                                                    tint = MaterialTheme.colorScheme.tertiary, // Changed
                                                                    modifier = Modifier.padding(4.dp)
                                                                )
                                                            }
                                                        }
                                                        
                                                        // Only add divider if not the last item
                                                        if (person != filteredContacts.last()) {
                                                            HorizontalDivider(
                                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), // Changed
                                                                modifier = Modifier.padding(start = 48.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(
                                            onClick = { showContactPicker = false },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary // Changed
                                            )
                                        ) {
                                            Text("Done")
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp), // Changed
                                    shape = MaterialTheme.shapes.medium
                                )
                            }
                        }
                        
                        // Transaction Type Selection
                        Text(
                            text = "Transaction Type",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface, // Changed
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Segmented button style toggle
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)) // Changed
                                .padding(4.dp)
                                .semantics {
                                    contentDescription = "Select transaction type: ${if (transactionType == "sent") "To Receive" else "To Pay"}"
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // To Receive option (money sent by you, they owe you)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(MaterialTheme.shapes.small)
                                        .background(
                                            if (transactionType == "sent")
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) // Changed
                                            else
                                                Color.Transparent
                                        )
                                        .clickable { transactionType = "sent" }
                                        .semantics {
                                            contentDescription = "Option: To Receive" + if (transactionType == "sent") ", Selected" else ""
                                            role = Role.RadioButton
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "To Receive",
                                            color = if (transactionType == "sent") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, // Changed
                                            fontWeight = if (transactionType == "sent")
                                                FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                                
                                // To Pay option (money received by you, you owe them)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(MaterialTheme.shapes.small)
                                        .background(
                                            if (transactionType == "received")
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) // Changed
                                            else
                                                Color.Transparent
                                        )
                                        .clickable { transactionType = "received" }
                                        .semantics {
                                            contentDescription = "Option: To Pay" + if (transactionType == "received") ", Selected" else ""
                                            role = Role.RadioButton
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "To Pay",
                                            color = if (transactionType == "received") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, // Changed
                                            fontWeight = if (transactionType == "received")
                                                FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Amount Field
                        Text(
                            text = "Amount",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface, // Changed
                            fontWeight = FontWeight.Medium
                        )
                        
                        OutlinedTextField(
                            value = amount,
                            onValueChange = {
                                amount = it
                                amountTouched = true
                                amountError = validateAmount(it)
                            },
                            placeholder = { Text("Enter amount", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // Changed
                                focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                                cursorColor = MaterialTheme.colorScheme.primary, // Changed
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), // Changed
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) // Changed
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            isError = amountTouched && amountError != null,
                            supportingText = {
                                if (amountTouched && amountError != null) {
                                    Text(
                                        text = amountError!!,
                                        color = MaterialTheme.colorScheme.error, // Changed
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else if (amountTouched && amount.isNotBlank() && amountError == null) { // Added not blank and no error check
                                    Text(
                                        text = "Valid amount",
                                        color = MaterialTheme.colorScheme.tertiary, // Changed (Placeholder for success)
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            prefix = { Text("₹", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Added color
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                        
                        // Description Field
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface, // Changed
                            fontWeight = FontWeight.Medium
                        )
                        
                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                description = it
                                descriptionTouched = true
                                descriptionError = validateDescription(it)
                            },
                            placeholder = { Text("What's this transaction for?", color = MaterialTheme.colorScheme.onSurfaceVariant) }, // Changed
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // Changed
                                focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                                cursorColor = MaterialTheme.colorScheme.primary, // Changed
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), // Changed
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) // Changed
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            isError = descriptionTouched && descriptionError != null,
                            supportingText = {
                                if (descriptionTouched && descriptionError != null) {
                                    Text(
                                        text = descriptionError!!,
                                        color = MaterialTheme.colorScheme.error, // Changed
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else if (descriptionTouched && description.length >= 3 && descriptionError == null) { // Added no error check
                                    val remainingChars = if (description.length > 50) 0 else 50 - description.length
                                    Text(
                                        text = if (remainingChars > 0)
                                            "$remainingChars characters remaining"
                                        else
                                            "Description is good",
                                        color = MaterialTheme.colorScheme.tertiary, // Changed (Placeholder for success)
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }
            }
            
            // Save Button
            ElevatedButton(
                onClick = {
                    if (validate()) {
                        // If in global mode, make sure a person is selected
                        // If in specific mode, use the preselected person
                        val personId = if (isGlobalMode) selectedPersonId else preselectedPerson?.id
                        // Double-check that we have a valid personId
                        personId?.let { id ->
                            val transaction = Transaction(
                                personId = id,
                                amount = amount.toDouble(),
                                description = description,
                                isCredit = transactionType == "sent" // true for "To Receive", false for "To Pay"
                            )
                            onTransactionAdded(transaction)
                        } ?: run {
                            // This shouldn't happen due to validation, but just in case
                            if (isGlobalMode) {
                                contactError = "Please select a contact"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // Changed
                        shape = MaterialTheme.shapes.medium
                    ),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Changed
                    contentColor = MaterialTheme.colorScheme.onPrimary // Changed
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 4.dp
                ),
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save",
                    tint = MaterialTheme.colorScheme.onPrimary // Changed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Transaction",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
