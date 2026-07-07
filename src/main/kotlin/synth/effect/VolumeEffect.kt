package synth.effect

import synth.source.AudioSource

class VolumeEffect(
    source: AudioSource,
    private val level: Double
) : AudioEffectDecorator(source) {

    init {
        require(level >= 0.0) { "Volume level must be >= 0.0, got $level" }
    }

    override fun renderSegments(): List<DoubleArray> =
        renderWrappedSegments().map { seg -> DoubleArray(seg.size) { i -> seg[i] * level } }

    override fun render(): DoubleArray = concatenate(renderSegments())
}
