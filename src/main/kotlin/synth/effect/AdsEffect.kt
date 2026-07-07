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

    private fun envelopeAt(time: Double): Double = when {
        attackEnd > 0.0 && time < attackEnd ->
            time / attackEnd
        decayEnd > attackEnd && time < decayEnd -> {
            val progress = (time - attackEnd) / (decayEnd - attackEnd)
            1.0 - progress * (1.0 - sustain)
        }
        else -> sustain
    }

    private fun applyEnvelope(segment: DoubleArray): DoubleArray =
        DoubleArray(segment.size) { i -> segment[i] * envelopeAt(i.toDouble() / sampleRate) }

    override fun renderSegments(): List<DoubleArray> =
        renderWrappedSegments().map { applyEnvelope(it) }

    override fun render(): DoubleArray = concatenate(renderSegments())
}
