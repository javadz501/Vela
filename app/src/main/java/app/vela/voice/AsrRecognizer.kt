package app.vela.voice

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Compatibility no-op: speech recognition is not shipped in this fork. */
@Singleton
class AsrRecognizer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun release() = Unit
    fun clearQuarantine(engine: AsrEngine = AsrEngine.DEFAULT) = Unit
    fun isInstalled(): Boolean = false
    fun hasMicPermission(): Boolean = false
    fun warmUp() = Unit

    suspend fun listen(
        onLevel: (Float) -> Unit,
        onListening: () -> Unit,
        canceled: () -> Boolean,
    ): VoiceResult = withContext(Dispatchers.Default) {
        VoiceResult.Failed(VoiceResult.Reason.MODEL, "Speech recognition is disabled in this build")
    }
}
