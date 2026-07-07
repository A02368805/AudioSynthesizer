package synth.waveform

import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

class SineWaveStrategy : WaveformStrategy {
    override fun generate(frequency: Double, durationSeconds: Double, sampleRate: Int): DoubleArray {
        val sampleCount = (durationSeconds * sampleRate).roundToInt()
        return DoubleArray(sampleCount) { n ->
            sin(2.0 * PI * frequency * n / sampleRate)
        }
    }
}
