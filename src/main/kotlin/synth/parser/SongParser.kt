package synth.parser

import synth.effect.AdsEffect
import synth.effect.ClipDistortionEffect
import synth.effect.TanhDistortionEffect
import synth.effect.VolumeEffect
import synth.model.Channel
import synth.model.Measure
import synth.model.NoteEvent
import synth.model.Song
import synth.model.SongHeader
import synth.notes.PianoNotes
import synth.source.AudioSource
import synth.source.BasicAudioSource
import synth.waveform.SawWaveStrategy
import synth.waveform.SineWaveStrategy
import synth.waveform.SquareWaveStrategy
import synth.waveform.WaveformStrategy
import synth.waveform.WhiteNoiseStrategy
import java.io.File

class SongParser {

    fun parse(filePath: String): Song {
        val file = File(filePath)
        if (!file.exists()) {
            throw SongParseException("Could not read input file: $filePath")
        }
        val lines = file.readLines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            throw SongParseException("Input file is empty: $filePath")
        }
        val header = parseHeader(lines[0])
        val channels = lines.drop(1).mapIndexed { index, line ->
            parseChannel(line, header, lineNum = index + 2)
        }
        return Song(header, channels)
    }

    private fun parseHeader(line: String): SongHeader {
        val tokens = line.trim().split(Regex("\\s+"))
        if (tokens.size != 3) {
            throw SongParseException(
                "Line 1: header must contain sampleRate beatsPerMeasure tempo, got ${tokens.size} token(s)."
            )
        }
        val sampleRate = tokens[0].toIntOrNull()?.takeIf { it > 0 }
            ?: throw SongParseException("Line 1: sampleRate '${tokens[0]}' must be a positive integer.")
        val beatsPerMeasure = tokens[1].toIntOrNull()?.takeIf { it > 0 }
            ?: throw SongParseException("Line 1: beatsPerMeasure '${tokens[1]}' must be a positive integer.")
        val tempo = tokens[2].toDoubleOrNull()?.takeIf { it > 0.0 }
            ?: throw SongParseException("Line 1: tempo '${tokens[2]}' must be a positive number.")
        return SongHeader(sampleRate, beatsPerMeasure, tempo)
    }

    private fun parseChannel(line: String, header: SongHeader, lineNum: Int): Channel {
        val segments = line.split("|")
        val settingsSegment = segments[0].trim()
        val measureSegments = segments.drop(1).filter { it.isNotBlank() }

        if (measureSegments.isEmpty()) {
            throw SongParseException("Line $lineNum: channel has no measures. At least one measure is required after the settings segment.")
        }

        val settingsTokens = settingsSegment.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (settingsTokens.isEmpty()) {
            throw SongParseException("Line $lineNum: channel settings are empty.")
        }

        val waveformToken = settingsTokens[0]
        val effectTokens = settingsTokens.drop(1)

        val waveform = parseWaveform(waveformToken, lineNum)

        val allNotes = mutableListOf<NoteEvent>()
        measureSegments.forEachIndexed { mIdx, segment ->
            val measure = parseMeasure(segment.trim(), lineNum, mIdx + 1)
            allNotes.addAll(measure.getNoteEvents())
        }

        var source: AudioSource = BasicAudioSource(waveform, allNotes, header)
        source = applyEffects(effectTokens, source, header, lineNum)

        return Channel(source)
    }

    private fun parseWaveform(token: String, lineNum: Int): WaveformStrategy {
        return when (token) {
            "sin" -> SineWaveStrategy()
            "square" -> SquareWaveStrategy()
            "saw" -> SawWaveStrategy()
            "whitenoise" -> WhiteNoiseStrategy()
            else -> throw SongParseException(
                "Line $lineNum: unknown waveform '$token'. Expected sin, square, saw, or whitenoise."
            )
        }
    }

    private fun applyEffects(
        tokens: List<String>,
        initialSource: AudioSource,
        header: SongHeader,
        lineNum: Int
    ): AudioSource {
        var source = initialSource
        for (token in tokens) {
            source = buildEffect(token, source, header, lineNum)
        }
        return source
    }

    private fun buildEffect(
        token: String,
        source: AudioSource,
        header: SongHeader,
        lineNum: Int
    ): AudioSource {
        val parts = token.split("$")
        return when (parts[0]) {
            "vol" -> {
                if (parts.size != 2) throw SongParseException(
                    "Line $lineNum: effect 'vol' requires 1 argument (\$level), got ${parts.size - 1}."
                )
                val level = parts[1].toDoubleOrNull()
                    ?: throw SongParseException(
                        "Line $lineNum: vol level '${parts[1]}' is not a valid number."
                    )
                if (level < 0.0) throw SongParseException(
                    "Line $lineNum: vol level '$level' must be >= 0.0."
                )
                VolumeEffect(source, level)
            }
            "ads" -> {
                if (parts.size != 4) throw SongParseException(
                    "Line $lineNum: effect 'ads' requires 3 arguments (\$attackEnd\$decayEnd\$sustain), got ${parts.size - 1}."
                )
                val attackEnd = parts[1].toDoubleOrNull()
                    ?: throw SongParseException(
                        "Line $lineNum: ads attackEnd '${parts[1]}' is not a valid number."
                    )
                val decayEnd = parts[2].toDoubleOrNull()
                    ?: throw SongParseException(
                        "Line $lineNum: ads decayEnd '${parts[2]}' is not a valid number."
                    )
                val sustain = parts[3].toDoubleOrNull()
                    ?: throw SongParseException(
                        "Line $lineNum: ads sustain '${parts[3]}' is not a valid number."
                    )
                if (attackEnd < 0.0) throw SongParseException(
                    "Line $lineNum: ads attackEnd must be >= 0.0."
                )
                if (decayEnd < attackEnd) throw SongParseException(
                    "Line $lineNum: ads decayEnd must be >= attackEnd."
                )
                if (sustain < 0.0 || sustain > 1.0) throw SongParseException(
                    "Line $lineNum: ads sustain must be in [0.0, 1.0]."
                )
                AdsEffect(source, attackEnd, decayEnd, sustain, header.sampleRate)
            }
            "tanh" -> {
                if (parts.size != 2) throw SongParseException(
                    "Line $lineNum: effect 'tanh' requires 1 argument (\$drive), got ${parts.size - 1}."
                )
                val drive = parts[1].toDoubleOrNull()
                    ?: throw SongParseException(
                        "Line $lineNum: tanh drive '${parts[1]}' is not a valid number."
                    )
                if (drive <= 0.0) throw SongParseException(
                    "Line $lineNum: tanh drive must be > 0.0."
                )
                TanhDistortionEffect(source, drive)
            }
            "clip" -> {
                if (parts.size != 2) throw SongParseException(
                    "Line $lineNum: effect 'clip' requires 1 argument (\$threshold), got ${parts.size - 1}."
                )
                val threshold = parts[1].toDoubleOrNull()
                    ?: throw SongParseException(
                        "Line $lineNum: clip threshold '${parts[1]}' is not a valid number."
                    )
                if (threshold <= 0.0 || threshold > 1.0) throw SongParseException(
                    "Line $lineNum: clip threshold must be in (0.0, 1.0]."
                )
                ClipDistortionEffect(source, threshold)
            }
            else -> throw SongParseException(
                "Line $lineNum: unknown effect '${parts[0]}'. Expected vol, ads, tanh, or clip."
            )
        }
    }

    private fun parseMeasure(segment: String, lineNum: Int, measureNum: Int): Measure {
        val tokens = segment.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.size % 2 != 0) {
            throw SongParseException(
                "Line $lineNum, measure $measureNum: note/duration tokens must come in pairs, got ${tokens.size} token(s)."
            )
        }
        val noteEvents = mutableListOf<NoteEvent>()
        var i = 0
        while (i < tokens.size) {
            val noteName = tokens[i]
            val durationStr = tokens[i + 1]
            val duration = durationStr.toDoubleOrNull()
                ?: throw SongParseException(
                    "Line $lineNum, measure $measureNum: duration '$durationStr' is not a valid number."
                )
            if (duration <= 0.0) throw SongParseException(
                "Line $lineNum, measure $measureNum: duration '$durationStr' must be positive."
            )
            if (!PianoNotes.isValidNote(noteName)) throw SongParseException(
                "Line $lineNum, measure $measureNum: unknown note '$noteName'."
            )
            noteEvents.add(NoteEvent(noteName, duration))
            i += 2
        }
        return Measure(noteEvents)
    }
}
