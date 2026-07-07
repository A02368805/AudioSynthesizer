package synth.model

data class SongHeader(
    val sampleRate: Int,
    val beatsPerMeasure: Int,
    val tempo: Double
) {
    fun beatDurationSeconds(): Double = 60.0 / tempo
    fun beatsToSeconds(beats: Double): Double = beats * beatDurationSeconds()
}
