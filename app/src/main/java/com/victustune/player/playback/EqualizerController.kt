package com.victustune.player.playback

import android.media.audiofx.Equalizer

data class EqPreset(
    val name: String,
    val levels: List<Int> // dB relative or millibels: -1000 to +1000 mB
)

class EqualizerController {

    private var equalizer: Equalizer? = null
    var isEnabled: Boolean = false
        private set

    val defaultPresets = listOf(
        EqPreset("Flat", listOf(0, 0, 0, 0, 0)),
        EqPreset("Bass Boost", listOf(600, 400, 100, 0, -100)),
        EqPreset("Electronic", listOf(500, 300, 0, 200, 400)),
        EqPreset("Rock", listOf(400, 200, -100, 200, 500)),
        EqPreset("Pop", listOf(-100, 200, 500, 200, -100)),
        EqPreset("Vocal Boost", listOf(-200, 100, 600, 400, 100)),
        EqPreset("Treble Boost", listOf(-200, -100, 100, 400, 700))
    )

    fun attachAudioSession(sessionId: Int) {
        if (sessionId == 0) return
        try {
            release()
            equalizer = Equalizer(0, sessionId).apply {
                enabled = isEnabled
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        try {
            equalizer?.enabled = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBandLevel(bandIndex: Short, levelMilliBels: Short) {
        try {
            equalizer?.setBandLevel(bandIndex, levelMilliBels)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyPreset(preset: EqPreset) {
        val eq = equalizer ?: return
        try {
            preset.levels.forEachIndexed { index, level ->
                if (index < eq.numberOfBands) {
                    eq.setBandLevel(index.toShort(), level.toShort())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            equalizer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        equalizer = null
    }
}
