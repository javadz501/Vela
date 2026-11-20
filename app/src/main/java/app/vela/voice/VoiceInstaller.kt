package app.vela.voice

import javax.inject.Inject
import javax.inject.Singleton

/** Compatibility no-op: external speech engines are not offered in this fork. */
@Singleton
class VoiceInstaller @Inject constructor() {
    data class Engine(val pkg: String, val label: String, val note: String)

    val engines: List<Engine> = emptyList()

    fun isInstalled(pkg: String): Boolean = false

    suspend fun installFromFDroid(pkg: String): String? =
        "Voice engines are disabled in this build"
}
