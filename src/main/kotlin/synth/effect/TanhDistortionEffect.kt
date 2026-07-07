package synth.effect

import synth.source.AudioSource
import kotlin.math.tanh

class TanhDistortionEffect(
    source: AudioSource,
    private val drive: Double
) : AudioEffectDecorator(source) {

    init {
        require(drive > 0.0) { "Drive must be > 0.0, got $drive" }
    }

    override fun renderSegments(): List<DoubleArray> =
        renderWrappedSegments().map { seg -> DoubleArray(seg.size) { i -> tanh(seg[i] * drive) } }

    override fun render(): DoubleArray = concatenate(renderSegments())
}
