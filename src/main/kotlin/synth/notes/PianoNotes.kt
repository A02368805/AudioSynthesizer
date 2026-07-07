package synth.notes

object PianoNotes {

    private val semitoneOffsets = mapOf(
        "C" to 0, "C#" to 1, "Db" to 1,
        "D" to 2, "D#" to 3, "Eb" to 3,
        "E" to 4,
        "F" to 5, "F#" to 6, "Gb" to 6,
        "G" to 7, "G#" to 8, "Ab" to 8,
        "A" to 9, "A#" to 10, "Bb" to 10,
        "B" to 11
    )

    private val noteRegex = Regex("^([A-Ga-g][#b]?)([0-8])$")

    fun frequency(noteName: String): Double {
        if (noteName == "-") return 0.0
        val (note, octave) = parseNote(noteName)
        val offset = semitoneOffsets[note]
            ?: throw IllegalArgumentException("Unknown note: $noteName")
        // MIDI numbering: C4 = 60, A4 = 69
        val midiNote = (octave + 1) * 12 + offset
        return 440.0 * Math.pow(2.0, (midiNote - 69).toDouble() / 12.0)
    }

    fun isValidNote(noteName: String): Boolean {
        if (noteName == "-") return true
        return try {
            val (note, _) = parseNote(noteName)
            semitoneOffsets.containsKey(note)
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun parseNote(noteName: String): Pair<String, Int> {
        val match = noteRegex.matchEntire(noteName)
            ?: throw IllegalArgumentException("Invalid note format: $noteName")
        val note = match.groupValues[1].replaceFirstChar { it.uppercaseChar() }
        val octave = match.groupValues[2].toInt()
        return note to octave
    }
}
