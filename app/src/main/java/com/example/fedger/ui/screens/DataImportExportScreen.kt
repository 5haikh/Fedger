package com.example.fedger.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fedger.ui.ImportExportState
import com.example.fedger.ui.PersonViewModel
import com.example.fedger.ui.components.EnhancedCard
import com.example.fedger.ui.components.FilePickerWithPermissions
import com.example.fedger.ui.components.StoragePermission
import com.example.fedger.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataImportExportScreen(
    viewModel: PersonViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val importExportState by viewModel.importExportState.collectAsState()
    val scrollState = rememberScrollState()
    
    // File picker for exporting data
    val exportFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            // Take persistable URI permission before using the URI
            StoragePermission.takePersistablePermission(
                context,
                it,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.exportData(context, it)
        }
    }
    
    // Create filename with current date
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val defaultFileName = "fedger_backup_$currentDate.json"
    
    // Function to initiate export (with permission check)
    fun performExport() {
        if (StoragePermission.checkPermissions(context)) {
            exportFilePicker.launch(defaultFileName)
        } else {
            Toast.makeText(context, "Storage permissions needed for export", Toast.LENGTH_SHORT).show()
        }
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
            
            // Export Card
            EnhancedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
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
                        text = "Export Data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    
                    Text(
                        text = "Save your financial data to a file for backup purposes. This will include all your contacts and transactions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGrey,
                        textAlign = TextAlign.Center
                    )
                    
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
                        Text("Export Data")
                    }
                }
            }
            
            // Import Card - Using our new FilePickerWithPermissions component
            EnhancedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                FilePickerWithPermissions(
                    onFilePicked = { uri ->
                        viewModel.importData(context, uri)
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
                            text = "Import Data",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        
                        Text(
                            text = "Load financial data from a backup file. Only import files that you previously exported from Fedger.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGrey,
                            textAlign = TextAlign.Center
                        )
                        
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
                            Text("Import Data")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
} 