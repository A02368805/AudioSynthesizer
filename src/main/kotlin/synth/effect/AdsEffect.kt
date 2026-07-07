package synth.effect

import synth.source.AudioSource

class AdsEffect(
    source: AudioSource,
    private val attackEnd: Double,
    private val decayEnd: Double,
    private val sustain: Double,
    private val sampleRate: Int
) : AudioEffectDecorator(source) {

    init {
        require(attackEnd >= 0.0) { "attackEnd must be >= 0.0" }
        require(decayEnd >= attackEnd) { "decayEnd must be >= attackEnd" }
        require(sustain in 0.0..1.0) { "sustain must be in [0.0, 1.0]" }
        require(sampleRate > 0) { "sampleRate must be positive" }
    }

    override fun render(): DoubleArray {
        val samples = renderWrapped()
        return DoubleArray(samples.size) { i ->
            val time = i.toDouble() / sampleRate
            val envelope = when {
                time < attackEnd -> time / attackEnd
                time < decayEnd -> {
                    val decayDuration = decayEnd - attackEnd
                    if (decayDuration > 0.0) {
                        1.0 + (time - attackEnd) / decayDuration * (sustain - 1.0)
                    } else {
                        sustain
                    }
                }
                else -> sustain
            }
            samples[i] * envelope
        }
    }
}
