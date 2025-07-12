package com.diettrackr.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diettrackr.app.ui.theme.*

@Composable
fun MacrosProgressBar(
    protein: Int,
    carbs: Int,
    fats: Int,
    targetProtein: Int = 100,
    targetCarbs: Int = 100,
    targetFats: Int = 100,
    showText: Boolean = true,
    modifier: Modifier = Modifier
) {
    val proteinProgress = if (targetProtein > 0) (protein / targetProtein.toFloat()).coerceIn(0f, 1f) else 0f
    val carbsProgress = if (targetCarbs > 0) (carbs / targetCarbs.toFloat()).coerceIn(0f, 1f) else 0f
    val fatsProgress = if (targetFats > 0) (fats / targetFats.toFloat()).coerceIn(0f, 1f) else 0f
    
    val animatedProteinProgress by animateFloatAsState(targetValue = proteinProgress)
    val animatedCarbsProgress by animateFloatAsState(targetValue = carbsProgress)
    val animatedFatsProgress by animateFloatAsState(targetValue = fatsProgress)
    
    Column(modifier = modifier) {
        // Modern progress bars with rounded corners
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Protein bar
            ModernProgressBar(
                progress = animatedProteinProgress,
                color = ProteinColor,
                lightColor = ProteinColorLight,
                label = "Protein",
                current = protein,
                target = targetProtein
            )
            
            // Carbs bar
            ModernProgressBar(
                progress = animatedCarbsProgress,
                color = CarbsColor,
                lightColor = CarbsColorLight,
                label = "Carbs",
                current = carbs,
                target = targetCarbs
            )
            
            // Fats bar
            ModernProgressBar(
                progress = animatedFatsProgress,
                color = FatsColor,
                lightColor = FatsColorLight,
                label = "Fats",
                current = fats,
                target = targetFats
            )
        }
    }
}

@Composable
private fun ModernProgressBar(
    progress: Float,
    color: Color,
    lightColor: Color,
    label: String,
    current: Int,
    target: Int
) {
    Column {
        // Label and values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Text(
                text = "$current/$target g",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2A2A2A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(color, lightColor)
                        )
                    )
            )
        }
    }
}

// Legacy component for backward compatibility
@Composable
private fun MacroLabel(
    label: String,
    current: Int,
    target: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = color
        )
        Text(
            text = "$current/$target g",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
} 