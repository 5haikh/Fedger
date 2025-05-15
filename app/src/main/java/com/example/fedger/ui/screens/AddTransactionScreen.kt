package com.example.fedger.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import com.example.fedger.ui.components.GradientSurface
import com.example.fedger.ui.theme.*

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
        viewModel!!.persons.collectAsState(initial = emptyList()).value
    } else {
        listOf(preselectedPerson!!)
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
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate back"
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Transaction",
                style = MaterialTheme.typography.headlineSmall,
                color = TextWhite,
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
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = if (isGlobalMode) 
                                "Add a new transaction with any contact."
                            else 
                                "Add a new transaction with ${preselectedPerson!!.name}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGrey
                        )
                        
                        // Contact Selection - Only shown in global mode
                        if (isGlobalMode) {
                            Text(
                                text = "Select Contact",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextWhite,
                                fontWeight = FontWeight.Medium
                            )
                            
                            // For person selection
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 4.dp)
                            ) {
                                OutlinedTextField(
                                    value = selectedContact?.name ?: "",
                                    onValueChange = { /* Read only */ },
                                    placeholder = { Text("Select a contact", color = TextGrey) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MediumPurple,
                                        unfocusedBorderColor = LightPurple.copy(alpha = 0.3f),
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite,
                                        cursorColor = MediumPurple,
                                        focusedContainerColor = CardBackground.copy(alpha = 0.2f),
                                        unfocusedContainerColor = CardBackground.copy(alpha = 0.1f),
                                        disabledTextColor = TextWhite,
                                        disabledBorderColor = LightPurple.copy(alpha = 0.3f),
                                        disabledContainerColor = CardBackground.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expanded = true }
                                        .semantics { 
                                            contentDescription = "Contact selection dropdown"
                                            role = Role.DropdownList
                                        },
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { expanded = true }) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Expand contact dropdown",
                                                tint = LightPurple
                                            )
                                        }
                                    },
                                    isError = contactError != null,
                                    supportingText = {
                                        if (contactError != null) {
                                            Text(
                                                text = contactError!!,
                                                color = TextRed,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        } else if (selectedContact != null) {
                                            Text(
                                                text = "Selected: ${selectedContact.name}",
                                                color = Color.Green.copy(alpha = 0.8f),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    },
                                    shape = MaterialTheme.shapes.medium,
                                    enabled = false
                                )
                                
                                // Improved dropdown menu with better styling
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .background(SurfaceLight)
                                        .height(350.dp)
                                ) {
                                    // Search bar at top of dropdown
                                    var contactSearchQuery by remember { mutableStateOf("") }
                                    
                                    OutlinedTextField(
                                        value = contactSearchQuery,
                                        onValueChange = { contactSearchQuery = it },
                                        placeholder = { Text("Search contacts", color = TextGrey) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MediumPurple,
                                            unfocusedBorderColor = LightPurple.copy(alpha = 0.3f),
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite,
                                            cursorColor = MediumPurple,
                                            focusedContainerColor = CardBackground.copy(alpha = 0.2f),
                                            unfocusedContainerColor = CardBackground.copy(alpha = 0.1f)
                                        ),
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search contacts",
                                                tint = LightPurple
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        singleLine = true,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    
                                    // Add separator
                                    Divider(
                                        color = LightPurple.copy(alpha = 0.3f),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                    )
                                    
                                    // Filter contacts by search query
                                    val filteredContacts = allContacts.filter { person -> 
                                        person.name.contains(contactSearchQuery, ignoreCase = true) || 
                                        person.phoneNumber.contains(contactSearchQuery, ignoreCase = true)
                                    }
                                    
                                    if (filteredContacts.isEmpty()) {
                                        Text(
                                            text = "No contacts found",
                                            color = TextGrey,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    } else {
                                        filteredContacts.forEach { person ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(
                                                            text = person.name,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = if (selectedPersonId == person.id) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                        if (person.phoneNumber.isNotEmpty()) {
                                                            Text(
                                                                text = person.phoneNumber,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = TextGrey
                                                            )
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    selectedPersonId = person.id
                                                    contactError = null
                                                    expanded = false
                                                },
                                                leadingIcon = {
                                                    // Show an avatar/icon
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(MediumPurple.copy(alpha = 0.2f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = person.name.firstOrNull()?.toString() ?: "?",
                                                            color = TextWhite
                                                        )
                                                    }
                                                },
                                                trailingIcon = if (selectedPersonId == person.id) {
                                                    {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected",
                                                            tint = MediumPurple
                                                        )
                                                    }
                                                } else null,
                                                modifier = Modifier.background(
                                                    if (selectedPersonId == person.id) MediumPurple.copy(alpha = 0.1f) else Color.Transparent
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Transaction Type Selection
                        Text(
                            text = "Transaction Type",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Segmented button style toggle
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(CardBackground.copy(alpha = 0.2f))
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
                                                MediumPurple.copy(alpha = 0.8f)
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
                                            color = TextWhite,
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
                                                MediumPurple.copy(alpha = 0.8f)
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
                                            color = TextWhite,
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
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                        
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { 
                                amount = it
                                amountTouched = true
                                amountError = validateAmount(it)
                            },
                            placeholder = { Text("Enter amount", color = TextGrey) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MediumPurple,
                                unfocusedBorderColor = LightPurple.copy(alpha = 0.3f),
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                cursorColor = MediumPurple,
                                focusedContainerColor = CardBackground.copy(alpha = 0.2f),
                                unfocusedContainerColor = CardBackground.copy(alpha = 0.1f)
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            isError = amountTouched && amountError != null,
                            supportingText = {
                                if (amountTouched && amountError != null) {
                                    Text(
                                        text = amountError!!,
                                        color = TextRed,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else if (amountTouched) {
                                    Text(
                                        text = "Valid amount",
                                        color = Color.Green.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            prefix = { Text("₹", fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                        
                        // Description Field
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                        
                        OutlinedTextField(
                            value = description,
                            onValueChange = { 
                                description = it
                                descriptionTouched = true
                                descriptionError = validateDescription(it)
                            },
                            placeholder = { Text("What's this transaction for?", color = TextGrey) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MediumPurple,
                                unfocusedBorderColor = LightPurple.copy(alpha = 0.3f),
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                cursorColor = MediumPurple,
                                focusedContainerColor = CardBackground.copy(alpha = 0.2f),
                                unfocusedContainerColor = CardBackground.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            isError = descriptionTouched && descriptionError != null,
                            supportingText = {
                                if (descriptionTouched && descriptionError != null) {
                                    Text(
                                        text = descriptionError!!,
                                        color = TextRed,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else if (descriptionTouched && description.length >= 3) {
                                    val remainingChars = if (description.length > 50) 0 else 50 - description.length
                                    Text(
                                        text = if (remainingChars > 0) 
                                            "$remainingChars characters remaining" 
                                        else 
                                            "Description is good",
                                        color = Color.Green.copy(alpha = 0.8f),
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
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MediumPurple,
                    contentColor = TextWhite
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save",
                    tint = TextWhite
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
