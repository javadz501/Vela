package app.vela.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/**
 * Whether Vela plays a short tone when you are over the posted limit while navigating (issue
 * #404). Off by default: being told off by your phone is an opt-in. Timing lives in :core
 * [app.vela.core.nav.SpeedingAlerts]; the posted limit is whatever the speed widget shows
 * (the offline graph's maxspeed, else the online overlay), so it only speaks where a limit is
 * known. Uses a non-verbal tone; TTS is not used.
 */
object SpeedingAlert {
    val on = mutableStateOf(true)

    fun init(context: Context) {
        on.value = prefs(context).getBoolean(KEY, true)
    }

    fun set(context: Context, value: Boolean) {
        on.value = value
        prefs(context).edit().putBoolean(KEY, value).apply()
    }

    private fun prefs(c: Context) = c.getSharedPreferences("vela_settings", Context.MODE_PRIVATE)

    private const val KEY = "speeding_alert"
}
