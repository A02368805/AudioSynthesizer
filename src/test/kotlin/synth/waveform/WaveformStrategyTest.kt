package synth.waveform

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WaveformStrategyTest {

    private val tolerance = 1e-6

    // ── Sine ─────────────────────────────────────────────────────────────────

    @Test
    fun `sine wave produces correct length`() {
        assertEquals(44100, SineWaveStrategy().generate(440.0, 1.0, 44100).size)
    }

    @Test
    fun `sine wave known samples at 1 Hz sampleRate 4`() {
        val result = SineWaveStrategy().generate(1.0, 1.0, 4)
        assertEquals(4, result.size)
        assertEquals(sin(0.0), result[0], tolerance)
        assertEquals(sin(PI / 2.0), result[1], tolerance)
        assertEquals(sin(PI), result[2], tolerance)
        assertEquals(sin(3.0 * PI / 2.0), result[3], tolerance)
    }

    @Test
    fun `sine wave values stay within range`() {
        val result = SineWaveStrategy().generate(440.0, 1.0, 44100)
        assertTrue(result.all { abs(it) <= 1.0 + 1e-10 })
    }

    // ── Square ────────────────────────────────────────────────────────────────

    @Test
    fun `square wave produces correct length`() {
        assertEquals(44100, SquareWaveStrategy().generate(440.0, 1.0, 44100).size)
    }

    @Test
    fun `square wave known samples at 1 Hz sampleRate 4`() {
        val result = SquareWaveStrategy().generate(1.0, 1.0, 4)
        assertEquals(4, result.size)
        // phases: 0, π/2, π, 3π/2 — first two < π → 1.0, last two ≥ π → -1.0
        assertEquals(1.0, result[0], tolerance)
        assertEquals(1.0, result[1], tolerance)
        assertEquals(-1.0, result[2], tolerance)
        assertEquals(-1.0, result[3], tolerance)
    }

    @Test
    fun `square wave only produces values of 1 or -1`() {
        val result = SquareWaveStrategy().generate(440.0, 1.0, 44100)
        assertTrue(result.all { it == 1.0 || it == -1.0 })
    }

    // ── Saw ───────────────────────────────────────────────────────────────────

    @Test
    fun `saw wave produces correct length`() {
        assertEquals(44100, SawWaveStrategy().generate(440.0, 1.0, 44100).size)
    }

    @Test
    fun `saw wave known samples at 1 Hz sampleRate 4`() {
        val result = SawWaveStrategy().generate(1.0, 1.0, 4)
        assertEquals(4, result.size)
        // phase[n] = 2π*n/4; value = 2*(phase/2π) - 1 = n/2 - 1
        assertEquals(-1.0, result[0], tolerance)
        assertEquals(-0.5, result[1], tolerance)
        assertEquals(0.0, result[2], tolerance)
        assertEquals(0.5, result[3], tolerance)
    }

    @Test
    fun `saw wave values stay within range`() {
        val result = SawWaveStrategy().generate(440.0, 1.0, 44100)
        assertTrue(result.all { it >= -1.0 - 1e-10 && it <= 1.0 + 1e-10 })
    }

    // ── White Noise ───────────────────────────────────────────────────────────

    @Test
    fun `white noise produces correct length`() {
        assertEquals(44100, WhiteNoiseStrategy().generate(440.0, 1.0, 44100).size)
    }

    @Test
    fun `white noise values are within range`() {
        val result = WhiteNoiseStrategy().generate(440.0, 1.0, 44100)
        assertTrue(result.all { it >= -1.0 && it <= 1.0 }, "All samples must be in [-1.0, 1.0]")
    }

    // ── Shared length behaviour ────────────────────────────────────────────────

    @Test
    fun `all strategies return correct length for half-second duration`() {
        val strategies: List<WaveformStrategy> = listOf(
            SineWaveStrategy(), SquareWaveStrategy(), SawWaveStrategy(), WhiteNoiseStrategy()
        )
        for (strategy in strategies) {
            val result = strategy.generate(440.0, 0.5, 44100)
            assertEquals(22050, result.size, "Wrong length for ${strategy::class.simpleName}")
        }
    }
}
