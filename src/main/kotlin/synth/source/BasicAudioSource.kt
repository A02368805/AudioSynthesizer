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
) : SegmentedAudioSource {

    override fun renderSegments(): List<DoubleArray> = notes.map { note ->
        val durationSeconds = header.beatsToSeconds(note.durationBeats)
        if (note.noteName == "-") {
            DoubleArray((durationSeconds * header.sampleRate).roundToInt())
        } else {
            val frequency = PianoNotes.frequency(note.noteName)
            waveform.generate(frequency, durationSeconds, header.sampleRate)
        }
    }

    override fun render(): DoubleArray = concatenateSegments(renderSegments())

    private fun concatenateSegments(segments: List<DoubleArray>): DoubleArray {
        val result = DoubleArray(segments.sumOf { it.size })
        var offset = 0
        for (segment in segments) {
            segment.copyInto(result, offset)
            offset += segment.size
        }
        return result
    }
}
