package com.victustune.player.data.remote.extractor

import android.net.Uri
import com.victustune.player.data.model.AudioQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import java.util.concurrent.ConcurrentHashMap

data class CachedStreamUrl(
    val url: String,
    val expiresAtMs: Long,
    val format: String,
    val bitrate: Int
)

class StreamResolver(
    private var defaultQuality: AudioQuality = AudioQuality.HIGH
) {

    private val cache = ConcurrentHashMap<String, CachedStreamUrl>()

    fun setQuality(quality: AudioQuality) {
        defaultQuality = quality
    }

    fun invalidate(videoId: String) {
        cache.remove(videoId)
    }

    fun clearCache() {
        cache.clear()
    }

    suspend fun resolveStreamUrl(
        videoId: String,
        quality: AudioQuality = defaultQuality
    ): String = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cached = cache[videoId]
        if (cached != null && cached.expiresAtMs > now + 60_000L) {
            return@withContext cached.url
        }

        val videoUrl = "https://www.youtube.com/watch?v=$videoId"
        val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, videoUrl)
        val audioStreams = streamInfo.audioStreams

        if (audioStreams.isNullOrEmpty()) {
            throw IllegalStateException("No audio streams found for video $videoId")
        }

        val selectedStream = selectBestAudioStream(audioStreams, quality)
        val streamUrl = selectedStream.content
            ?: throw IllegalStateException("Selected audio stream has empty URL for video $videoId")

        // Parse expiration time from URL if available (e.g. &expire=1600000000), or default to 4 hours
        val expiresAtMs = extractExpirationMs(streamUrl, defaultTtlMs = 4 * 60 * 60 * 1000L)

        cache[videoId] = CachedStreamUrl(
            url = streamUrl,
            expiresAtMs = expiresAtMs,
            format = selectedStream.format?.name ?: "UNKNOWN",
            bitrate = selectedStream.averageBitrate
        )

        streamUrl
    }

    fun selectBestAudioStream(
        streams: List<AudioStream>,
        quality: AudioQuality
    ): AudioStream {
        // Group and sort streams
        // NewPipeExtractor MediaFormats: OPUS, M4A, WEBMA, MP3
        // Prefer Opus, then M4A
        fun formatScore(stream: AudioStream): Int {
            return when (stream.format) {
                MediaFormat.OPUS -> 300
                MediaFormat.M4A -> 200
                MediaFormat.WEBMA -> 100
                else -> 50
            }
        }

        return when (quality) {
            AudioQuality.HIGH, AudioQuality.AUTO -> {
                // Pick highest bitrate, tie-break with Opus > M4A
                streams.maxWithOrNull(
                    compareBy<AudioStream> { it.averageBitrate }
                        .thenBy { formatScore(it) }
                ) ?: streams.first()
            }
            AudioQuality.MEDIUM -> {
                // Target ~128kbps, closest to 128_000
                val targetBitrate = 128_000
                streams.minWithOrNull(
                    compareBy<AudioStream> { kotlin.math.abs(it.averageBitrate - targetBitrate) }
                        .thenByDescending { formatScore(it) }
                ) ?: streams.first()
            }
            AudioQuality.LOW -> {
                // Target lowest reasonable bitrate (>40kbps)
                streams.minWithOrNull(
                    compareBy<AudioStream> { it.averageBitrate }
                        .thenBy { formatScore(it) }
                ) ?: streams.first()
            }
        }
    }

    private fun extractExpirationMs(url: String, defaultTtlMs: Long): Long {
        val now = System.currentTimeMillis()
        return try {
            val uri = Uri.parse(url)
            val expireParam = uri.getQueryParameter("expire")
            if (expireParam != null) {
                val expireSec = expireParam.toLong()
                expireSec * 1000L
            } else {
                now + defaultTtlMs
            }
        } catch (e: Exception) {
            now + defaultTtlMs
        }
    }
}
