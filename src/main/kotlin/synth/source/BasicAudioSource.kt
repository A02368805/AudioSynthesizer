package synth.source

import synth.model.NoteEvent
import synth.model.SongHeader
import synth.notes.PianoNotes
import synth.waveform.WaveformStrategy
import kotlin.math.roundToInt

class BasicAudioSource(
    private val waveform: WaveformStrategy,
    private val notes: List<NoteEvent>,
    private val header: SongHeader
) : AudioSource {

    override fun render(): DoubleArray {
        val segments = notes.map { note ->
            val durationSeconds = header.beatsToSeconds(note.durationBeats)
            if (note.noteName == "-") {
                val sampleCount = (durationSeconds * header.sampleRate).roundToInt()
                DoubleArray(sampleCount)
            } else {
                val frequency = PianoNotes.frequency(note.noteName)
                waveform.generate(frequency, durationSeconds, header.sampleRate)
            }
        }
        val totalLength = segments.sumOf { it.size }
        val result = DoubleArray(totalLength)
        var offset = 0
        for (segment in segments) {
            segment.copyInto(result, offset)
            offset += segment.size
        }
        return result
    }
}
