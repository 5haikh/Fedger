package com.example.fedger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import com.example.fedger.model.Person
import com.example.fedger.ui.components.EnhancedCard
// Removed direct theme imports like com.example.fedger.ui.theme.*
// import com.example.fedger.ui.theme.TextWhite // Example, will be removed by tool if not used

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonScreen(
    onPersonAdded: (Person) -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Validation states
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    
    // Add validation touch state to track if fields have been interacted with
    var nameTouched by remember { mutableStateOf(false) }
    var phoneTouched by remember { mutableStateOf(false) }
    
    // Remove avatar color selection and keep only the scroll state
    val scrollState = rememberScrollState()
    
    // Validate name function for real-time validation
    fun validateName(value: String): String? {
        return when {
            value.isBlank() -> "Name is required"
            value.length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
    }
    
    // Validate phone function for real-time validation
    fun validatePhone(value: String): String? {
        if (value.isBlank()) return null // Phone is optional
        
        // Check if the phone number contains only digits, +, spaces, and hyphens
        val cleanValue = value.replace(Regex("[\\s-]"), "")
        val phoneRegex = Regex("^[+]?[0-9]{10,15}$")
        
        return if (!cleanValue.matches(phoneRegex)) {
            "Enter a valid phone number"
        } else {
            null
        }
    }
    
    // Validate function
    fun validate(): Boolean {
        // Always mark fields as touched when submitting
        nameTouched = true
        phoneTouched = true
        
        // Validate all fields
        val nameValidation = validateName(name)
        val phoneValidation = validatePhone(phone)
        
        // Update error states
        nameError = nameValidation
        phoneError = phoneValidation
        
        // Return true if all validations pass
        return nameValidation == null && phoneValidation == null
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
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary // Ensure nav icon also uses onPrimary
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
                text = "Add New Contact",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground, // Changed
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            
            // Main Card with scrollable content using our enhanced card
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
                        // Person Details Header
                        Text(
                            text = "Contact Details",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface, // Changed
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Add a new contact to track money flow with.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant // Changed
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Name Field
                        Text(
                            text = "Name",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface, // Changed
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.semantics { contentDescription = "Name field label" }
                        )
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameTouched = true
                                nameError = validateName(it)
                            },
                            placeholder = { Text("Enter contact's name", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }, // Changed
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // Changed
                                focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                                cursorColor = MaterialTheme.colorScheme.primary, // Changed
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), // Changed
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) // Changed
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            isError = nameTouched && nameError != null,
                            supportingText = {
                                if (nameTouched && nameError != null) {
                                    Text(
                                        text = nameError!!,
                                        color = MaterialTheme.colorScheme.error, // Changed
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                        
                        // Phone Field
                        Text(
                            text = "Phone (Optional)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface, // Changed
                            fontWeight = FontWeight.Medium
                        )
                        
                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                phoneTouched = true
                                phoneError = validatePhone(it)
                            },
                            placeholder = { Text("Enter phone number", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }, // Changed
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary, // Changed
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // Changed
                                focusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Changed
                                cursorColor = MaterialTheme.colorScheme.primary, // Changed
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), // Changed
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) // Changed
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            isError = phoneTouched && phoneError != null,
                            supportingText = {
                                if (phoneTouched && phoneError != null) {
                                    Text(
                                        text = phoneError!!,
                                        color = MaterialTheme.colorScheme.error, // Changed
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else if (phoneTouched && phone.isNotBlank() && phoneError == null) { // Added phone.isNotBlank and phoneError == null
                                    Text(
                                        text = "Valid phone number format",
                                        color = MaterialTheme.colorScheme.tertiary, // Changed (Placeholder for success color)
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                        
                        // Notes Field
                        Text(
                            text = "Notes (Optional)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface, // Changed
                            fontWeight = FontWeight.Medium
                        )
                        
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("Add any notes about this contact", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }, // Changed
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
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }
            }
            
            // Save Button - Using an elevated button for better emphasis
            ElevatedButton(
                onClick = {
                    if (validate()) {
                        val newPerson = Person(
                            id = 0, // Will be replaced by Room
                            name = name.trim(),
                            phoneNumber = phone.trim(),
                            address = notes.trim()
                        )
                        onPersonAdded(newPerson)
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
                    text = "Save Contact",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
