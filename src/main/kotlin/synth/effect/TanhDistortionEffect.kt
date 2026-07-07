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

    override fun render(): DoubleArray {
        val samples = renderWrapped()
        return DoubleArray(samples.size) { i -> tanh(samples[i] * drive) }
    }
}
