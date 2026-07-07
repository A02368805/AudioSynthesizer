package synth.waveform

import kotlin.math.PI
import kotlin.math.roundToInt

class SquareWaveStrategy : WaveformStrategy {
    override fun generate(frequency: Double, durationSeconds: Double, sampleRate: Int): DoubleArray {
        val sampleCount = (durationSeconds * sampleRate).roundToInt()
        return DoubleArray(sampleCount) { n ->
            val phase = (2.0 * PI * frequency * n / sampleRate) % (2.0 * PI)
            if (phase < PI) 1.0 else -1.0
        }
    }
}
