package com.example.fedger.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fedger.ui.navigation.Screen
import kotlinx.coroutines.delay

/**
 * An enhanced component for switching between Ledger and Password Manager apps
 * with a toggle switch design
 */
@Composable
fun AppSwitcher(
    navController: NavController,
    currentApp: String,
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer // Changed
) {
    // Use the new enhanced toggle style app switcher
    EnhancedToggleAppSwitcher(
        navController = navController,
        currentApp = currentApp,
        tint = tint
    )
}

/**
 * An enhanced toggle switch implementation for switching between the Ledger and Password Manager apps
 * with improved animations and visual effects
 */
@Composable
fun EnhancedToggleAppSwitcher(
    navController: NavController,
    currentApp: String,
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer // Changed
) {
    val isLedgerSelected = currentApp == "Ledger"
    val isPasswordManagerSelected = currentApp == "Password Manager"
    
    // Pulse animation for the whole toggle
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Remember if user interacted with the toggle
    var isInteracted by remember { mutableStateOf(false) }
    
    // Auto reset interaction state after delay
    LaunchedEffect(isInteracted) {
        if (isInteracted) {
            delay(1500)
            isInteracted = false
        }
    }
    
    Box(
        modifier = Modifier
            .scale(if (isInteracted) 1.1f else scale)
            .padding(8.dp)
            .height(44.dp)
            .width(110.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = if (isLedgerSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary // Changed
            )
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant) // Changed
            .border(
                width = 1.5.dp,
                color = if (isLedgerSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f), // Changed
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        // Background tracks for toggle
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ledger side
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (isLedgerSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent, // Changed
                        shape = RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp)
                    )
                    .clickable {
                        if (!isLedgerSelected) {
                            isInteracted = true
                            navController.navigate(Screen.PersonList.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Ledger",
                    tint = if (isLedgerSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), // Changed
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer {
                            alpha = if (isLedgerSelected) 1f else 0.7f
                            scaleX = if (isLedgerSelected) 1.1f else 0.9f
                            scaleY = if (isLedgerSelected) 1.1f else 0.9f
                        }
                )
            }
            
            // Password Manager side
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (isPasswordManagerSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else Color.Transparent, // Changed
                        shape = RoundedCornerShape(topEnd = 22.dp, bottomEnd = 22.dp)
                    )
                    .clickable {
                        if (!isPasswordManagerSelected) {
                            isInteracted = true
                            navController.navigate(Screen.PasswordList.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password Manager",
                    tint = if (isPasswordManagerSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), // Changed
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer {
                            alpha = if (isPasswordManagerSelected) 1f else 0.7f
                            scaleX = if (isPasswordManagerSelected) 1.1f else 0.9f
                            scaleY = if (isPasswordManagerSelected) 1.1f else 0.9f
                        }
                )
            }
        }
        
        // Sliding indicator animation
        val transition = updateTransition(
            targetState = isLedgerSelected,
            label = "toggle_transition"
        )
        
        val offsetX by transition.animateDp(
            transitionSpec = {
                tween(durationMillis = 400, easing = FastOutSlowInEasing)
            },
            label = "toggle_offset"
        ) { isLedger ->
            if (isLedger) 0.dp else 55.dp
        }
        
        // Sliding indicator with enhanced effects
        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .size(width = 55.dp, height = 44.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (isLedgerSelected) 22.dp else 0.dp,
                        bottomStart = if (isLedgerSelected) 22.dp else 0.dp,
                        topEnd = if (isPasswordManagerSelected) 22.dp else 0.dp,
                        bottomEnd = if (isPasswordManagerSelected) 22.dp else 0.dp
                    )
                )
                .background(
                    color = if (isLedgerSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f), // Changed
                    shape = RoundedCornerShape(
                        topStart = if (isLedgerSelected) 22.dp else 0.dp,
                        bottomStart = if (isLedgerSelected) 22.dp else 0.dp,
                        topEnd = if (isPasswordManagerSelected) 22.dp else 0.dp,
                        bottomEnd = if (isPasswordManagerSelected) 22.dp else 0.dp
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isLedgerSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), // Changed
                    shape = RoundedCornerShape(
                        topStart = if (isLedgerSelected) 22.dp else 0.dp,
                        bottomStart = if (isLedgerSelected) 22.dp else 0.dp,
                        topEnd = if (isPasswordManagerSelected) 22.dp else 0.dp,
                        bottomEnd = if (isPasswordManagerSelected) 22.dp else 0.dp
                    )
                )
        )
    }
}

@Composable
private fun AppSwitcherItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)) // Changed
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer, // Changed
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface, // Changed (assuming DropdownMenu is on a surface)
                    fontWeight = if (isPressed) FontWeight.Bold else FontWeight.Normal
                )
            }
        },
        onClick = onClick
    )
} 