package synth.source

interface SegmentedAudioSource : AudioSource {
    fun renderSegments(): List<DoubleArray>
}
