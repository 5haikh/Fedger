package com.example.fedger.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fedger.data.EncryptionUtil
import com.example.fedger.model.Credential
import com.example.fedger.model.CredentialType
import com.example.fedger.model.PasswordEntry
import com.example.fedger.ui.PasswordViewModel
import com.example.fedger.ui.components.EnhancedCard
import com.example.fedger.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailsScreen(
    entryId: Int,
    viewModel: PasswordViewModel,
    onEditClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }
    var entry by remember { mutableStateOf<PasswordEntry?>(null) }
    var credentials by remember { mutableStateOf<List<Credential>>(emptyList()) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var confirmResetEncryption by remember { mutableStateOf(false) }
    // Use a manually created coroutine scope
    val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    LaunchedEffect(entryId) {
        viewModel.getEntryWithCredentials(entryId).collect { result ->
            result?.let {
                entry = it.entry
                credentials = it.credentials
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                    Text(
                        text = entry?.title ?: "Loading...",
                        color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!entry?.category.isNullOrBlank()) {
                            Text(
                                text = entry?.category ?: "",
                                color = LightPurple,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepPurple,
                    titleContentColor = TextWhite,
                    actionIconContentColor = TextWhite
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        ) {
        if (entry == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MediumPurple
                )
        } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        EnhancedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    ) {
                                        Text(
                                            text = entry?.title ?: "",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        
                                        if (!entry?.category.isNullOrBlank()) {
                                            Text(
                                                text = "Category: ${entry?.category}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = LightPurple
                                            )
                                }
                                
                                if (!entry?.description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = entry?.description ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextWhite
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Last updated info with icon
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Update,
                                        contentDescription = null,
                                        tint = TextGrey,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(4.dp))
                                    
                                    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                    Text(
                                        text = "Last updated: ${dateFormat.format(Date(entry?.updatedAt ?: 0))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextGrey
                                    )
                                }
                                
                                // Created info with icon
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = TextGrey,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(4.dp))
                                    
                                    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                    Text(
                                        text = "Created: ${dateFormat.format(Date(entry?.createdAt ?: 0))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextGrey
                                    )
                                }
                            }
                        }
                    }
                    
                    // Credentials header
                    item {
                        Text(
                            text = "Credentials",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    
                    // Credentials
                    if (credentials.isEmpty()) {
                        item {
                            EnhancedCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NoEncryption,
                                            contentDescription = null,
                                            tint = TextGrey,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            "No credentials found",
                                            color = TextGrey,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(credentials) { credential ->
                            var showPassword by remember { mutableStateOf(false) }
                            
                            EnhancedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    val credentialType = getCredentialTypeOrDefault(credential.type)
                                    val gradientColors = when (credentialType) {
                                        CredentialType.USERNAME_PASSWORD -> listOf(MediumPurple.copy(alpha = 0.2f), LightPurple.copy(alpha = 0.1f))
                                        CredentialType.PASSWORD_ONLY -> listOf(LightPurple.copy(alpha = 0.2f), DeepPurple.copy(alpha = 0.1f))
                                        CredentialType.PIN -> listOf(MediumPurple.copy(alpha = 0.2f), Color(0xFF6200EA).copy(alpha = 0.1f))
                                        CredentialType.CUSTOM -> listOf(MediumPurple.copy(alpha = 0.2f), DeepPurple.copy(alpha = 0.1f))
                                    }
                                    
                                    // Header row with label and actions
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.horizontalGradient(gradientColors)
                                            )
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Label with appropriate icon
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Select icon based on credential type
                                            val icon = try {
                                                val type = getCredentialTypeOrDefault(credential.type)
                                                
                                                when (type) {
                                                    CredentialType.USERNAME_PASSWORD -> Icons.Default.Person
                                                    CredentialType.PASSWORD_ONLY -> Icons.Default.Password
                                                    CredentialType.PIN -> Icons.Default.Pin
                                                    CredentialType.CUSTOM -> {
                                                        // Fallback to inferring from label for custom types
                                                        getIconFromLabel(credential.label)
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                // Fallback to inferring from label if type can't be parsed
                                                getIconFromLabel(credential.label)
                                            }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        when (credentialType) {
                                                            CredentialType.USERNAME_PASSWORD -> MediumPurple
                                                            CredentialType.PASSWORD_ONLY -> LightPurple
                                                            CredentialType.PIN -> Color(0xFF6200EA)
                                                            CredentialType.CUSTOM -> MediumPurple
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = TextWhite,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.width(12.dp))
                                            
                                            // Use a more descriptive label based on credential type
                                            val credentialType = getCredentialTypeOrDefault(credential.type)
                                            
                                            // If the credential has a display name, use it
                                            // Otherwise, use a default based on the type
                                            val displayLabel = if (credential.displayName.isNotBlank()) {
                                                credential.displayName
                                            } else {
                                                when (credentialType) {
                                                    CredentialType.USERNAME_PASSWORD -> "Login Credentials"
                                                    CredentialType.PASSWORD_ONLY -> "Password"
                                                    CredentialType.PIN -> "PIN Code"
                                                    CredentialType.CUSTOM -> credential.label
                                                }
                                            }
                                            
                                            Text(
                                                text = displayLabel,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                        }
                                        
                                        Row {
                                            // Copy button
                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        
                                                        // Use viewModel to decrypt the value
                                                        val valueToCopy = try {
                                                            viewModel.decryptCredentialValue(credential)
                                                        } catch (e: Exception) {
                                                            // If decryption fails, show error and don't copy
                                                            Toast.makeText(
                                                                context,
                                                                "Failed to decrypt: ${e.message}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            return@IconButton
                                                        }
                                                        
                                                        // For username/password type, ask which value to copy
                                                        val credentialType = getCredentialTypeOrDefault(credential.type)
                                                        
                                                        val finalValueToCopy = if (credentialType == CredentialType.USERNAME_PASSWORD) {
                                                            val parts = valueToCopy.split(":", limit = 2)
                                                            if (parts.size == 2) {
                                                                // Default to copying the password part
                                                                parts[1] 
                                                            } else {
                                                                valueToCopy
                                                            }
                                                        } else {
                                                            valueToCopy
                                                        }
                                                        
                                                        val clipData = ClipData.newPlainText(
                                                            "Password", 
                                                            finalValueToCopy
                                                        )
                                                        clipboardManager.setPrimaryClip(clipData)
                                                        
                                                        // Show toast with security message
                                                        Toast.makeText(
                                                            context, 
                                                            "${credential.label} copied (clears in 30s)", 
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        
                                                        // Clear clipboard after 30 seconds for security (reduced from 60s)
                                                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                            try {
                                                                // Check if our content is still in clipboard
                                                                val currentClip = clipboardManager.primaryClip
                                                                if (currentClip != null && 
                                                                    currentClip.itemCount > 0 && 
                                                                    currentClip.getItemAt(0).text == finalValueToCopy) {
                                                                    // Only clear if it's still our content
                                                                    val emptyClip = ClipData.newPlainText("", "")
                                                                    clipboardManager.setPrimaryClip(emptyClip)
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Clipboard cleared for security",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            } catch (e: Exception) {
                                                                // Failed to clear clipboard
                                                            }
                                                        }, 30000) // Reduced to 30 seconds
                                                    } catch (e: Exception) {
                                                        Toast.makeText(
                                                            context,
                                                            "Could not copy: ${e.message}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(SurfaceLight)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy to Clipboard",
                                                    tint = LightPurple,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            // Toggle visibility button with improved UI
                                            IconButton(
                                                onClick = { showPassword = !showPassword },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(SurfaceLight)
                                            ) {
                                                Icon(
                                                    imageVector = if (showPassword) 
                                                        Icons.Default.Visibility 
                                                    else 
                                                        Icons.Default.VisibilityOff,
                                                    contentDescription = if (showPassword) 
                                                        "Hide Value" 
                                                    else 
                                                        "Show Value",
                                                    tint = LightPurple,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Credential value in a highlighted box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceLight.copy(alpha = 0.7f))
                                            .padding(16.dp)
                                    ) {
                                        Column {
                                            // Handle different credential types with consistent display
                                            val credentialType = getCredentialTypeOrDefault(credential.type)
                                            
                                            // Always try to decrypt the value here for visibility
                                            val decryptedValue = try {
                                                viewModel.decryptCredentialValue(credential)
                                            } catch (e: Exception) {
                                                // Show error when decryption fails with recovery option
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = null,
                                                        tint = TextRed,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Decryption failed",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = TextRed
                                                    )
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    ElevatedButton(
                                                        onClick = { showRecoveryDialog = true },
                                                        colors = ButtonDefaults.elevatedButtonColors(
                                                            containerColor = MediumPurple
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                        modifier = Modifier.padding(start = 8.dp)
                                                    ) {
                                                        Text("Recover", fontSize = 12.sp)
                                                    }
                                                }
                                                "••••••••" // Return masked placeholder
                                            }
                                            
                                            if (showPassword) {
                                                when (credentialType) {
                                                    CredentialType.USERNAME_PASSWORD -> {
                                                        // For username/password type, split and show both fields
                                                        val parts = decryptedValue.split(":", limit = 2)
                                                        
                                                        if (parts.size == 2) {
                                                            Text(
                                                                text = "Username:",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = MediumPurple,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            
                                                            Text(
                                                                text = parts[0],
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                color = TextWhite
                                                            )
                                                            
                                                            Spacer(modifier = Modifier.height(12.dp))
                                                            
                                                            Text(
                                                                text = "Password:",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = MediumPurple,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            
                                                            Text(
                                                                text = parts[1],
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                color = TextWhite
                                                            )
                                                        } else {
                                                            // Fallback if splitting fails
                                                            Text(
                                                                text = decryptedValue,
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                color = TextWhite
                                                            )
                                                        }
                                                    }
                                                    CredentialType.PASSWORD_ONLY -> {
                                                        // For password type
                                                        Text(
                                                            text = "Password:",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = LightPurple,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        
                                                        Text(
                                                            text = decryptedValue,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = TextWhite
                                                        )
                                                    }
                                                    CredentialType.PIN -> {
                                                        // For PIN type
                                                        Text(
                                                            text = "PIN:",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = Color(0xFF6200EA),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        
                                                        Text(
                                                            text = decryptedValue,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = TextWhite,
                                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                            letterSpacing = 2.sp
                                                        )
                                                    }
                                                    CredentialType.CUSTOM -> {
                                                        // For custom type, use the label from the credential
                                                        Text(
                                                            text = "${credential.label}:",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = MediumPurple,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        
                                                        Text(
                                                            text = decryptedValue,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = TextWhite
                                                        )
                                                    }
                                                }
                                            } else {
                                                // When not showing password, just show masked text
                                                when (credentialType) {
                                                    CredentialType.USERNAME_PASSWORD -> {
                                                        // Split username/password display even when masked
                                                        val parts = decryptedValue.split(":", limit = 2)
                                                        
                                                        if (parts.size == 2) {
                                                            Text(
                                                                text = "Username:",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = MediumPurple,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            
                                                            // Show username but mask password
                                                            Text(
                                                                text = parts[0],
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                color = TextWhite
                                                            )
                                                            
                                                            Spacer(modifier = Modifier.height(12.dp))
                                                            
                                                            Text(
                                                                text = "Password:",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = MediumPurple,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            
                                                            Text(
                                                                text = "••••••••",
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                color = TextWhite.copy(alpha = 0.7f),
                                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                            )
                                                        } else {
                                                            Text(
                                                                text = "••••••••",
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                color = TextWhite.copy(alpha = 0.7f),
                                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                            )
                                                        }
                                                    }
                                                    else -> {
                                                        // Default mask display for other types
                                                        Text(
                                                            text = "••••••••",
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = TextWhite.copy(alpha = 0.7f),
                                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    if (credential.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // Notes with distinct styling
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SurfaceLight.copy(alpha = 0.3f))
                                                .padding(12.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Notes",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MediumPurple,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                
                                                Spacer(modifier = Modifier.height(4.dp))
                                                
                                                Text(
                                                    text = credential.notes,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextGrey
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Actions
                    item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                                .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ElevatedButton(
                                onClick = { entry?.id?.let { onEditClick(it) } },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MediumPurple,
                            contentColor = TextWhite
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Password",
                            tint = TextWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    ElevatedButton(
                        onClick = { confirmDelete = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = TextRed.copy(alpha = 0.9f),
                            contentColor = TextWhite
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Password",
                            tint = TextWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.titleMedium
                        )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Confirm Deletion", color = TextWhite) },
            text = { Text("Are you sure you want to delete '${entry?.title}'? This action cannot be undone.", color = TextWhite) },
            confirmButton = {
                Button(
                    onClick = {
                        entry?.let { viewModel.deletePasswordEntry(it) }
                        confirmDelete = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextRed,
                        contentColor = TextWhite
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { confirmDelete = false },
                    border = BorderStroke(1.dp, TextWhite.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextWhite
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Cancel")
                }
            },
            containerColor = CardBackground
        )
    }
    
    // Recovery dialog
    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Credential Recovery", color = TextWhite)
                }
            },
            text = {
                Column {
                    Text("One or more credentials could not be decrypted. This may happen if your device was reset or restored from backup.", color = TextWhite)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Try Recovery: Uses backup decryption methods", color = TextWhite)
                    Text("• Reset Key: Last resort, may cause data loss", color = TextRed)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRecoveryDialog = false
                        coroutineScope.launch {
                            viewModel.attemptCredentialRecovery().collect { count ->
                                if (count > 0) {
                                    Toast.makeText(context, 
                                        "Recovery successful: $count credentials fixed", 
                                        Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, 
                                        "Could not recover any credentials", 
                                        Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MediumPurple)
                ) {
                    Text("Try Recovery")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = { showRecoveryDialog = false }
                    ) {
                        Text("Cancel", color = TextWhite)
                    }
                    
                    Button(
                        onClick = { 
                            showRecoveryDialog = false
                            confirmResetEncryption = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TextRed)
                    ) {
                        Text("Reset Key")
                    }
                }
            },
            containerColor = CardBackground
        )
    }
    
    // Reset encryption confirmation
    if (confirmResetEncryption) {
        AlertDialog(
            onDismissRequest = { confirmResetEncryption = false },
            title = { Text("Reset Encryption Key?", color = TextRed) },
            text = { 
                Text(
                    "WARNING: This is irreversible and may cause permanent data loss for unrecoverable credentials. Continue?", 
                    color = TextWhite
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = viewModel.resetEncryptionKey()
                        if (result) {
                            Toast.makeText(
                                context, 
                                "Encryption key was reset. Recovery mode enabled.", 
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        confirmResetEncryption = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TextRed)
                ) {
                    Text("Reset Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmResetEncryption = false }) {
                    Text("Cancel", color = TextWhite)
                }
            },
            containerColor = CardBackground
        )
    }
}

// Helper functions for credential type handling
private fun getCredentialTypeOrDefault(typeString: String?): CredentialType {
    return try {
        if (typeString.isNullOrEmpty()) {
            CredentialType.CUSTOM
        } else {
            CredentialType.valueOf(typeString)
        }
    } catch (e: Exception) {
        CredentialType.CUSTOM
    }
}

private fun getIconFromLabel(label: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        label.contains("user", ignoreCase = true) ||
        label.contains("email", ignoreCase = true) -> Icons.Default.Person
        label.contains("pass", ignoreCase = true) -> Icons.Default.Password
        label.contains("pin", ignoreCase = true) ||
        label.contains("code", ignoreCase = true) -> Icons.Default.Pin
        label.contains("key", ignoreCase = true) -> Icons.Default.Key
        label.contains("token", ignoreCase = true) -> Icons.Default.Token
        label.contains("card", ignoreCase = true) -> Icons.Default.CreditCard
        else -> Icons.Default.VpnKey
    }
} 