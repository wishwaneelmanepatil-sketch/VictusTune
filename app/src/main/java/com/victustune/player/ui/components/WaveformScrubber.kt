package com.victustune.player.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.victustune.player.ui.theme.TextSecondary
import kotlin.math.sin

@Composable
fun WaveformScrubber(
    currentPositionMs: Long,
    totalDurationMs: Long,
    accentColor: Color,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val actualProgress = if (totalDurationMs > 0) {
        (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val displayedProgress = if (isDragging) dragProgress else actualProgress
    val displayedPositionMs = if (isDragging) (dragProgress * totalDurationMs).toLong() else currentPositionMs

    // Subtle wave animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "waveformAnim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Waveform Bar Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .pointerInput(totalDurationMs) {
                    detectTapGestures { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        val targetMs = (newProgress * totalDurationMs).toLong()
                        onSeek(targetMs)
                    }
                }
                .pointerInput(totalDurationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            val targetMs = (dragProgress * totalDurationMs).toLong()
                            onSeek(targetMs)
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barCount = 48
                val barWidth = 4.dp.toPx()
                val spacing = (canvasWidth - (barCount * barWidth)) / (barCount - 1).coerceAtLeast(1)

                val activeColor = accentColor
                val inactiveColor = Color.White.copy(alpha = 0.2f)
                val thumbColor = Color.White

                for (i in 0 until barCount) {
                    val barFraction = i.toFloat() / barCount.toFloat()
                    val x = i * (barWidth + spacing)

                    // Wave pattern equation
                    val baseHeightFactor = 0.25f + 0.55f * sin((i * 0.35f)).let { kotlin.math.abs(it) }
                    val dynamicWave = if (isPlaying && !isDragging) {
                        0.15f * sin(i * 0.4f + wavePhase)
                    } else 0f

                    val heightFraction = (baseHeightFactor + dynamicWave).coerceIn(0.15f, 0.95f)
                    val barH = canvasHeight * heightFraction
                    val y = (canvasHeight - barH) / 2f

                    val isPast = barFraction <= displayedProgress
                    val color = if (isPast) activeColor else inactiveColor

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barH),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }

                // Scrubber Playhead Thumb indicator
                val thumbX = (displayedProgress * canvasWidth).coerceIn(0f, canvasWidth)
                drawCircle(
                    color = thumbColor,
                    radius = if (isDragging) 8.dp.toPx() else 5.dp.toPx(),
                    center = Offset(thumbX, canvasHeight / 2f)
                )
                drawCircle(
                    color = accentColor.copy(alpha = 0.4f),
                    radius = if (isDragging) 14.dp.toPx() else 8.dp.toPx(),
                    center = Offset(thumbX, canvasHeight / 2f)
                )
            }
        }

        // Timestamps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDuration(displayedPositionMs),
                fontSize = 12.sp,
                color = TextSecondary
            )
            val remainingMs = (totalDurationMs - displayedPositionMs).coerceAtLeast(0L)
            Text(
                text = "-${formatDuration(remainingMs)}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
