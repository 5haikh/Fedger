package com.example.fedger.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fedger.ui.ImportExportState
import com.example.fedger.ui.PasswordViewModel
import com.example.fedger.ui.components.EnhancedCard
import com.example.fedger.ui.components.FilePickerWithPermissions
import com.example.fedger.ui.components.StoragePermission
import com.example.fedger.ui.theme.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordImportExportScreen(
    viewModel: PasswordViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val importExportState by viewModel.importExportState.collectAsStateWithLifecycle()
    
    // Add state to track if we should use decrypted values
    var useDecryptedValues by remember { mutableStateOf(true) }
    
    // Define file pickers using the system file pickers
    val exportFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            uri?.let {
                try {
                    // Take persistable permission for the URI
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    
                    // Pass the useDecryptedValues flag to the export function - always true by default
                    viewModel.exportPasswords(context, it, useDecryptedValues)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Failed to take permission: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    )
    
    // Remember if we should show security warning dialog
    var showSecurityWarning by remember { mutableStateOf(false) }
    
    fun performExport() {
        // Check permissions before exporting
        if (StoragePermission.checkPermissions(context)) {
            showSecurityWarning = true
        } else {
            Toast.makeText(context, "Storage permissions needed for export", Toast.LENGTH_SHORT).show()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Password Import/Export",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    ) 
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Export Card
                EnhancedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MediumPurple.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                tint = TextWhite,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        Text(
                            text = "Export Passwords",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        
                        Text(
                            text = "Save your password data to a file for backup purposes. " +
                                  "The exported file will be encrypted but should still be kept secure.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGrey,
                            textAlign = TextAlign.Center
                        )
                        
                        // Security warning
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFFB74D), // Orange warning color
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Keep exported files in a secure location",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFFB74D)
                            )
                        }
                        
                        // Add a switch for exporting decrypted values
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceLight.copy(alpha = 0.2f))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Export decrypted values",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = if (useDecryptedValues) "ON - Passwords will be readable" else "OFF - Passwords will be encrypted",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (useDecryptedValues) Color(0xFFFFB74D) else TextGrey
                                )
                            }
                            
                            Switch(
                                checked = useDecryptedValues,
                                onCheckedChange = { useDecryptedValues = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MediumPurple,
                                    checkedTrackColor = MediumPurple.copy(alpha = 0.5f),
                                    uncheckedThumbColor = TextGrey,
                                    uncheckedTrackColor = CardBackground
                                )
                            )
                        }
                        
                        Button(
                            onClick = { performExport() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MediumPurple
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Passwords")
                        }
                    }
                }
                
                // Import Card - Using our new FilePickerWithPermissions component
                EnhancedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    FilePickerWithPermissions(
                        onFilePicked = { uri ->
                            viewModel.importPasswords(context, uri, useDecryptedValues)
                        },
                        mimeTypes = arrayOf("application/json"),
                        modifier = Modifier.fillMaxWidth()
                    ) { startFilePicker ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(LightPurple.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = TextWhite,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            Text(
                                text = "Import Passwords",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            
                            Text(
                                text = "Load password data from a backup file. " +
                                      "This will add any new passwords found in the backup file.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGrey,
                                textAlign = TextAlign.Center
                            )
                            
                            // Add a switch for importing decrypted values
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceLight.copy(alpha = 0.2f))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Import expects decrypted values",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = if (useDecryptedValues) "ON - File contains readable passwords" else "OFF - File contains encrypted passwords",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (useDecryptedValues) Color(0xFFFFB74D) else TextGrey
                                    )
                                }
                                
                                Switch(
                                    checked = useDecryptedValues,
                                    onCheckedChange = { useDecryptedValues = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = LightPurple,
                                        checkedTrackColor = LightPurple.copy(alpha = 0.5f),
                                        uncheckedThumbColor = TextGrey,
                                        uncheckedTrackColor = CardBackground
                                    )
                                )
                            }
                            
                            Button(
                                onClick = { startFilePicker() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LightPurple
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Import Passwords")
                            }
                        }
                    }
                }
                
                // Status and progress section - show while operations are in progress
                when (val state = importExportState) {
                    is ImportExportState.Loading -> {
                        EnhancedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = MediumPurple
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextWhite,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    is ImportExportState.Success -> {
                        EnhancedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = TextGreen,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextWhite,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    is ImportExportState.Error -> {
                        EnhancedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = TextRed,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextWhite,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        // Idle state, no status to show
                    }
                }
                
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    // Security warning dialog
    if (showSecurityWarning) {
        AlertDialog(
            onDismissRequest = { showSecurityWarning = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Export Security Warning",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite
                    )
                }
            },
            text = {
                Text(
                    text = if (useDecryptedValues) {
                        "You've enabled 'Export decrypted values'. This means your passwords will be readable as plain text in the exported file.\n\nThis is a SERIOUS SECURITY RISK. Only proceed if you understand the implications and will secure the exported file appropriately."
                    } else {
                        "Your passwords will be exported in an encrypted format, but anyone with access to the file could still attempt to decrypt it.\n\nMake sure to keep this file in a secure location."
                    },
                    color = TextWhite
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSecurityWarning = false
                        exportFilePicker.launch("fedger_passwords.json")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (useDecryptedValues) Color(0xFFEF5350) else MediumPurple
                    )
                ) {
                    Text("I Understand, Export")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSecurityWarning = false }
                ) {
                    Text("Cancel", color = TextWhite)
                }
            },
            containerColor = CardBackground,
            shape = MaterialTheme.shapes.large
        )
    }
} 