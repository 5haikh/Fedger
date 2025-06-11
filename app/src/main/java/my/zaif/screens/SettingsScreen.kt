package my.zaif.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import my.zaif.components.ContentCard
import my.zaif.components.ScreenTitle
import my.zaif.components.SectionTitle
import my.zaif.ui.theme.Spacing
import my.zaif.ui.theme.ThemeManager

@Composable
fun SettingsScreen() {
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current
    
    Scaffold(
        topBar = {
            ScreenTitle(title = "Settings")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top
        ) {
            // Settings category
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(Spacing.animationDurationMedium)) + 
                       slideInVertically(tween(Spacing.animationDurationMedium)) { it / 2 }
            ) {
                SectionTitle(title = "Appearance")
            }
            
            // Dark mode setting
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(Spacing.animationDurationMedium + 100)) + 
                       slideInVertically(tween(Spacing.animationDurationMedium + 100)) { it / 2 }
            ) {
                SettingCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.cardInnerPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = "Dark Mode",
                                modifier = Modifier
                                    .padding(end = Spacing.contentPadding)
                                    .size(Spacing.iconSize),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Dark Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Switch between light and dark themes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = ThemeManager.isDarkTheme,
                            onCheckedChange = { 
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                ThemeManager.isDarkTheme = it 
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }
            
            // About section
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(Spacing.animationDurationMedium + 200)) + 
                       slideInVertically(tween(Spacing.animationDurationMedium + 200)) { it / 2 }
            ) {
                SectionTitle(
                    title = "About", 
                    modifier = Modifier.padding(top = Spacing.contentGroupSpacing)
                )
            }
            
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(Spacing.animationDurationMedium + 300)) + 
                       slideInVertically(tween(Spacing.animationDurationMedium + 300)) { it / 2 }
            ) {
                SettingCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.cardInnerPadding)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "App Info",
                                modifier = Modifier
                                    .padding(end = Spacing.contentPadding)
                                    .size(Spacing.iconSize),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Zaif",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Version 1.0",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = Spacing.contentItemSpacing + Spacing.extraSmall),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        
                        Text(
                            text = "A personal management app for tracking your finances and storing credentials securely.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                start = Spacing.avatarSize - Spacing.extraSmall, 
                                end = Spacing.small
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingCard(
    content: @Composable () -> Unit
) {
    ContentCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Spacing.screenHorizontalPadding, 
                vertical = Spacing.screenVerticalPadding
            ),
        elevation = 2
    ) {
        content()
    }
} 