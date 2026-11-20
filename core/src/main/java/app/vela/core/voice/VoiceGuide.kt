package app.vela.core.voice

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Speech output is disabled in this fork. Navigation remains visual and silent. */
data class VoiceEngine(val packageName: String, val label: String)

@Singleton
class VoiceGuide @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    var neural: NeuralSynth? = null
    var langUnavailable: ((String) -> Unit)? = null
    var onSpoken: ((String) -> Unit)? = null
    var onMuted: ((String) -> Unit)? = null
    var onPromptAlert: (() -> Unit)? = null
    var roadNameLatin: Map<String, String> = emptyMap()

    @Volatile var working: Boolean? = false
        private set
    @Volatile var muted: Boolean = true
    @Volatile var alertsOnly: Boolean = false

    fun setVolume(v: Float) = Unit
    fun setRate(rate: Float) = Unit
    fun reroutingChime() = Unit
    fun fasterRouteChime() = Unit
    fun placeTone(notes: List<Pair<Double, Int>>) = Unit
    fun alertChime() = Unit
    fun init(enginePackage: String? = null) = Unit
    fun test() = Unit
    fun availableEngines(): List<VoiceEngine> = emptyList()
    fun speak(text: String, interrupt: Boolean = false, ignoreMute: Boolean = false) = Unit
    fun prepare(text: String, onDone: () -> Unit = {}) = onDone()
    fun speakOpener(text: String) = Unit
    fun stop() = Unit
    fun shutdown() = Unit
}
