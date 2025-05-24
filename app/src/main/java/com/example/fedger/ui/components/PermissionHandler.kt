package com.example.fedger.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * A unified permission handler for storage operations that works across Android versions
 */
object StoragePermission {
    private const val TAG = "StoragePermission"
    
    // Check if storage permissions are granted based on Android version
    fun checkPermissions(context: Context): Boolean {
        // On Android 11+ (API 30+), we use Storage Access Framework and don't need runtime permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (Q) - READ is sufficient for most operations with scoped storage
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 9 and below - need both READ and WRITE
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    // Get the permission(s) we need to request based on Android version
    fun getRequiredPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ doesn't need runtime permissions for Storage Access Framework
            emptyList()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }
    
    // Take a persistable URI permission so we can access it later
    fun takePersistablePermission(context: Context, uri: Uri, flags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION) {
        try {
            context.contentResolver.takePersistableUriPermission(uri, flags)
            Log.d(TAG, "Took persistable permission for URI: $uri")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to take persistable permission for URI: $uri", e)
        }
    }
}

/**
 * Composable to handle storage permissions with a consistent UI
 * 
 * @param onPermissionGranted Callback when permission is granted
 * @param onPermissionDenied Callback when permission is denied
 * @param onShowRationale Callback to show rationale (optional, default UI provided)
 */
@Composable
fun RequestStoragePermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {},
    onShowRationale: @Composable () -> Unit = { DefaultRationaleUI(onPermissionDenied) }
) {
    val context = LocalContext.current
    val requiredPermissions = StoragePermission.getRequiredPermissions()
    
    // Skip permission check on Android 11+ since we use Storage Access Framework
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        LaunchedEffect(Unit) {
            onPermissionGranted()
        }
        return
    }
    
    // Track permission state
    var hasCheckedPermission by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(StoragePermission.checkPermissions(context)) }
    var showRationale by remember { mutableStateOf(false) }
    
    // Handle multi-permission requests for older Android versions
    val multiplePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        permissionGranted = allGranted
        
        if (allGranted) {
            onPermissionGranted()
        } else {
            showRationale = true
        }
        
        hasCheckedPermission = true
    }
    
    // Handle single permission request for newer Android versions
    val singlePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        
        if (isGranted) {
            onPermissionGranted()
        } else {
            showRationale = true
        }
        
        hasCheckedPermission = true
    }
    
    // Effect to check permissions on first composition
    LaunchedEffect(Unit) {
        if (!permissionGranted && !hasCheckedPermission) {
            if (requiredPermissions.size == 1) {
                singlePermissionLauncher.launch(requiredPermissions.first())
            } else if (requiredPermissions.isNotEmpty()) {
                multiplePermissionLauncher.launch(requiredPermissions.toTypedArray())
            } else {
                // No permissions needed
                onPermissionGranted()
            }
        } else if (permissionGranted) {
            onPermissionGranted()
        }
    }
    
    if (showRationale) {
        onShowRationale()
    }
}

/**
 * Default UI for showing permission rationale
 */
@Composable
fun DefaultRationaleUI(onDismiss: () -> Unit) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Storage Permission Required") },
        text = {
            Column {
                Text(
                    "Storage permission is needed to import and export files. " +
                    "Without this permission, you cannot backup or restore your data."
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Please grant the permission in the app settings."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Open app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                    onDismiss()
                }
            ) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * A Composable that wraps file picker functionality with permission handling
 * 
 * @param onFilePicked Callback with the selected URI
 * @param mimeTypes The MIME types to filter files by
 * @param modifier Modifier for the content
 * @param content The content to display; will receive a startFilePicker function to trigger the picker
 */
@Composable
fun FilePickerWithPermissions(
    onFilePicked: (Uri) -> Unit,
    mimeTypes: Array<String> = arrayOf("application/json"),
    modifier: Modifier = Modifier,
    content: @Composable (startFilePicker: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // Take a persistable permission for the URI
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, 
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                Log.d("FilePickerWithPermissions", "Took persistable permission for URI: $it")
            } catch (e: Exception) {
                Log.e("FilePickerWithPermissions", "Failed to take persistable permission: ${e.message}")
                // Continue anyway, as we might still be able to read the file this time
            }
            
            onFilePicked(it)
        }
    }
    
    // Start file picker function
    fun startFilePicker() {
        // On all Android versions, we now use the Storage Access Framework which handles permissions internally
        filePickerLauncher.launch(mimeTypes)
    }
    
    Box(modifier = modifier) {
        content(::startFilePicker)
    }
} 