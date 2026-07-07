package synth.model

class Song(
    val header: SongHeader,
    private val channels: List<Channel>
) {
    fun getChannels(): List<Channel> = channels
    fun renderChannels(): List<DoubleArray> = channels.map { it.render() }
}
