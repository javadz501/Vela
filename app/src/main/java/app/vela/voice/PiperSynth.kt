package app.vela.voice

import app.vela.core.voice.NeuralSynth
import javax.inject.Inject
import javax.inject.Singleton

/** Compatibility no-op: neural TTS is not shipped in this fork. */
@Singleton
class PiperSynth @Inject constructor() : NeuralSynth {
    override val ready: Boolean = false
    override val voiceLanguage: String? = null
    var numSpeakers: Int = 0
        private set

    override fun voiceFor(lang: String): Boolean = false
    override fun warmUp() = Unit
    override fun speak(text: String, interrupt: Boolean, onDone: () -> Unit) = onDone()
    override fun prepare(text: String, onDone: () -> Unit) = onDone()
    override fun stop() = Unit
    override fun release() = Unit

    fun reloadVoice() = Unit
    fun deleteModelDir(dir: java.io.File) { dir.deleteRecursively() }
}
