package synth.waveform

import kotlin.math.roundToInt
import kotlin.random.Random

class WhiteNoiseStrategy : WaveformStrategy {
    override fun generate(frequency: Double, durationSeconds: Double, sampleRate: Int): DoubleArray {
        val sampleCount = (durationSeconds * sampleRate).roundToInt()
        return DoubleArray(sampleCount) { Random.nextDouble(-1.0, 1.0) }
    }
}
