package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A highly polished reusable container that implements a Premium Frosted Glass (Glassmorphism) look.
 * Combines translucent surfaces, dynamic linear gradient borders, shadows, and custom shapes.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    glassColor: Color = Color(0x12FFFFFF), // Micro-alpha translucent white
    glassBorderColorStart: Color = Color(0x3BFFFFFF), // Crisp upper border glow
    glassBorderColorEnd: Color = Color(0x05FFFFFF), // Fade-out lower border glow
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val roundedShape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            // Ambient soft shadow for visual depth and layered surface separation
            .shadow(
                elevation = 8.dp,
                shape = roundedShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(roundedShape)
            // Translucent glass filling
            .background(glassColor)
            // Linear gradient border sheen mimicking physical light reflection
            .border(
                border = BorderStroke(
                    width = borderWidth,
                    brush = Brush.verticalGradient(
                        colors = listOf(glassBorderColorStart, glassBorderColorEnd)
                    )
                ),
                shape = roundedShape
            )
            .padding(contentPadding)
    ) {
        content()
    }
}
