package synth.app

import synth.audio.AudioMixer
import synth.audio.AudioPlayer
import synth.parser.SongParser

class AudioSynthesizer {

    private val parser = SongParser()
    private val mixer = AudioMixer()

    fun run(filePath: String) {
        val song = parser.parse(filePath)
        val channelSamples = song.renderChannels()
        val mixed = mixer.mix(channelSamples)
        AudioPlayer(song.header.sampleRate).play(mixed)
    }
}
