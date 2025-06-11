package my.zaif.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import my.zaif.data.entity.Entity
import my.zaif.ui.theme.Spacing

@Composable
fun EntityDialog(
    entity: Entity? = null,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var entityName by remember { mutableStateOf(entity?.entityName ?: "") }
    var isNameError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        // Request focus on the name field when dialog appears
        focusRequester.requestFocus()
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) + 
                   scaleIn(spring(stiffness = Spring.StiffnessLow), initialScale = 0.95f)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(Spacing.dialogWidth)
                    .padding(
                        horizontal = Spacing.screenHorizontalPadding, 
                        vertical = Spacing.screenVerticalPadding
                    )
                    .shadow(
                        elevation = Spacing.dialogElevation,
                        shape = RoundedCornerShape(Spacing.dialogCornerRadius),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                shape = RoundedCornerShape(Spacing.dialogCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = Spacing.dialogElevation
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(Spacing.contentGroupSpacing)
                ) {
                    Text(
                        text = if (entity == null) "Add New Entity" else "Edit Entity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = entityName,
                        onValueChange = { 
                            entityName = it
                            isNameError = it.isBlank()
                        },
                        label = { Text("Entity Name") },
                        placeholder = { Text("Enter entity name (e.g., Gmail, Bank XYZ)") },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Domain, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        isError = isNameError,
                        supportingText = {
                            if (isNameError) {
                                Text(
                                    text = "Entity name cannot be empty",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        shape = RoundedCornerShape(Spacing.cardCornerRadius),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(Spacing.cardCornerRadius)
                        ) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Button(
                            onClick = { 
                                if (entityName.isNotBlank()) {
                                    onSave(entityName)
                                } else {
                                    isNameError = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(Spacing.cardCornerRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = Spacing.cardElevation,
                                pressedElevation = 1.dp
                            )
                        ) {
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
} 