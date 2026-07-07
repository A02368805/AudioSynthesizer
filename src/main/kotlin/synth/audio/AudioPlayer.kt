package synth.audio

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

class AudioPlayer(private val sampleRate: Int) {

    fun play(samples: DoubleArray) {
        val pcm = convertToPCM(samples)
        val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, false)
        val line = AudioSystem.getSourceDataLine(format)
        line.open(format)
        line.start()
        line.write(pcm, 0, pcm.size)
        line.drain()
        line.close()
    }

    internal fun convertToPCM(samples: DoubleArray): ByteArray {
        val pcm = ByteArray(samples.size * 2)
        samples.forEachIndexed { i, sample ->
            val clamped = sample.coerceIn(-1.0, 1.0)
            val shortValue = (clamped * Short.MAX_VALUE).toInt().toShort()
            pcm[i * 2] = (shortValue.toInt() and 0xff).toByte()
            pcm[i * 2 + 1] = ((shortValue.toInt() shr 8) and 0xff).toByte()
        }
        return pcm
    }
}
