package com.example.fedger.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fedger.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun AppSelectionScreen(
    onLedgerSelected: () -> Unit,
    onPasswordManagerSelected: () -> Unit
) {
    // Animation states
    var isReady by remember { mutableStateOf(false) }
    
    // Start animations after a short delay
    LaunchedEffect(Unit) {
        delay(100)
        isReady = true
    }
    
    // Create pulsing animation for the logo
    val pulsateAnimation = rememberInfiniteTransition(label = "pulsate")
    val scale by pulsateAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Create glow animation for the logo
    val glowAnimation = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepPurple,
                        DeepPurple.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        // Background decoration elements
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .graphicsLayer { alpha = 0.07f }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MediumPurple,
                            Color.Transparent
                        )
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .graphicsLayer { alpha = 0.07f }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PurpleHighlight,
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // App logo and title
            AnimatedVisibility(
                visible = isReady,
                enter = fadeIn(animationSpec = tween(1000)) + 
                        slideInVertically(
                            animationSpec = tween(1000, easing = EaseOutQuart),
                            initialOffsetY = { -40 }
                        )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 60.dp)
                ) {
                    // Logo circle with glow
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        // Glow effect
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(scale * 1.2f)
                                .blur(radius = 20.dp)
                                .alpha(glowAlpha)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MediumPurple,
                                            Color.Transparent,
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        
                        // Main logo container
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(scale)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = CircleShape,
                                    spotColor = MediumPurple
                                )
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MediumPurple,
                                            MediumPurple.copy(alpha = 0.9f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "₹",
                                color = TextWhite,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "FEDGER",
                        color = TextWhite,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Your digital finance & security companion",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextWhite.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // App selection cards
            AnimatedVisibility(
                visible = isReady,
                enter = fadeIn(animationSpec = tween(1500)) + 
                        slideInVertically(
                            animationSpec = tween(1500, easing = EaseOutCubic),
                            initialOffsetY = { 100 }
                        )
            ) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SELECT AN APP",
                        color = TextWhite.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    // App selection cards
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Ledger card
                        EnhancedAppSelectionCard(
                            title = "Ledger",
                            description = "Track your financial transactions easily",
                            icon = Icons.Default.CreditCard,
                            primaryColor = MediumPurple,
                            secondaryColor = MediumPurple.copy(alpha = 0.7f),
                            onClick = onLedgerSelected
                        )
                        
                        // Password Manager card
                        EnhancedAppSelectionCard(
                            title = "Password Manager",
                            description = "Securely store & manage your passwords",
                            icon = Icons.Default.Lock,
                            primaryColor = PurpleHighlight,
                            secondaryColor = PurpleHighlight.copy(alpha = 0.7f),
                            onClick = onPasswordManagerSelected
                        )
                    }
                }
            }
            
            // Footer/version
            AnimatedVisibility(
                visible = isReady,
                enter = fadeIn(animationSpec = tween(2000))
            ) {
                Text(
                    text = "v1.0",
                    color = TextWhite.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun EnhancedAppSelectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    // Card hover/press animations
    var isPressed by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100), 
        label = "cardScale"
    )
    
    val coroutineScope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .scale(cardScale)
            .fillMaxWidth()
            .height(90.dp)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = primaryColor
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        CardBackground,
                        CardBackground.copy(alpha = 0.95f)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = {
                        onClick()
                    }
                )
            }
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.6f),
                        primaryColor.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.3f),
                                primaryColor.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.5f),
                                primaryColor.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = primaryColor,
                    modifier = Modifier.size(30.dp)
                )
            }
            
            // Text content
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGrey,
                    maxLines = 1
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Arrow indicator
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = secondaryColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
} 