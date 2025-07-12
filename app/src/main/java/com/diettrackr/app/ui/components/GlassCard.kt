package com.diettrackr.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.diettrackr.app.ui.theme.GlassBorder
import com.diettrackr.app.ui.theme.GlassCardBackground
import com.diettrackr.app.ui.theme.GlassBackgroundStrong

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 20,
    backgroundAlpha: Float = 0.1f,
    borderAlpha: Float = 0.25f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = backgroundAlpha),
                        Color.White.copy(alpha = backgroundAlpha * 0.7f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .padding(20.dp),
        content = content
    )
}

@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = GlassCardBackground,
    cornerRadius: Int = 20,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .padding(20.dp),
        content = content
    )
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color>,
    cornerRadius: Int = 20,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .padding(20.dp),
        content = content
    )
} 