package synth.effect

import synth.source.AudioSource

abstract class AudioEffectDecorator(
    private val wrappedSource: AudioSource
) : AudioSource {
    protected fun renderWrapped(): DoubleArray = wrappedSource.render()
}
