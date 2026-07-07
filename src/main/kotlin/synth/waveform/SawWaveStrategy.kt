package synth.waveform

import kotlin.math.PI
import kotlin.math.roundToInt

class SawWaveStrategy : WaveformStrategy {
    override fun generate(frequency: Double, durationSeconds: Double, sampleRate: Int): DoubleArray {
        val sampleCount = (durationSeconds * sampleRate).roundToInt()
        return DoubleArray(sampleCount) { n ->
            val phase = (2.0 * PI * frequency * n / sampleRate) % (2.0 * PI)
            2.0 * (phase / (2.0 * PI)) - 1.0
        }
    }
}
