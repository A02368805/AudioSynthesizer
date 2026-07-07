package synth.effect

import synth.source.AudioSource

class ClipDistortionEffect(
    source: AudioSource,
    private val threshold: Double
) : AudioEffectDecorator(source) {

    init {
        require(threshold > 0.0 && threshold <= 1.0) {
            "Threshold must be in (0.0, 1.0], got $threshold"
        }
    }

    override fun renderSegments(): List<DoubleArray> =
        renderWrappedSegments().map { seg -> DoubleArray(seg.size) { i -> seg[i].coerceIn(-threshold, threshold) } }

    override fun render(): DoubleArray = concatenate(renderSegments())
}
