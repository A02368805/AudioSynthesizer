package synth.parser

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SongParserTest {

    private val parser = SongParser()

    private fun withTempFile(content: String, block: (String) -> Unit) {
        val file = File.createTempFile("song_test_", ".txt")
        try {
            file.writeText(content)
            block(file.absolutePath)
        } finally {
            file.delete()
        }
    }

    // ── Valid inputs ──────────────────────────────────────────────────────────

    @Test
    fun `parses valid header values`() {
        withTempFile("44100 3 90\nsin |- 1|") { path ->
            val song = parser.parse(path)
            assertEquals(44100, song.header.sampleRate)
            assertEquals(3, song.header.beatsPerMeasure)
            assertEquals(90.0, song.header.tempo, 1e-9)
        }
    }

    @Test
    fun `parses channel with no effects`() {
        withTempFile("44100 4 120\nsin |A4 1|") { path ->
            assertEquals(1, parser.parse(path).getChannels().size)
        }
    }

    @Test
    fun `parses channel with all four effects`() {
        withTempFile(
            "44100 4 120\nsin vol\$0.8 ads\$0.02\$0.5\$0.6 tanh\$2.0 clip\$0.9|A4 1|"
        ) { path ->
            assertNotNull(parser.parse(path))
        }
    }

    @Test
    fun `handles trailing pipe`() {
        withTempFile("44100 4 120\nsin |A4 1|") { path ->
            assertEquals(1, parser.parse(path).getChannels().size)
        }
    }

    @Test
    fun `parses fractional durations`() {
        withTempFile("44100 4 120\nsin |A4 0.5 C4 1.5|") { path ->
            assertNotNull(parser.parse(path))
        }
    }

    @Test
    fun `parses rest note dash`() {
        withTempFile("44100 4 120\nsin |- 2 A4 1|") { path ->
            assertNotNull(parser.parse(path))
        }
    }

    @Test
    fun `parses multiple channels`() {
        withTempFile("44100 4 120\nsin |A4 1|\nsaw |C4 1|") { path ->
            assertEquals(2, parser.parse(path).getChannels().size)
        }
    }

    @Test
    fun `parses all four waveforms`() {
        for (waveform in listOf("sin", "square", "saw", "whitenoise")) {
            withTempFile("44100 4 120\n$waveform |A4 1|") { path ->
                assertNotNull(parser.parse(path), "Failed for waveform: $waveform")
            }
        }
    }

    @Test
    fun `parses note with sharp`() {
        withTempFile("44100 4 120\nsin |C#4 1|") { path ->
            assertNotNull(parser.parse(path))
        }
    }

    @Test
    fun `parses note with flat`() {
        withTempFile("44100 4 120\nsin |Bb3 1|") { path ->
            assertNotNull(parser.parse(path))
        }
    }

    // ── Invalid inputs ────────────────────────────────────────────────────────

    @Test
    fun `throws on missing file`() {
        val ex = assertFailsWith<SongParseException> { parser.parse("no_such_file.txt") }
        assertTrue(ex.message!!.contains("no_such_file.txt"))
    }

    @Test
    fun `throws on empty file`() {
        withTempFile("") { path ->
            assertFailsWith<SongParseException> { parser.parse(path) }
        }
    }

    @Test
    fun `throws on header with too few tokens`() {
        withTempFile("44100 3\nsin |A4 1|") { path ->
            val ex = assertFailsWith<SongParseException> { parser.parse(path) }
            assertTrue(ex.message!!.contains("header"))
        }
    }

    @Test
    fun `throws on header with too many tokens`() {
        withTempFile("44100 3 90 extra\nsin |A4 1|") { path ->
            val ex = assertFailsWith<SongParseException> { parser.parse(path) }
            assertTrue(ex.message!!.contains("header"))
        }
    }

    @Test
    fun `throws on non-numeric sample rate`() {
        withTempFile("abc 3 90\nsin |A4 1|") { path ->
            val ex = assertFailsWith<SongParseException> { parser.parse(path) }
            assertTrue(ex.message!!.contains("sampleRate"))
        }
    }

    @Test
    fun `throws on unknown waveform`() {
        withTempFile("44100 4 120\ntriangle |A4 1|") { path ->
            val ex = assertFailsWith<SongParseException> { parser.parse(path) }
            assertTrue(ex.message!!.contains("triangle"))
        }
    }

    @Test
    fun `throws on unknown effect name`() {
        withTempFile("44100 4 120\nsin reverb\$1.0|A4 1|") { path ->
            val ex = assertFailsWith<SongParseException> { parser.parse(path) }
            assertTrue(ex.message!!.contains("reverb"))
        }
    }

    @Test
    fun `throws on ads with wrong argument count`() {
        withTempFile("44100 4 120\nsin ads\$0.01\$0.2|A4 1|") { path ->
            val ex = assertFailsWith<SongParseException> { parser.parse(path) }
            assertTrue(ex.message!!.contains("ads"))
        }
    }

    @Test
    fun `throws on non-numeric effect argument`() {
        withTempFile("44100 4 120\nsin vol\$loud|A4 1|") { path ->
            val ex = assertFailsWith<SongParseException> { parser.parse(path) }
            assertTrue(ex.message!!.contains("loud") || ex.message!!.contains("vol"))
        }
    }

    @Test
    fun `throws on odd number of measure tokens`() {
        withTempFile("44100 4 120\nsin |A4 1 C4|") { path ->
            val ex = assertFailsWith<SongParseException> { parser.parse(path) }
            assertTrue(ex.message!!.contains("pair"))
        }
    }

    @Test
    fun `throws on unknown note`() {
        withTempFile("44100 4 120\nsin |H4 1|") { path ->
            val ex = assertFailsWith<SongParseException> { parser.parse(path) }
            assertTrue(ex.message!!.contains("H4"))
        }
    }

    @Test
    fun `throws on non-positive duration`() {
        withTempFile("44100 4 120\nsin |A4 -1|") { path ->
            val ex = assertFailsWith<SongParseException> { parser.parse(path) }
            assertTrue(ex.message!!.contains("positive"))
        }
    }

    @Test
    fun `throws on zero duration`() {
        withTempFile("44100 4 120\nsin |A4 0|") { path ->
            val ex = assertFailsWith<SongParseException> { parser.parse(path) }
            assertTrue(ex.message!!.contains("positive"))
        }
    }

    @Test
    fun `throws when channel has no measures`() {
        withTempFile("44100 4 120\nsin vol\$0.8") { path ->
            val ex = assertFailsWith<SongParseException> { parser.parse(path) }
            assertTrue(ex.message!!.contains("measure"), "Expected 'measure' in: ${ex.message}")
        }
    }
}
