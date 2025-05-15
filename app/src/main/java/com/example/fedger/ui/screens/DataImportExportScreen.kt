package com.example.fedger.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.example.fedger.FedgerApplication
import com.example.fedger.ui.ImportExportState
import com.example.fedger.ui.PersonViewModel
import com.example.fedger.ui.components.EnhancedCard
import com.example.fedger.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DataImportExportScreen(
    viewModel: PersonViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val importExportState by viewModel.importExportState.collectAsState()
    val scrollState = rememberScrollState()
    
    // For permission handling
    val hasStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // For Android 13+ we need different permissions
        val readMediaPermission = rememberPermissionState(permission = Manifest.permission.READ_MEDIA_IMAGES)
        readMediaPermission.status.isGranted
    } else {
        // For Android 12 and below we need storage permissions
        val readStoragePermission = rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
        val writeStoragePermission = rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)
        readStoragePermission.status.isGranted && writeStoragePermission.status.isGranted
    }
    
    // File pickers
    val exportFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.exportData(context, it)
        }
    }
    
    val importFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // Take a persistable URI permission to allow app to access document later
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.importData(context, it)
        }
    }
    
    // Create filename with current date
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val defaultFileName = "fedger_backup_$currentDate.json"
    
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Import & Export Data",
                style = MaterialTheme.typography.headlineSmall,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            
            // Status card that appears only when there's an operation in progress
            AnimatedVisibility(
                visible = importExportState !is ImportExportState.Idle,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                EnhancedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (val state = importExportState) {
                            is ImportExportState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = LightPurple
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextWhite,
                                    textAlign = TextAlign.Center
                                )
                            }
                            is ImportExportState.Success -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
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
                            is ImportExportState.Error -> {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Error",
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
                            else -> { /* Idle state, nothing to show */ }
                        }
                    }
                }
            }
            
            // Main content card
            EnhancedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Export Section
                    Text(
                        text = "Export Data",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Export all your contacts and transactions data to a JSON file. " +
                               "You can use this file to restore your data later or transfer to another device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGrey
                    )
                    
                    Button(
                        onClick = { 
                            if (hasStoragePermission || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                exportFilePicker.launch(defaultFileName)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Storage permission required for export",
                                    Toast.LENGTH_LONG
                                ).show()
                                openAppSettings(context)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MediumPurple,
                            contentColor = TextWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Export",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export Data", 
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = TextGrey.copy(alpha = 0.3f)
                    )
                    
                    // Import Section
                    Text(
                        text = "Import Data",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Import contacts and transactions from a previously exported JSON file. " +
                               "This will add all imported data to your existing records.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGrey
                    )
                    
                    // Warning message - flattened structure
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardBackground)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = AccentAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Imported data will be added to your existing records. " +
                                  "Duplicate contacts may be created if they already exist.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGrey
                        )
                    }
                    
                    Button(
                        onClick = { 
                            if (hasStoragePermission || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                importFilePicker.launch(arrayOf("application/json"))
                            } else {
                                Toast.makeText(
                                    context,
                                    "Storage permission required for import",
                                    Toast.LENGTH_LONG
                                ).show()
                                openAppSettings(context)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MediumPurple,
                            contentColor = TextWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Import",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Import Data", 
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Helper function to open app settings
private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    startActivity(context, intent, null)
} 