package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * A glossy, translucent button conforming to Material 3 accessibility guidelines.
 * Includes interactive state ripples, minimum touch target height constraints, and custom icon supports.
 */
@Composable
fun GlassmorphicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    glowAccent: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
) {
    val interactionSource = remember { MutableInteractionSource() }

    GlassmorphicCard(
        modifier = modifier
            .heightIn(min = 48.dp) // Accessibility touch target guarantee
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.rememberRipple(
                    bounded = true,
                    color = MaterialTheme.colorScheme.primary
                ),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        cornerRadius = 12.dp,
        glassColor = if (enabled) glowAccent else Color(0x08FFFFFF),
        contentPadding = 0.dp // Reset inner padding for exact alignment
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}
