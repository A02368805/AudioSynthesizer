package synth.app

import synth.parser.SongParseException

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: AudioSynthesizer <song-file>")
        return
    }
    try {
        AudioSynthesizer().run(args[0])
    } catch (e: SongParseException) {
        println("Parse error: ${e.message}")
    } catch (e: Exception) {
        println("Error: ${e.message ?: "An unexpected error occurred."}")
    }
}
