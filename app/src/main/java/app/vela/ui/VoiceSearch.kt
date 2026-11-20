package app.vela.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/** Voice search is disabled in this fork; text search remains available. */
object VoiceSearch {
    val enabled = mutableStateOf(false)
    val engine = mutableStateOf(Engine.AUTO)
    val provider = mutableStateOf<String?>(null)

    enum class Engine { AUTO, LOCAL, SYSTEM }
    enum class Mode { LOCAL, SYSTEM, NONE }
    data class Provider(val label: String, val component: android.content.ComponentName)

    fun init(context: Context) { enabled.value = false }
    fun set(context: Context, value: Boolean) { enabled.value = false }
    fun setEngine(context: Context, value: Engine) { engine.value = value }
    fun providers(context: Context): List<Provider> = emptyList()
    fun launchComponent(context: Context): android.content.ComponentName? = null
    fun chosenProvider(context: Context): Provider? = null
    fun setProvider(context: Context, component: android.content.ComponentName) = Unit
    fun clearProvider(context: Context) { provider.value = null }
    fun hasProvider(context: Context): Boolean = false
    fun localReady(context: Context): Boolean = false
    fun resolvedMode(context: Context): Mode = Mode.NONE
}
