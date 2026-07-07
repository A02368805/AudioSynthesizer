package synth.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AudioPlayerTest {

    private val player = AudioPlayer(44100)

    @Test
    fun `convertToPCM produces two bytes per sample`() {
        val samples = doubleArrayOf(0.0, 0.5, -1.0, 1.0)
        assertEquals(samples.size * 2, player.convertToPCM(samples).size)
    }

    @Test
    fun `convertToPCM encodes silence as zero bytes`() {
        val pcm = player.convertToPCM(doubleArrayOf(0.0))
        assertEquals(0, pcm[0].toInt())
        assertEquals(0, pcm[1].toInt())
    }

    @Test
    fun `convertToPCM encodes max amplitude as Short MAX_VALUE`() {
        val pcm = player.convertToPCM(doubleArrayOf(1.0))
        val lo = pcm[0].toInt() and 0xff
        val hi = pcm[1].toInt() and 0xff
        val reconstructed = ((hi shl 8) or lo).toShort()
        assertEquals(Short.MAX_VALUE, reconstructed)
    }

    @Test
    fun `convertToPCM encodes min amplitude as Short MIN_VALUE plus 1`() {
        val pcm = player.convertToPCM(doubleArrayOf(-1.0))
        val lo = pcm[0].toInt() and 0xff
        val hi = pcm[1].toInt() and 0xff
        val reconstructed = ((hi shl 8) or lo).toShort()
        // (-1.0 * Short.MAX_VALUE).toInt().toShort() = -32767
        assertEquals((-Short.MAX_VALUE).toShort(), reconstructed)
    }

    @Test
    fun `convertToPCM clamps values above 1`() {
        val pcm = player.convertToPCM(doubleArrayOf(1.5))
        val lo = pcm[0].toInt() and 0xff
        val hi = pcm[1].toInt() and 0xff
        val reconstructed = ((hi shl 8) or lo).toShort()
        assertEquals(Short.MAX_VALUE, reconstructed)
    }

    @Test
    fun `convertToPCM clamps values below minus 1`() {
        val pcm = player.convertToPCM(doubleArrayOf(-2.0))
        val lo = pcm[0].toInt() and 0xff
        val hi = pcm[1].toInt() and 0xff
        val reconstructed = ((hi shl 8) or lo).toShort()
        assertEquals((-Short.MAX_VALUE).toShort(), reconstructed)
    }
}
