package com.victustune.player.ui.screens.extras

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.victustune.player.data.model.AudioQuality
import com.victustune.player.ui.theme.*

@Composable
fun SettingsDialog(
    currentQuality: AudioQuality,
    onSelectQuality: (AudioQuality) -> Unit,
    currentCacheBytes: Long,
    maxCacheSizeMb: Int,
    onSelectMaxCacheSizeMb: (Int) -> Unit,
    onClearCache: () -> Unit,
    onDismiss: () -> Unit
) {
    val formattedCache = formatBytes(currentCacheBytes)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Streaming Settings",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Audio Quality Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Audio Stream Quality",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = VividCyan
                    )

                    AudioQuality.values().forEach { quality ->
                        val selected = quality == currentQuality
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectQuality(quality) }
                                .background(if (selected) DarkSurfaceVariant else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = quality.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (selected) TextPrimary else TextSecondary
                                )
                                Text(
                                    text = when (quality) {
                                        AudioQuality.AUTO -> "Auto-detect based on network (Recommended)"
                                        AudioQuality.HIGH -> "Highest bitrate (160kbps Opus / 256kbps M4A)"
                                        AudioQuality.MEDIUM -> "Standard quality (~128kbps)"
                                        AudioQuality.LOW -> "Data saver (~50kbps)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }
                            RadioButton(
                                selected = selected,
                                onClick = { onSelectQuality(quality) },
                                colors = RadioButtonDefaults.colors(selectedColor = VividCyan)
                            )
                        }
                    }
                }

                HorizontalDivider(color = DarkSurfaceVariant)

                // Cache Management Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Media Stream Cache",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = VividCyan
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Disk Usage",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = formattedCache,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    // Max cache size selector
                    Text(
                        text = "Cache Limit",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(256, 512, 1024).forEach { sizeMb ->
                            val isSelected = sizeMb == maxCacheSizeMb
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectMaxCacheSizeMb(sizeMb) },
                                label = { Text(if (sizeMb >= 1024) "1 GB" else "$sizeMb MB") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VividCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = VividCyan
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = onClearCache,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                    ) {
                        Text("Clear Stream Cache")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = VividCyan, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkSurface
    )
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes.toDouble() / (1024 * 1024)
    return if (mb < 1.0) {
        "%.1f KB".format(bytes.toDouble() / 1024)
    } else {
        "%.1f MB".format(mb)
    }
}
