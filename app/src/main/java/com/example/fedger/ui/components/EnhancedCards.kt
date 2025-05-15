package com.example.fedger.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fedger.ui.theme.CardBackground
import com.example.fedger.ui.theme.LightPurple
import com.example.fedger.ui.theme.MediumPurple
import com.example.fedger.ui.theme.SurfaceDark
import com.example.fedger.ui.theme.SurfaceLight
import com.example.fedger.ui.theme.PurpleHighlight

// Simplified enhanced card with optimized animation
@Composable
fun EnhancedCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Simplified animation - only animate scale if there's a click handler
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        label = "scale"
    )
    
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }
    
    // Use a single Card component to reduce nesting
    Card(
        modifier = modifier
            .scale(scale)
            .then(clickableModifier),
        shape = shape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        ),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground // Use solid color instead of gradient
        ),
    ) {
        // Add a thin border inside the content area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = LightPurple.copy(alpha = 0.3f),
                    shape = shape
                )
                .padding(16.dp),
            content = content
        )
    }
}

// Optimized transaction card with simplified animation
@Composable
fun TransactionCard(
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Only animate if clickable
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        label = "scale"
    )
    
    val shape = MaterialTheme.shapes.medium
    
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }
    
    // Simplified card structure
    Card(
        modifier = modifier
            .scale(scale)
            .then(clickableModifier),
        shape = shape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) SurfaceLight else CardBackground
        )
    ) {
        // Simplified border
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isHighlighted) 1.5.dp else 1.dp,
                    color = if (isHighlighted) PurpleHighlight.copy(alpha = 0.8f) else LightPurple.copy(alpha = 0.3f),
                    shape = shape
                )
                .padding(16.dp),
            content = content
        )
    }
}

// Simplified gradient surface with reduced complexity
@Composable
fun GradientSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    startColor: Color = CardBackground,
    endColor: Color = SurfaceDark,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(startColor) // Simplified to solid color
            .border(
                width = 1.dp,
                color = LightPurple.copy(alpha = 0.3f),
                shape = shape
            ),
        content = content
    )
}

// Optimized accent card with simplified animation
@Composable
fun AccentCard(
    modifier: Modifier = Modifier,
    accentColor: Color = MediumPurple,
    shape: Shape = MaterialTheme.shapes.medium,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        label = "scale"
    )
    
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }
    
    // Simplified card structure
    Card(
        modifier = modifier
            .scale(scale)
            .then(clickableModifier),
        shape = shape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = accentColor.copy(alpha = 0.8f),
                    shape = shape
                )
                .padding(16.dp),
            content = content
        )
    }
} 