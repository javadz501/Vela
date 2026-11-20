package app.vela.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.mutableStateOf
import app.vela.core.i18n.NavStringsRegistry
import java.util.Locale

/**
 * App language preference — follow the system only when it is English or Persian; unsupported
 * system languages resolve to English. The user may explicitly choose English or Persian.
 * A process-wide reactive holder + persisted preference drives Android resources, app-originated
 * request locale, and generated on-screen navigation text.
 *
 * Resolve the locale here rather than reading `Locale.getDefault()` in navigation code.
 */
object AppLocale {
    /** "" = follow an English or Persian system locale; otherwise use English. */
    val language = mutableStateOf("")

    /** The directions language: "" = the same as the app; otherwise "en" or "fa".
     *  It controls on-screen navigation text independently of the app menus. */
    val directions = mutableStateOf("")

    /** The only languages exposed by this fork's language pickers. */
    val SUPPORTED = listOf("en", "fa")

    private val ENDONYMS = mapOf("en" to "English", "fa" to "فارسی")

    /** The language's own name (endonym) — what a speaker of it expects to see in a language list. */
    fun endonym(code: String): String = ENDONYMS[code] ?: code.replaceFirstChar { it.uppercase() }

    /** The supported language closest to the device/system locale. Used when the user turns OFF
     *  "follow system language" so the revealed picker starts on a sensible current choice instead
     *  of nothing; falls back to English when the system language isn't one Vela ships. */
    fun deviceDefaultSupported(): String =
        if (Locale.getDefault().language == "fa") "fa" else "en"

    fun init(context: Context) {
        val storedLanguage = prefs(context).getString(KEY, "").orEmpty()
        val storedDirections = prefs(context).getString(KEY_DIRECTIONS, "").orEmpty()
        language.value = normalize(storedLanguage)
        directions.value = normalize(storedDirections)
        if (language.value != storedLanguage || directions.value != storedDirections) {
            prefs(context).edit()
                .putString(KEY, language.value)
                .putString(KEY_DIRECTIONS, directions.value)
                .apply()
        }
        apply()
    }

    /** Set by the hosting Activity so a language change can re-create it (to re-read localized
     *  resources). Null until an Activity registers it. */
    var onLocaleChanged: (() -> Unit)? = null

    fun set(context: Context, langCode: String) {
        val supportedCode = normalize(langCode)
        val changed = supportedCode != language.value
        language.value = supportedCode
        prefs(context).edit().putString(KEY, supportedCode).apply()
        apply()
        // Re-create the Activity so `stringResource`/`getString` re-resolve in the new language;
        // navigation text is updated by apply().
        if (changed) onLocaleChanged?.invoke()
    }

    /** Set the on-screen navigation language ("" = the same as the app). No Activity re-create is needed. */
    fun setDirections(context: Context, langCode: String) {
        val supportedCode = normalize(langCode)
        directions.value = supportedCode
        prefs(context).edit().putString(KEY_DIRECTIONS, supportedCode).apply()
        apply()
    }

    /** The resolved directions locale: the override when set, else [effective]. */
    fun directionsEffective(): Locale = directions.value.takeIf { it.isNotBlank() }
        ?.let { Locale.forLanguageTag(it) } ?: effective()

    /** The resolved locale, restricted to English and Persian. */
    fun effective(): Locale = when (language.value) {
        "fa" -> Locale.forLanguageTag("fa")
        "en" -> Locale.ENGLISH
        else -> Locale.getDefault().takeIf { it.language in SUPPORTED } ?: Locale.ENGLISH
    }

    /** The device locale captured before the app applies its own language override. */
    private var systemDefault: Locale? = null

    /** Wrap the base context using only English or Persian. Unsupported system locales use English.
     *  The preference is read here because this runs from `attachBaseContext`, before [init]. */
    fun wrap(base: Context): Context {
        if (systemDefault == null) systemDefault = Locale.getDefault()
        val stored = prefs(base).getString(KEY, "").orEmpty()
        val lang = normalize(stored)
        if (lang != stored) prefs(base).edit().putString(KEY, lang).apply()
        val systemLocale = systemDefault ?: Locale.ENGLISH
        val locale = when (lang) {
            "fa" -> Locale.forLanguageTag("fa")
            "en" -> Locale.ENGLISH
            else -> systemLocale.takeIf { it.language in SUPPORTED } ?: Locale.ENGLISH
        }
        Locale.setDefault(locale)
        syncAcceptLanguage(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }

    /** Keep app-originated requests within the two supported interface languages. */
    private fun syncAcceptLanguage(locale: Locale) {
        app.vela.core.data.google.BrowserHeaders.acceptLanguage = app.vela.core.data.google.BrowserHeaders
            .acceptLanguageFor(listOf(locale.toLanguageTag()))
    }

    private fun normalize(code: String): String = when (code) {
        "", "en", "fa" -> code
        else -> "en"
    }

    /** Push the resolved locale into the app's locale-aware subsystems. */
    private fun apply() {
        NavStringsRegistry.setLocale(directionsEffective())
    }

    private fun prefs(c: Context) = c.getSharedPreferences("vela_settings", Context.MODE_PRIVATE)
    private const val KEY = "app_language"
    private const val KEY_DIRECTIONS = "directions_language"
}
