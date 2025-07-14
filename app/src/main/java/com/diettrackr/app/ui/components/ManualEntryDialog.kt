package com.diettrackr.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.diettrackr.app.data.models.Meal
import com.diettrackr.app.ui.theme.*

data class ManualEntryData(
    val calories: Int? = null,
    val protein: Int? = null,
    val carbs: Int? = null,
    val fats: Int? = null
)

@Composable
fun ManualEntryDialog(
    meal: Meal,
    initialData: ManualEntryData = ManualEntryData(),
    onDismiss: () -> Unit,
    onSave: (ManualEntryData) -> Unit
) {
    var calories by remember { mutableStateOf(initialData.calories?.toString() ?: "") }
    var protein by remember { mutableStateOf(initialData.protein?.toString() ?: "") }
    var carbs by remember { mutableStateOf(initialData.carbs?.toString() ?: "") }
    var fats by remember { mutableStateOf(initialData.fats?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        ModernCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            cornerRadius = 20
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manual Entry",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Meal info
                Text(
                    text = meal.name,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Text(
                    text = "Original: ${meal.calories} cal, ${meal.protein}p ${meal.carbs}c ${meal.fats}f",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Input fields
                ManualEntryField(
                    label = "Calories",
                    value = calories,
                    onValueChange = { calories = it },
                    color = CardOrange,
                    placeholder = meal.calories.toString()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ManualEntryField(
                    label = "Protein (g)",
                    value = protein,
                    onValueChange = { protein = it },
                    color = ProteinColor,
                    placeholder = meal.protein.toString()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ManualEntryField(
                    label = "Carbs (g)",
                    value = carbs,
                    onValueChange = { carbs = it },
                    color = CarbsColor,
                    placeholder = meal.carbs.toString()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ManualEntryField(
                    label = "Fats (g)",
                    value = fats,
                    onValueChange = { fats = it },
                    color = FatsColor,
                    placeholder = meal.fats.toString()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Clear button
                    ModernActionButton(
                        text = "Clear",
                        icon = Icons.Default.Clear,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    ) {
                        calories = ""
                        protein = ""
                        carbs = ""
                        fats = ""
                    }
                    
                    // Save button
                    ModernActionButton(
                        text = "Save",
                        icon = Icons.Default.Save,
                        color = DarkPrimary,
                        modifier = Modifier.weight(1f)
                    ) {
                        val entryData = ManualEntryData(
                            calories = calories.toIntOrNull(),
                            protein = protein.toIntOrNull(),
                            carbs = carbs.toIntOrNull(),
                            fats = fats.toIntOrNull()
                        )
                        onSave(entryData)
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualEntryField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    color: Color,
    placeholder: String
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.White.copy(alpha = 0.5f)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = color,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                cursorColor = color,
                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
            ),
            singleLine = true
        )
    }
} 