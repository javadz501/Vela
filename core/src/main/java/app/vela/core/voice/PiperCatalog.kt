package app.vela.core.voice

/** Voice quality tier (Piper trains low / medium / high variants of the same speaker). */
enum class VoiceQuality { LOW, MEDIUM, HIGH }

/** MULTI = a multi-speaker pack (libritts_r=904, vctk=109), audition variants in the playground. */
enum class VoiceGender { FEMALE, MALE, NEUTRAL, MULTI }

/**
 * One browsable Piper voice. [id] is simultaneously the sherpa asset id, the on-disk dir name, and
 * the `.onnx` file stem (`<id>.onnx`), the single source of truth. Its `lang_REGION` prefix
 * (`en_US`, `en_GB`) gives the [langCode] (for grouping voices by language + pairing to
 * the app locale) and the [region]. Pure data (no Android types) so it lives in `:core` and is
 * unit-tested.
 */
data class PiperVoice(
    val id: String, // e.g. "fr_FR-siwis-medium"
    val displayName: String, // "Siwis"
    val gender: VoiceGender,
    val quality: VoiceQuality,
    val sizeMb: Int, // full-precision archive size, download label + progress estimate
    val numSpeakers: Int, // 1 = single-speaker; 904 = libritts_r; …
    val note: String? = null, // one short "sounds like" line for the row
    val recommended: Boolean = false, // the best voice for its language, floated to the top of its group
    val novelty: Boolean = false, // sinks to the bottom (GLaDOS)
) {
    val sizeBytes: Long get() = sizeMb * 1_000_000L
    val multiSpeaker: Boolean get() = numSpeakers > 1

    /** Language code from the id prefix: "en" (Persian uses the phone system TTS) (for grouping + app-locale pairing). */
    val langCode: String get() = id.substringBefore('_')

    /** Region from the id prefix: "US", "GB" (shown in the row). */
    val region: String get() = id.substringBefore('-').substringAfter('_', "")
}

/**
 * The curated catalog of downloadable Piper voices, all packaged on the sherpa-onnx `tts-models`
 * GitHub release as `vits-piper-<id>.tar.bz2`. Compiled-in for v1; a future bet is to fold this into
 * the signed `calibration.json` so new voices ship without an app release (would also require pinning
 * the download host, `github.com`, in the calibration allowlist, see ROADMAP). Localization
 * ([[project_vela_i18n]]) pairs the app locale to the matching-language voice via [defaultFor].
 */
object PiperCatalog {
    private const val BASE = "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/"

    /** Download URL derived purely from the id, matches the sherpa asset naming scheme. */
    fun downloadUrl(id: String): String = "${BASE}vits-piper-$id.tar.bz2"

    /** Endonym (the language named in its own language, like Google/Apple) for a [langCode]. */
    fun languageLabel(langCode: String): String = LANGUAGE_LABELS[langCode] ?: langCode.uppercase()

    private val LANGUAGE_LABELS: Map<String, String> = mapOf(
        "en" to "English", "fa" to "فارسی",
    )

    /** The recommended default voice for a language code (used to auto-suggest a voice for the app
     *  locale). Falls back to the English default when a language has no curated voice. */
    fun defaultFor(langCode: String): PiperVoice =
        ALL.firstOrNull { it.langCode == langCode && it.recommended } ?: byId(VelaPiper.DEFAULT_VOICE_ID)!!

    /** Curated Piper voices worth offering for spoken navigation. `recommended` = the best voice for
     *  its language (the auto-suggested default; the ★-marked, top-of-group one). */
        // ── English (US) ──
        PiperVoice("en_US-hfc_female-medium", "HFC Female", VoiceGender.FEMALE, VoiceQuality.MEDIUM, 67, 1, "Bright, clear, the default, very Google-like", recommended = true),
        PiperVoice("en_US-libritts_r-medium", "LibriTTS-R", VoiceGender.MULTI, VoiceQuality.MEDIUM, 82, 904, "904 variants to pick from"),
        PiperVoice("en_US-lessac-medium", "Lessac", VoiceGender.FEMALE, VoiceQuality.MEDIUM, 67, 1, "Clear, neutral US female, the classic maps voice"),
        PiperVoice("en_US-hfc_male-medium", "HFC Male", VoiceGender.MALE, VoiceQuality.MEDIUM, 67, 1, "Even, neutral US male, clean maps-style guidance"),
        PiperVoice("en_US-ryan-high", "Ryan (HQ)", VoiceGender.MALE, VoiceQuality.HIGH, 115, 1, "Warm, confident US male at top quality"),
        PiperVoice("en_US-amy-medium", "Amy", VoiceGender.FEMALE, VoiceQuality.MEDIUM, 67, 1, "Soft, calm US female, easy on long drives"),
        PiperVoice("en_US-lessac-high", "Lessac (HQ)", VoiceGender.FEMALE, VoiceQuality.HIGH, 115, 1, "The neutral maps voice, richer top quality"),
        PiperVoice("en_US-ryan-medium", "Ryan", VoiceGender.MALE, VoiceQuality.MEDIUM, 67, 1, "Warm US male at the lighter size"),
        PiperVoice("en_US-kristin-medium", "Kristin", VoiceGender.FEMALE, VoiceQuality.MEDIUM, 67, 1, "Gentle, measured US female, relaxed and clear"),
        PiperVoice("en_US-joe-medium", "Joe", VoiceGender.MALE, VoiceQuality.MEDIUM, 67, 1, "Laid-back US male, casual, conversational"),
        PiperVoice("en_US-kusal-medium", "Kusal", VoiceGender.MALE, VoiceQuality.MEDIUM, 67, 1, "Mellow US male with a subtle accent"),
        PiperVoice("en_US-norman-medium", "Norman", VoiceGender.MALE, VoiceQuality.MEDIUM, 67, 1, "Deeper US male, grounded, authoritative"),
        PiperVoice("en_US-ljspeech-high", "LJSpeech (HQ)", VoiceGender.FEMALE, VoiceQuality.HIGH, 115, 1, "Polished audiobook-style US female"),
        PiperVoice("en_US-arctic-medium", "Arctic", VoiceGender.MULTI, VoiceQuality.MEDIUM, 80, 18, "18-voice US pack, variety in one download"),
        PiperVoice("en_US-glados-high", "GLaDOS", VoiceGender.NEUTRAL, VoiceQuality.HIGH, 115, 1, "Portal's GLaDOS, deadpan robotic, for fun", novelty = true),
        // ── English (GB) ──
        PiperVoice("en_GB-alba-medium", "Alba", VoiceGender.FEMALE, VoiceQuality.MEDIUM, 67, 1, "Scottish-tinged UK female, warm and characterful"),
        PiperVoice("en_GB-jenny_dioco-medium", "Jenny", VoiceGender.FEMALE, VoiceQuality.MEDIUM, 67, 1, "Friendly UK female, clear, natural British read"),
        PiperVoice("en_GB-cori-high", "Cori (HQ)", VoiceGender.FEMALE, VoiceQuality.HIGH, 115, 1, "Crisp UK female at top quality"),
        PiperVoice("en_GB-northern_english_male-medium", "Northern Male", VoiceGender.MALE, VoiceQuality.MEDIUM, 67, 1, "Northern-English UK male, down-to-earth"),
        PiperVoice("en_GB-alan-medium", "Alan", VoiceGender.MALE, VoiceQuality.MEDIUM, 67, 1, "Steady UK male, classic British navigation tone"),
        PiperVoice("en_GB-vctk-medium", "VCTK", VoiceGender.MULTI, VoiceQuality.MEDIUM, 80, 109, "109-voice UK pack, a huge range of accents"),
    )

    fun byId(id: String): PiperVoice? = ALL.firstOrNull { it.id == id }

    /** True when the catalog ships a downloadable voice for [langCode]. False for languages Piper
     *  can't train (Japanese: espeak-ng has no Japanese phonemizer), where spoken guidance falls to
     *  the phone's own system TTS instead of a Vela voice. Drives the "get a Vela voice" vs "add a
     *  system voice" fork in the no-voice nudge. */
    fun hasVoiceFor(langCode: String): Boolean = ALL.any { it.langCode == langCode }

    /** All language codes present, English first then by endonym, for grouping the browser. */
    fun languageCodes(): List<String> =
        ALL.map { it.langCode }.distinct().sortedWith(compareByDescending<String> { it == "en" }.thenBy { languageLabel(it) })
}
