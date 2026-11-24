package com.victustune.player.data.repository

import com.victustune.player.data.model.LyricLine
import com.victustune.player.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Pattern

class LyricsRepository {

    private val timePattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\]")

    suspend fun loadLyricsForSong(song: Song): List<LyricLine> = withContext(Dispatchers.IO) {
        // Try looking for local .lrc file in song directory
        val localFile = try {
            val path = song.contentUri.path
            if (path != null) {
                val f = File(path)
                val lrcFile = File(f.parentFile, "${f.nameWithoutExtension}.lrc")
                if (lrcFile.exists() && lrcFile.canRead()) lrcFile else null
            } else null
        } catch (e: Exception) {
            null
        }

        if (localFile != null) {
            val parsed = parseLrc(localFile.readText())
            if (parsed.isNotEmpty()) return@withContext parsed
        }

        // Fallback: Generate contextual synced demo lyrics matching the song's duration
        generateMockLyrics(song)
    }

    fun parseLrc(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        lrcContent.lineSequence().forEach { line ->
            val matcher = timePattern.matcher(line)
            if (matcher.find()) {
                val minutes = matcher.group(1)?.toLongOrNull() ?: 0L
                val seconds = matcher.group(2)?.toLongOrNull() ?: 0L
                val msRaw = matcher.group(3) ?: "00"
                val millis = if (msRaw.length == 2) msRaw.toLong() * 10 else msRaw.toLong()
                val totalMs = (minutes * 60 + seconds) * 1000 + millis
                val text = line.substring(matcher.end()).trim()
                if (text.isNotEmpty()) {
                    lines.add(LyricLine(totalMs, text))
                }
            }
        }
        return lines.sortedBy { it.timeMs }
    }

    private fun generateMockLyrics(song: Song): List<LyricLine> {
        val duration = song.durationMs.coerceAtLeast(60000L)
        val step = (duration / 12).coerceAtLeast(4000L)

        val lyricTexts = listOf(
            "✦ In the neon static of the night",
            "Waves of sound dissolve into light",
            "Underneath the digital sky",
            "Frequencies whisper and sigh",
            "Midnight rhythms begin to bloom",
            "Echoes reflecting across the room",
            "Hold onto the pulse, let it glide",
            "Surfing the crest of the infinite tide",
            "Harmonics align in the twilight haze",
            "Lost in the Victus Tune sonic maze",
            "Pure resonance, no rewind",
            "Leaving the static far behind ✦"
        )

        return lyricTexts.mapIndexed { index, text ->
            LyricLine(timeMs = index * step, text = text)
        }
    }
}
