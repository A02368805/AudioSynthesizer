package synth.model

import synth.source.AudioSource

class Channel(private val source: AudioSource) {
    fun render(): DoubleArray = source.render()
}
