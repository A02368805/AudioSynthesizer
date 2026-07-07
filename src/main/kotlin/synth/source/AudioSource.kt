package synth.source

interface AudioSource {
    fun render(): DoubleArray
}
