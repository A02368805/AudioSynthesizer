package synth.audio

import kotlin.math.abs

class AudioMixer {

    fun mix(channels: List<DoubleArray>): DoubleArray {
        if (channels.isEmpty()) return DoubleArray(0)
        val maxLength = channels.maxOf { it.size }
        val mixed = DoubleArray(maxLength) { i ->
            channels.sumOf { ch -> if (i < ch.size) ch[i] else 0.0 }
        }
        return normalize(mixed)
    }

    internal fun normalize(samples: DoubleArray): DoubleArray {
        val peak = samples.maxOfOrNull { abs(it) } ?: 0.0
        return if (peak > 1.0) DoubleArray(samples.size) { i -> samples[i] / peak }
        else samples
    }
}
