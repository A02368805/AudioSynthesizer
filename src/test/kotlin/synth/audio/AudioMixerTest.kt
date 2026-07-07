package synth.audio

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AudioMixerTest {

    private val mixer = AudioMixer()

    @Test
    fun `empty channel list returns empty array`() {
        assertEquals(0, mixer.mix(emptyList()).size)
    }

    @Test
    fun `single channel within range passes through unchanged`() {
        val ch = doubleArrayOf(0.5, -0.3, 0.1)
        val result = mixer.mix(listOf(ch))
        assertEquals(0.5, result[0], 1e-9)
        assertEquals(-0.3, result[1], 1e-9)
        assertEquals(0.1, result[2], 1e-9)
    }

    @Test
    fun `two same-length channels sum sample by sample`() {
        val result = mixer.mix(listOf(doubleArrayOf(0.2, 0.3), doubleArrayOf(0.1, -0.1)))
        assertEquals(0.3, result[0], 1e-9)
        assertEquals(0.2, result[1], 1e-9)
    }

    @Test
    fun `shorter channel is padded with zeros`() {
        val ch1 = doubleArrayOf(0.5, 0.5, 0.5)
        val ch2 = doubleArrayOf(0.1)
        val result = mixer.mix(listOf(ch1, ch2))
        assertEquals(3, result.size)
        assertEquals(0.6, result[0], 1e-9)
        assertEquals(0.5, result[1], 1e-9)
        assertEquals(0.5, result[2], 1e-9)
    }

    @Test
    fun `output length equals longest channel`() {
        val ch1 = doubleArrayOf(0.1, 0.2, 0.3, 0.4)
        val ch2 = doubleArrayOf(0.1, 0.2)
        assertEquals(4, mixer.mix(listOf(ch1, ch2)).size)
    }

    @Test
    fun `normalization triggers when peak exceeds 1`() {
        val result = mixer.mix(listOf(doubleArrayOf(0.8), doubleArrayOf(0.8)))
        // sum = 1.6, normalised to 1.0
        assertEquals(1.0, result[0], 1e-9)
    }

    @Test
    fun `normalization does not trigger when peak equals 1`() {
        val result = mixer.mix(listOf(doubleArrayOf(0.5), doubleArrayOf(0.5)))
        assertEquals(1.0, result[0], 1e-9)
    }

    @Test
    fun `normalization preserves relative ratios`() {
        val ch1 = doubleArrayOf(0.6, 0.8)
        val ch2 = doubleArrayOf(0.6, 0.8)
        val result = mixer.mix(listOf(ch1, ch2))
        // sums: [1.2, 1.6], peak = 1.6 → [0.75, 1.0]
        assertEquals(0.75, result[0], 1e-9)
        assertEquals(1.0, result[1], 1e-9)
    }

    @Test
    fun `result always stays within range after normalization`() {
        val ch1 = doubleArrayOf(0.9, -0.9)
        val ch2 = doubleArrayOf(0.9, -0.9)
        val result = mixer.mix(listOf(ch1, ch2))
        assertTrue(result.all { abs(it) <= 1.0 + 1e-10 })
    }

    @Test
    fun `normalize exposed method works on already-in-range data`() {
        val samples = doubleArrayOf(0.3, -0.5, 0.1)
        val result = mixer.normalize(samples)
        samples.forEachIndexed { i, v -> assertEquals(v, result[i], 1e-9) }
    }

    @Test
    fun `normalize scales down when peak exceeds 1`() {
        val samples = doubleArrayOf(0.5, 2.0, -1.5)
        val result = mixer.normalize(samples)
        assertEquals(1.0, result[1], 1e-9)
        assertEquals(0.25, result[0], 1e-9)
    }
}
