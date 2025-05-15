package com.example.fedger.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.example.fedger.ui.theme.AccentTeal
import com.example.fedger.ui.theme.LightPurple
import com.example.fedger.ui.theme.MediumPurple
import com.example.fedger.ui.theme.PurpleHighlight
import com.example.fedger.ui.theme.TextGrey
import com.example.fedger.ui.theme.TextWhite
import com.example.fedger.ui.theme.HighContrastGrey

// Simplified loading indicator with reduced animations and improved battery efficiency
@Composable
fun StyledLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MediumPurple,
    size: Float = 1f,
    isVisible: Boolean = true // Add visibility control to stop animations when not visible
) {
    if (!isVisible) return
    
    // Use a simpler animation with longer duration and less frequent updates
    val infiniteTransition = rememberInfiniteTransition(label = "loader")
    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000, // Longer duration means less CPU/GPU updates
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier
            .size((50 * size).dp),
        contentAlignment = Alignment.Center
    ) {
        // Use a more battery-efficient approach with fewer composables
        CircularProgressIndicator(
            modifier = Modifier.size((50 * size).dp),
            color = PurpleHighlight.copy(alpha = 0.4f),
            strokeWidth = (4 * size).dp,
            strokeCap = StrokeCap.Round
        )
        
        // Single rotating progress indicator instead of multiple animated elements
        CircularProgressIndicator(
            modifier = Modifier
                .size((36 * size).dp),
            progress = rotationAnim / 360f,
            color = MediumPurple,
            strokeWidth = (3 * size).dp,
            strokeCap = StrokeCap.Round
        )
    }
}

// Empty state for person list
@Composable
fun EmptyPersonListState(
    onAddClick: () -> Unit = {}
) {
    EmptyStateBase(
        icon = Icons.Default.Person,
        title = "No Contacts Added Yet",
        description = "Use the 'Add Contact' button in the bottom navigation bar to add someone you owe money to or who owes you",
        iconTint = PurpleHighlight,
        showButton = false,
        showBottomBarHint = true
    )
}

// Empty state for transactions
@Composable
fun EmptyTransactionsState(
    onAddClick: () -> Unit
) {
    EmptyStateBase(
        icon = Icons.Default.Info,
        title = "No Transactions Yet",
        description = "Track money flow by adding transactions with this person",
        buttonText = "Add Transaction",
        onButtonClick = onAddClick,
        iconTint = AccentTeal
    )
}

// Empty state for search results
@Composable
fun EmptySearchState(
    query: String
) {
    EmptyStateBase(
        icon = Icons.Default.Search,
        title = "No Results Found",
        description = "We couldn't find any results for \"$query\"",
        showButton = false,
        iconTint = TextGrey
    )
}

// Simplified base component for empty states with reduced animations
@Composable
private fun EmptyStateBase(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String = "",
    onButtonClick: () -> Unit = {},
    iconTint: Color = LightPurple,
    showButton: Boolean = true,
    showBottomBarHint: Boolean = false
) {
    // Remove excessive animations to improve performance
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Static icon container with simplified shadow
        Box(
            modifier = Modifier
                .size(120.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = iconTint.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(color = iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(60.dp),
                tint = iconTint
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = HighContrastGrey, // Use higher contrast color
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        
        if (showButton) {
            Spacer(modifier = Modifier.height(32.dp))
            
            ElevatedButton(
                onClick = onButtonClick,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MediumPurple,
                    contentColor = TextWhite
                ),
                modifier = Modifier
                    .height(50.dp)
                    .semantics {
                        contentDescription = buttonText
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                
                Spacer(modifier = Modifier.size(8.dp))
                
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (showBottomBarHint) {
            // For empty states with bottom navigation, add a hint that points to it
            Spacer(modifier = Modifier.height(32.dp))
            
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "Look at bottom navigation",
                tint = TextGrey,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Use the options below",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGrey,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Optimized content state handler with better memory efficiency
@Composable
fun <T> ContentStateHandler(
    isLoading: Boolean,
    items: List<T>,
    emptyContent: @Composable () -> Unit,
    loadingContent: @Composable () -> Unit = { LoadingState() },
    content: @Composable () -> Unit
) {
    // Use a more direct approach to avoid unnecessary nesting and recompositions
    if (isLoading) {
        loadingContent()
    } else if (items.isEmpty()) {
        emptyContent()
    } else {
        content()
    }
}

// More efficient loading state component with controlled animations
@Composable
fun LoadingState(
    message: String = "Loading..."
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
            .semantics {
                contentDescription = "Loading content, please wait"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Control animation visibility only when component is visible
        // to reduce battery consumption when loading state is not shown
        StyledLoadingIndicator(
            size = 1.2f,
            isVisible = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = HighContrastGrey,
            fontWeight = FontWeight.Medium
        )
    }
} 