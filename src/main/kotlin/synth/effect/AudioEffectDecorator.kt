package synth.effect

import synth.source.AudioSource
import synth.source.SegmentedAudioSource

abstract class AudioEffectDecorator(
    private val wrappedSource: AudioSource
) : SegmentedAudioSource {

    protected fun renderWrapped(): DoubleArray = wrappedSource.render()

    protected fun renderWrappedSegments(): List<DoubleArray> =
        if (wrappedSource is SegmentedAudioSource) wrappedSource.renderSegments()
        else listOf(wrappedSource.render())

    protected fun concatenate(segments: List<DoubleArray>): DoubleArray {
        val result = DoubleArray(segments.sumOf { it.size })
        var offset = 0
        for (seg in segments) {
            seg.copyInto(result, offset)
            offset += seg.size
        }
        return result
    }
}
