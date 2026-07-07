package synth.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SongHeaderTest {

    @Test
    fun `beatDurationSeconds for tempo 120 is 0 point 5`() {
        val header = SongHeader(44100, 4, 120.0)
        assertEquals(0.5, header.beatDurationSeconds(), 1e-9)
    }

    @Test
    fun `beatsToSeconds two beats at tempo 120 is 1 second`() {
        val header = SongHeader(44100, 4, 120.0)
        assertEquals(1.0, header.beatsToSeconds(2.0), 1e-9)
    }

    @Test
    fun `beatDurationSeconds for tempo 90`() {
        val header = SongHeader(44100, 3, 90.0)
        assertEquals(60.0 / 90.0, header.beatDurationSeconds(), 1e-9)
    }

    @Test
    fun `beatsToSeconds three beats at tempo 90`() {
        val header = SongHeader(44100, 3, 90.0)
        val expected = 3.0 * (60.0 / 90.0)
        assertEquals(expected, header.beatsToSeconds(3.0), 1e-9)
    }

    @Test
    fun `header stores provided values`() {
        val header = SongHeader(22050, 4, 60.0)
        assertEquals(22050, header.sampleRate)
        assertEquals(4, header.beatsPerMeasure)
        assertEquals(60.0, header.tempo, 1e-9)
    }
}
