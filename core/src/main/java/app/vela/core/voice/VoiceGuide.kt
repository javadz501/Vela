package app.vela.core.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/** Speech synthesis is disabled. This class retains only short, locally synthesized alert tones. */
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
    @Volatile var alertsOnly: Boolean = true
    @Volatile private var volume: Float = 1.0f

    private val audioExecutor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "Vela-AudioAlerts").apply { isDaemon = true }
    }

    fun setVolume(v: Float) { volume = v.coerceIn(0.2f, 1.0f) }
    fun setRate(rate: Float) = Unit

    fun reroutingChime() = chime(listOf(659.25 to 140, 440.0 to 190))
    fun fasterRouteChime() = chime(listOf(587.33 to 120, 880.0 to 200))
    fun placeTone(notes: List<Pair<Double, Int>>) = chime(notes)
    fun alertChime() = chime(listOf(1046.5 to 110, 1046.5 to 110))

    /** Generate short sine-wave tones in-process; no audio assets, TTS engine or model required. */
    private fun chime(notes: List<Pair<Double, Int>>) {
        val valid = notes.filter { (hz, ms) -> hz.isFinite() && hz in 40.0..10_000.0 && ms in 20..1000 }
        if (valid.isEmpty()) return
        audioExecutor.execute {
            var track: AudioTrack? = null
            try {
                val sampleRate = 22_050
                val gapSamples = sampleRate * 30 / 1000
                val counts = valid.map { sampleRate * it.second / 1000 }
                val pcm = ShortArray(counts.sum() + gapSamples * (valid.size - 1))
                var offset = 0
                val gain = volume.coerceIn(0.0f, 1.0f).toDouble()
                valid.forEachIndexed { index, note ->
                    val count = counts[index]
                    for (i in 0 until count) {
                        val fade = minOf(1.0, i / (sampleRate * 0.012), (count - 1 - i) / (sampleRate * 0.012))
                        val sample = kotlin.math.sin(2.0 * Math.PI * note.first * i / sampleRate)
                        pcm[offset + i] = (sample * 9_500.0 * fade * gain).toInt().toShort()
                    }
                    offset += count + if (index < valid.lastIndex) gapSamples else 0
                }
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(pcm.size * 2)
                    .build()
                if (track.state != AudioTrack.STATE_INITIALIZED) return@execute
                val written = track.write(pcm, 0, pcm.size)
                if (written <= 0) return@execute
                track.play()
                Thread.sleep((written * 1000L / sampleRate) + 80L)
            } catch (interrupted: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (_: Throwable) {
                // A failed sound must never interrupt navigation or route calculation.
            } finally {
                runCatching { track?.release() }
            }
        }
    }

    /** Speech is intentionally unavailable in this fork. */
    fun init(enginePackage: String? = null) = Unit
    fun test() = Unit
    fun availableEngines(): List<VoiceEngine> = emptyList()
    fun speak(text: String, interrupt: Boolean = false, ignoreMute: Boolean = false) = Unit
    fun prepare(text: String, onDone: () -> Unit = {}) = onDone()
    fun speakOpener(text: String) = Unit
    fun stop() = Unit
    fun shutdown() = Unit
}
