package com.victustune.player.ui.screens.extras

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.victustune.player.playback.EqPreset
import com.victustune.player.playback.EqualizerController
import com.victustune.player.ui.theme.*

@Composable
fun EqualizerScreen(
    equalizerController: EqualizerController,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEnabled by remember { mutableStateOf(equalizerController.isEnabled) }
    var selectedPresetName by remember { mutableStateOf("Flat") }

    val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    var bandLevels by remember {
        mutableStateOf(listOf(0, 0, 0, 0, 0))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Equalizer",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Enable / Disable Switch Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Equalizer Audio FX",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = if (isEnabled) "Active" else "Bypassed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isEnabled) VividCyan else TextTertiary
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = {
                    isEnabled = it
                    equalizerController.setEnabled(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ElectricPurple,
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = DarkSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preset Chips
        Text(
            text = "PRESETS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(equalizerController.defaultPresets) { preset ->
                val isSelected = selectedPresetName == preset.name
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedPresetName = preset.name
                        bandLevels = preset.levels
                        equalizerController.applyPreset(preset)
                    },
                    label = { Text(preset.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricPurple,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurfaceVariant,
                        labelColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5-band Vertical Sliders
        Text(
            text = "FREQUENCY BANDS (-10dB to +10dB)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bandFrequencies.forEachIndexed { index, freqLabel ->
                val currentLevel = bandLevels.getOrElse(index) { 0 }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "+10",
                        fontSize = 10.sp,
                        color = TextTertiary
                    )

                    // Vertical slider represented via Compose Slider in column
                    Slider(
                        value = currentLevel.toFloat(),
                        onValueChange = { newVal ->
                            val updated = bandLevels.toMutableList()
                            updated[index] = newVal.toInt()
                            bandLevels = updated
                            equalizerController.setBandLevel(index.toShort(), newVal.toInt().toShort())
                            selectedPresetName = "Custom"
                        },
                        valueRange = -1000f..1000f,
                        enabled = isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricPurple,
                            activeTrackColor = VividCyan,
                            inactiveTrackColor = DarkSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .width(120.dp)
                    )

                    Text(
                        text = "-10",
                        fontSize = 10.sp,
                        color = TextTertiary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = freqLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) TextPrimary else TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
