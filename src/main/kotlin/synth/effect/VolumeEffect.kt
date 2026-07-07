package synth.effect

import synth.source.AudioSource

class VolumeEffect(
    source: AudioSource,
    private val level: Double
) : AudioEffectDecorator(source) {

    init {
        require(level >= 0.0) { "Volume level must be >= 0.0, got $level" }
    }

    override fun render(): DoubleArray {
        val samples = renderWrapped()
        return DoubleArray(samples.size) { i -> samples[i] * level }
    }
}
