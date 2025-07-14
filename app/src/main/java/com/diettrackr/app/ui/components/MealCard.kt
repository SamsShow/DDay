package com.diettrackr.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diettrackr.app.data.models.Meal
import com.diettrackr.app.data.models.MealComponent
import com.diettrackr.app.data.models.MealStatus
import com.diettrackr.app.data.models.DailyLog
import com.diettrackr.app.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun MealCard(
    meal: Meal,
    components: List<MealComponent>,
    status: MealStatus = MealStatus.PENDING,
    dailyLog: DailyLog? = null,
    onStatusChange: (MealStatus) -> Unit = {},
    onEditClick: () -> Unit = {},
    onManualEntry: (ManualEntryData) -> Unit = {},
    onFoodRecognition: () -> Unit = {},
    expanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when (status) {
            MealStatus.COMPLETED -> CompletedColor
            MealStatus.SKIPPED -> SkippedColor
            MealStatus.MODIFIED -> ModifiedColor
            MealStatus.PENDING -> PendingColor
        }
    )
    
    var showManualEntryDialog by remember { mutableStateOf(false) }
    
    // Use manual values if available, otherwise use meal defaults
    val displayCalories = dailyLog?.manualCalories ?: meal.calories
    val displayProtein = dailyLog?.manualProtein ?: meal.protein
    val displayCarbs = dailyLog?.manualCarbs ?: meal.carbs
    val displayFats = dailyLog?.manualFats ?: meal.fats
    
    // Simple glass card without colors
    ModernCard(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        cornerRadius = 24
    ) {
        Column {
            // Header row with time and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time and title
                Column {
                    Text(
                        text = meal.time.format(DateTimeFormatter.ofPattern("h:mm a")),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = meal.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Status indicator with modern styling
                StatusBadge(status = status, color = statusColor)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Macro summary with modern progress bars
            MacrosProgressBar(
                protein = displayProtein,
                carbs = displayCarbs,
                fats = displayFats,
                targetProtein = displayProtein,
                targetCarbs = displayCarbs,
                targetFats = displayFats
            )
            
            // Manual entry indicator
            if (dailyLog?.hasManualEntry == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Manual entry",
                        tint = CardOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Custom values entered",
                        fontSize = 12.sp,
                        color = CardOrange,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Calories with modern styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ${displayCalories} calories",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Expand/collapse button
                IconButton(
                    onClick = { onExpandChange(!expanded) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Expandable section
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                
                // Meal components with modern styling
                if (components.isNotEmpty()) {
                    Text(
                        text = "Components:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    components.forEach { component ->
                        MealComponentItem(component)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Manual Entry and Food Recognition buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModernActionButton(
                        text = "Manual Entry",
                        icon = Icons.Default.Edit,
                        color = CardOrange,
                        modifier = Modifier.weight(1f)
                    ) {
                        showManualEntryDialog = true
                    }
                    
                    ModernActionButton(
                        text = "Food Recognition",
                        icon = Icons.Default.CameraAlt,
                        color = CardBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        onFoodRecognition()
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Action buttons with modern styling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (status) {
                        MealStatus.PENDING -> {
                            ModernActionButton(
                                text = "Complete",
                                icon = Icons.Default.Check,
                                color = CompletedColor,
                                modifier = Modifier.weight(1f)
                            ) {
                                onStatusChange(MealStatus.COMPLETED)
                            }
                            
                            ModernActionButton(
                                text = "Skip",
                                icon = Icons.Default.Close,
                                color = SkippedColor,
                                modifier = Modifier.weight(1f)
                            ) {
                                onStatusChange(MealStatus.SKIPPED)
                            }
                            
                            ModernActionButton(
                                text = "Modify",
                                icon = Icons.Default.Edit,
                                color = ModifiedColor,
                                modifier = Modifier.weight(1f)
                            ) {
                                onEditClick()
                                onStatusChange(MealStatus.MODIFIED)
                            }
                        }
                        else -> {
                            ModernActionButton(
                                text = "Reset",
                                icon = Icons.Default.Refresh,
                                color = PendingColor,
                                modifier = Modifier.weight(1f)
                            ) {
                                onStatusChange(MealStatus.PENDING)
                            }
                            
                            ModernActionButton(
                                text = "Edit",
                                icon = Icons.Default.Edit,
                                color = ModifiedColor,
                                modifier = Modifier.weight(1f)
                            ) {
                                onEditClick()
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Manual Entry Dialog
    if (showManualEntryDialog) {
        ManualEntryDialog(
            meal = meal,
            initialData = ManualEntryData(
                calories = dailyLog?.manualCalories,
                protein = dailyLog?.manualProtein,
                carbs = dailyLog?.manualCarbs,
                fats = dailyLog?.manualFats
            ),
            onDismiss = { showManualEntryDialog = false },
            onSave = { entryData ->
                onManualEntry(entryData)
                showManualEntryDialog = false
            }
        )
    }
}

@Composable
private fun StatusBadge(status: MealStatus, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = when (status) {
                    MealStatus.COMPLETED -> Icons.Default.CheckCircle
                    MealStatus.SKIPPED -> Icons.Default.Cancel
                    MealStatus.MODIFIED -> Icons.Default.Edit
                    MealStatus.PENDING -> Icons.Default.Schedule
                },
                contentDescription = "Meal status",
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = when (status) {
                    MealStatus.COMPLETED -> "DONE"
                    MealStatus.SKIPPED -> "SKIPPED"
                    MealStatus.MODIFIED -> "MODIFIED"
                    MealStatus.PENDING -> "PENDING"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun MealComponentItem(component: MealComponent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Component name and quantity
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = component.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = component.quantity,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        
        // Macro values with modern styling
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MacroValue("P", component.protein, ProteinColor)
            MacroValue("C", component.carbs, CarbsColor)
            MacroValue("F", component.fats, FatsColor)
        }
    }
}

@Composable
fun MacroValue(label: String, value: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun ModernActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// Legacy component for backward compatibility
@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    ModernActionButton(text, icon, color, onClick = onClick)
} 