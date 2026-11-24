package com.victustune.player

import com.victustune.player.data.model.AudioQuality
import com.victustune.player.data.remote.extractor.StreamResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.DeliveryMethod

class StreamResolverTest {

    private fun createStream(id: String, format: MediaFormat, bitrate: Int, url: String): AudioStream {
        return AudioStream.Builder()
            .setId(id)
            .setDeliveryMethod(DeliveryMethod.PROGRESSIVE_HTTP)
            .setMediaFormat(format)
            .setAverageBitrate(bitrate)
            .setContent(url, false)
            .build()
    }

    @Test
    fun testSelectBestAudioStreamHighQuality() {
        val resolver = StreamResolver()
        val streamLow = createStream("1", MediaFormat.OPUS, 50_000, "https://stream/low.opus")
        val streamMed = createStream("2", MediaFormat.M4A, 128_000, "https://stream/med.m4a")
        val streamHigh = createStream("3", MediaFormat.OPUS, 160_000, "https://stream/high.opus")

        val selected = resolver.selectBestAudioStream(listOf(streamLow, streamMed, streamHigh), AudioQuality.HIGH)
        assertNotNull(selected)
        assertEquals("3", selected.id)
        assertEquals(160_000, selected.averageBitrate)
        assertEquals(MediaFormat.OPUS, selected.format)
    }

    @Test
    fun testSelectBestAudioStreamMediumQuality() {
        val resolver = StreamResolver()
        val streamLow = createStream("1", MediaFormat.OPUS, 50_000, "https://stream/low.opus")
        val streamMed = createStream("2", MediaFormat.M4A, 128_000, "https://stream/med.m4a")
        val streamHigh = createStream("3", MediaFormat.OPUS, 160_000, "https://stream/high.opus")

        val selected = resolver.selectBestAudioStream(listOf(streamLow, streamMed, streamHigh), AudioQuality.MEDIUM)
        assertNotNull(selected)
        assertEquals("2", selected.id)
        assertEquals(128_000, selected.averageBitrate)
    }

    @Test
    fun testSelectBestAudioStreamLowQuality() {
        val resolver = StreamResolver()
        val streamLow = createStream("1", MediaFormat.OPUS, 50_000, "https://stream/low.opus")
        val streamMed = createStream("2", MediaFormat.M4A, 128_000, "https://stream/med.m4a")
        val streamHigh = createStream("3", MediaFormat.OPUS, 160_000, "https://stream/high.opus")

        val selected = resolver.selectBestAudioStream(listOf(streamLow, streamMed, streamHigh), AudioQuality.LOW)
        assertNotNull(selected)
        assertEquals("1", selected.id)
        assertEquals(50_000, selected.averageBitrate)
    }

    @Test
    fun testFormatTieBreakingPrefersOpusOverM4AAtSameBitrate() {
        val resolver = StreamResolver()
        val streamM4a = createStream("m4a", MediaFormat.M4A, 160_000, "https://stream/m4a")
        val streamOpus = createStream("opus", MediaFormat.OPUS, 160_000, "https://stream/opus")

        val selected = resolver.selectBestAudioStream(listOf(streamM4a, streamOpus), AudioQuality.HIGH)
        assertNotNull(selected)
        assertEquals("opus", selected.id)
        assertEquals(MediaFormat.OPUS, selected.format)
    }
}
