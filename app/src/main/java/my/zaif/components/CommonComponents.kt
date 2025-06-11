package my.zaif.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import my.zaif.ui.theme.Spacing

@Composable
fun ScreenTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(Spacing.animationDurationMedium)) + 
                   slideInVertically(tween(Spacing.animationDurationMedium)) { -it }
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    horizontal = Spacing.screenHorizontalPadding, 
                    vertical = Spacing.medium
                )
            )
        }
        
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}

@Composable
fun EmptyStateMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(Spacing.animationDurationLong)) + 
                   slideInVertically(tween(Spacing.animationDurationLong)) { it / 2 }
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.extraLarge)
            )
        }
    }
}

@Composable
fun ContentSurface(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Spacing.medium),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Box(
            modifier = Modifier.padding(contentPadding)
        ) {
            content()
        }
    }
}

@Composable
fun VerticalSpacer(height: androidx.compose.ui.unit.Dp = Spacing.medium) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(Spacing.animationDurationMedium)) + 
                   slideInVertically(tween(Spacing.animationDurationMedium)) { it / 2 }
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    horizontal = Spacing.screenHorizontalPadding, 
                    vertical = Spacing.contentItemSpacing + Spacing.extraSmall
                )
            )
        }
    }
}

@Composable
fun ContentCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    elevation: Int = 2,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = elevation.dp,
                shape = shape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        content()
    }
} 