package my.zaif.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import my.zaif.data.entity.Entity
import my.zaif.ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntityItem(
    entity: Entity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ContentCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Spacing.screenHorizontalPadding, 
                vertical = Spacing.screenVerticalPadding / 2
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = 3
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardInnerPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Entity icon with circle background and first character
            Box(
                modifier = Modifier
                    .size(Spacing.avatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val initial = entity.entityName.firstOrNull()?.uppercase() ?: "?"
                Text(
                    text = initial,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            // Entity details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.contentPadding)
            ) {
                Text(
                    text = entity.entityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
} 