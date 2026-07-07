package synth.effect

import synth.source.AudioSource
import kotlin.math.abs
import kotlin.math.tanh
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeAudioSource(private val samples: DoubleArray) : AudioSource {
    override fun render(): DoubleArray = samples.copyOf()
}

class AudioEffectDecoratorTest {

    // ── VolumeEffect ─────────────────────────────────────────────────────────

    @Test
    fun `volume effect scales samples by level`() {
        val effect = VolumeEffect(FakeAudioSource(doubleArrayOf(1.0, -0.5, 0.8)), 0.5)
        val result = effect.render()
        assertEquals(0.5, result[0], 1e-9)
        assertEquals(-0.25, result[1], 1e-9)
        assertEquals(0.4, result[2], 1e-9)
    }

    @Test
    fun `volume effect of 1 does not alter samples`() {
        val input = doubleArrayOf(0.3, -0.7, 0.0)
        val result = VolumeEffect(FakeAudioSource(input), 1.0).render()
        input.forEachIndexed { i, v -> assertEquals(v, result[i], 1e-9) }
    }

    @Test
    fun `volume effect of 0 produces silence`() {
        val result = VolumeEffect(FakeAudioSource(doubleArrayOf(1.0, -1.0)), 0.0).render()
        assertTrue(result.all { it == 0.0 })
    }

    // ── ClipDistortionEffect ──────────────────────────────────────────────────

    @Test
    fun `clip effect clamps samples to threshold`() {
        val effect = ClipDistortionEffect(FakeAudioSource(doubleArrayOf(1.0, 0.2, -0.9)), 0.5)
        val result = effect.render()
        assertEquals(0.5, result[0], 1e-9)
        assertEquals(0.2, result[1], 1e-9)
        assertEquals(-0.5, result[2], 1e-9)
    }

    @Test
    fun `clip does not modify samples already within threshold`() {
        val input = doubleArrayOf(0.1, -0.3, 0.0)
        val result = ClipDistortionEffect(FakeAudioSource(input), 0.5).render()
        input.forEachIndexed { i, v -> assertEquals(v, result[i], 1e-9) }
    }

    // ── TanhDistortionEffect ──────────────────────────────────────────────────

    @Test
    fun `tanh effect output stays within minus 1 to 1`() {
        val result = TanhDistortionEffect(FakeAudioSource(doubleArrayOf(1.0, -1.0, 0.5, 0.0)), 2.0).render()
        assertTrue(result.all { abs(it) <= 1.0 })
    }

    @Test
    fun `tanh effect preserves zero`() {
        val result = TanhDistortionEffect(FakeAudioSource(doubleArrayOf(0.0)), 5.0).render()
        assertEquals(0.0, result[0], 1e-9)
    }

    @Test
    fun `tanh effect higher drive compresses more than lower drive`() {
        val input = doubleArrayOf(0.5)
        val low = TanhDistortionEffect(FakeAudioSource(input), 1.0).render()[0]
        val high = TanhDistortionEffect(FakeAudioSource(input), 10.0).render()[0]
        assertTrue(high > low, "Higher drive should produce larger compressed output")
    }

    @Test
    fun `tanh effect applies tanh(sample times drive)`() {
        val sample = 0.6
        val drive = 3.0
        val result = TanhDistortionEffect(FakeAudioSource(doubleArrayOf(sample)), drive).render()
        assertEquals(tanh(sample * drive), result[0], 1e-9)
    }

    // ── AdsEffect ────────────────────────────────────────────────────────────

    @Test
    fun `ads attack phase ramps from near zero to 1`() {
        val sampleRate = 100
        val samples = DoubleArray(100) { 1.0 }
        // attackEnd=0.5s, decayEnd=0.8s, sustain=0.6
        val result = AdsEffect(FakeAudioSource(samples), 0.5, 0.8, 0.6, sampleRate).render()
        assertEquals(0.0, result[0], 0.02)     // t=0 → envelope ≈ 0
        assertEquals(1.0, result[50], 0.05)    // t=0.5s → envelope ≈ 1
    }

    @Test
    fun `ads sustain phase holds at sustain level`() {
        val sampleRate = 100
        val samples = DoubleArray(200) { 1.0 }
        // attackEnd=0.1s, decayEnd=0.2s, sustain=0.6
        val result = AdsEffect(FakeAudioSource(samples), 0.1, 0.2, 0.6, sampleRate).render()
        for (i in 25 until 200) {
            assertEquals(0.6, result[i], 0.02, "Sample $i should be at sustain level")
        }
    }

    @Test
    fun `ads with attackEnd zero starts at full volume`() {
        val sampleRate = 100
        val samples = DoubleArray(100) { 1.0 }
        val result = AdsEffect(FakeAudioSource(samples), 0.0, 0.5, 0.7, sampleRate).render()
        assertEquals(1.0, result[0], 0.01)
    }

    // ── Stacked decorators ────────────────────────────────────────────────────

    @Test
    fun `clip then volume stack applies effects in order`() {
        val source = FakeAudioSource(doubleArrayOf(1.0, -1.0))
        val clipped = ClipDistortionEffect(source, 0.5)   // ±1.0 → ±0.5
        val result = VolumeEffect(clipped, 0.5).render()  // ±0.5 → ±0.25
        assertEquals(0.25, result[0], 1e-9)
        assertEquals(-0.25, result[1], 1e-9)
    }

    @Test
    fun `volume then tanh stack applies effects in order`() {
        val source = FakeAudioSource(doubleArrayOf(0.5))
        val vol = VolumeEffect(source, 2.0)                  // 0.5 → 1.0
        val result = TanhDistortionEffect(vol, 2.0).render() // tanh(1.0 * 2.0) = tanh(2.0)
        assertEquals(tanh(2.0), result[0], 1e-9)
    }

    @Test
    fun `three stacked effects all apply`() {
        val source = FakeAudioSource(doubleArrayOf(1.0))
        val clipped = ClipDistortionEffect(source, 0.8)    // 1.0 → 0.8
        val vol = VolumeEffect(clipped, 0.5)               // 0.8 → 0.4
        val result = TanhDistortionEffect(vol, 1.0).render() // tanh(0.4)
        assertEquals(tanh(0.4), result[0], 1e-9)
    }
}
