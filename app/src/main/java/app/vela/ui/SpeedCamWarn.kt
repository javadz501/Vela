package app.vela.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/**
 * Whether an approaching speed camera triggers a short audible tone during navigation (issue #229).
 *
 * Separate from [SpeedCams], which only draws them on the map, for two reasons. Seeing a marker
 * and hearing a tone are different enough that the warning remains independently configurable.
 * Camera-warning features are restricted in some countries, so users should disable this option
 * wherever local law requires. It is nested under the camera toggle in Settings.
 */
object SpeedCamWarn {
    val on = mutableStateOf(true)

    fun init(context: Context) {
        on.value = prefs(context).getBoolean(KEY, true)
    }

    fun set(context: Context, value: Boolean) {
        on.value = value
        prefs(context).edit().putBoolean(KEY, value).apply()
    }

    private fun prefs(c: Context) = c.getSharedPreferences("vela_settings", Context.MODE_PRIVATE)
    private const val KEY = "speed_cam_warn"
}
