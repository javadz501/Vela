package app.vela.voice

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Compatibility no-op: speech model downloads are disabled in this fork. */
@Singleton
class KokoroInstaller @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun download(
        url: String,
        destDir: File,
        sizeEst: Long,
        onExtracting: () -> Unit = {},
        active: () -> Boolean = { true },
        onProgress: (Float) -> Unit,
    ): Boolean = false
}
