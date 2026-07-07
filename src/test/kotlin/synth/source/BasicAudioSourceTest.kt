package synth.source

import synth.model.NoteEvent
import synth.model.SongHeader
import synth.waveform.SineWaveStrategy
import synth.waveform.WhiteNoiseStrategy
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BasicAudioSourceTest {

    // 120 BPM → 0.5 s per beat
    private val header = SongHeader(44100, 4, 120.0)

    @Test
    fun `single note A4 two beats renders expected sample count`() {
        val notes = listOf(NoteEvent("A4", 2.0))   // 2 beats × 0.5 s = 1 second
        val source = BasicAudioSource(SineWaveStrategy(), notes, header)
        val expected = (1.0 * 44100).roundToInt()
        assertEquals(expected, source.render().size)
    }

    @Test
    fun `two notes concatenate to correct total length`() {
        val notes = listOf(NoteEvent("A4", 1.0), NoteEvent("C4", 1.0))  // 0.5 s each
        val source = BasicAudioSource(SineWaveStrategy(), notes, header)
        val perNote = (0.5 * 44100).roundToInt()
        assertEquals(perNote * 2, source.render().size)
    }

    @Test
    fun `rest note renders all zeros`() {
        val notes = listOf(NoteEvent("-", 2.0))
        val source = BasicAudioSource(SineWaveStrategy(), notes, header)
        assertTrue(source.render().all { it == 0.0 })
    }

    @Test
    fun `whitenoise rest still renders all zeros`() {
        val notes = listOf(NoteEvent("-", 2.0))
        val source = BasicAudioSource(WhiteNoiseStrategy(), notes, header)
        assertTrue(source.render().all { it == 0.0 }, "Whitenoise rest must be silent")
    }

    @Test
    fun `rest followed by note has correct total length`() {
        val notes = listOf(NoteEvent("-", 1.0), NoteEvent("A4", 1.0))
        val source = BasicAudioSource(SineWaveStrategy(), notes, header)
        val perBeat = (0.5 * 44100).roundToInt()
        assertEquals(perBeat * 2, source.render().size)
    }

    @Test
    fun `render is consistent across multiple calls`() {
        val notes = listOf(NoteEvent("A4", 1.0))
        val source = BasicAudioSource(SineWaveStrategy(), notes, header)
        val first = source.render()
        val second = source.render()
        assertEquals(first.size, second.size)
        for (i in first.indices) {
            assertEquals(first[i], second[i], 1e-12)
        }
    }
}
