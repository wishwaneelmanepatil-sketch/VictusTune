package com.victustune.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.victustune.player.ui.theme.DarkSurfaceBorder

@Composable
fun Modifier.glassmorphic(
    cornerRadius: Dp = 20.dp,
    tintColor: Color = Color.Transparent,
    tintAlpha: Float = 0.15f
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            (tintColor.takeIf { it != Color.Transparent } ?: Color(0xFF1E2235)).copy(alpha = 0.6f + tintAlpha),
            Color(0xFF0F111A).copy(alpha = 0.85f)
        )
    )

    return this
        .clip(shape)
        .background(backgroundBrush)
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.18f),
                    DarkSurfaceBorder.copy(alpha = 0.05f)
                )
            ),
            shape = shape
        )
}
