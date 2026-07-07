package synth.waveform

interface WaveformStrategy {
    fun generate(frequency: Double, durationSeconds: Double, sampleRate: Int): DoubleArray
}
