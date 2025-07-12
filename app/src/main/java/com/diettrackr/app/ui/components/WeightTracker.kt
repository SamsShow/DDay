package com.diettrackr.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.diettrackr.app.data.models.WeightEntry
import com.diettrackr.app.ui.theme.DarkPrimary
import com.diettrackr.app.ui.theme.DarkSurface
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTracker(
    currentWeight: Float,
    goalWeight: Float,
    recentEntries: List<WeightEntry>,
    onAddEntry: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newWeightInput by remember { mutableStateOf("") }
    
    GlassCard(modifier = modifier) {
        Column {
            // Header
            Text(
                text = "Weight Tracker",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Current vs Goal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeightBox(
                    title = "Current",
                    weight = currentWeight,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                WeightBox(
                    title = "Goal",
                    weight = goalWeight,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Weight chart
            if (recentEntries.size > 1) {
                WeightChart(
                    entries = recentEntries,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add new entry button
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add weight entry"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Entry")
            }
        }
    }
    
    // Add weight entry dialog
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Add Weight Entry",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = newWeightInput,
                        onValueChange = { newWeightInput = it },
                        label = { Text("Weight (kg)") },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = DarkPrimary,
                            focusedLabelColor = DarkPrimary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                newWeightInput.toFloatOrNull()?.let { weight ->
                                    onAddEntry(weight)
                                    showAddDialog = false
                                    newWeightInput = ""
                                }
                            },
                            enabled = newWeightInput.toFloatOrNull() != null
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeightBox(
    title: String,
    weight: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0x33FFFFFF),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "%.1f kg".format(weight),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun WeightChart(
    entries: List<WeightEntry>,
    modifier: Modifier = Modifier
) {
    val sortedEntries = entries.sortedBy { it.date }
    val minWeight = sortedEntries.minByOrNull { it.weight }?.weight ?: 0f
    val maxWeight = sortedEntries.maxByOrNull { it.weight }?.weight ?: 0f
    val range = maxOf(1f, maxWeight - minWeight + 2f)
    
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val xStep = width / (sortedEntries.size - 1).coerceAtLeast(1)
        
        // Draw horizontal grid lines
        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        for (i in 0..4) {
            val y = height * (1 - i / 4f)
            drawLine(
                color = Color(0x33FFFFFF),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f,
                pathEffect = dashPathEffect
            )
        }
        
        // Draw the weight line
        if (sortedEntries.size > 1) {
            for (i in 0 until sortedEntries.size - 1) {
                val startX = i * xStep
                val startY = if (range > 0) height * (1 - (sortedEntries[i].weight - minWeight) / range) else height / 2
                val endX = (i + 1) * xStep
                val endY = if (range > 0) height * (1 - (sortedEntries[i + 1].weight - minWeight) / range) else height / 2
                
                drawLine(
                    color = DarkPrimary,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3f
                )
            }
            
            // Draw points
            sortedEntries.forEachIndexed { index, entry ->
                val x = index * xStep
                val y = if (range > 0) height * (1 - (entry.weight - minWeight) / range) else height / 2
                
                drawCircle(
                    color = DarkPrimary,
                    radius = 6f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.Black,
                    radius = 3f,
                    center = Offset(x, y)
                )
            }
        }
    }
} 