package app.vela.core.i18n

import app.vela.core.model.LaneSide
import app.vela.core.voice.SpeechText
import java.util.Locale
import kotlin.math.roundToInt

/**
 * All Vela-GENERATED spoken/banner nav text for ONE language. Vela builds turn instructions itself
 * (from OSRM step geometry) rather than scraping them, so localizing navigation = translating this
 * small, bounded set of templates — NOT machine-translating prose. The road/dest NAME passed in is
 * DATA (already in the local language) and is never translated; each method decides the word ORDER
 * around it (which differs by language: "Turn left onto X" versus Persian word order), which is why
 * this is per-language templates, not per-word substitution.
 *
 * Resolved by [NavStringsRegistry] (set explicitly from the app locale, never `Locale.getDefault()`,
 * because these run off the main thread — the nav loop + the TTS worker). Part of the app localization
 * effort (see the `project_vela_i18n` memory note).
 */
interface NavStrings {
    val locale: Locale

    /**
     * The full instruction for an OSRM maneuver — mirrors `RouteGeometry.osrmPhrase`. [type] is the
     * OSRM maneuver type ("turn", "off ramp", "roundabout", …, language-independent); [mod] is the OSRM
     * modifier token ("left", "slight right", "straight", …, language-independent — each language maps
     * it); [road] is the road being entered; [dest] a ramp's sign destination; [exitNo] a ramp exit
     * number; [rbExit] a roundabout exit count.
     */
    fun phrase(type: String, mod: String?, road: String?, dest: String?, exitNo: String?, rbExit: Int?): String

    /**
     * Spoken ordinal for an ordinary junction branch. This deliberately does not alter roundabout
     * or signed motorway-exit instructions. Implementations without a localized template keep the
     * router's original instruction.
     */
    fun junctionExit(ordinal: Int, direction: String, road: String?, fallbackInstruction: String): String =
        fallbackInstruction

    /** A ferry leg on a walk or a ride: EN "Take the ferry". Every language overrides it. */
    fun ferry(): String

    /** Landmark lead-in "Pass the traffic light" / "Pass N traffic lights" for Google-style guidance, prepended
     *  to a turn ("Pass the light, then turn left onto 5th Ave"). Default "" = feature omitted for that language
     *  (English-first); only added when it makes sense (1–2 signals right before a surface-street turn). */
    fun passLights(count: Int): String = ""

    /** The lights lead-in joined to the turn: "Pass the traffic light, then turn left". A language
     *  with a [passLights] phrase joins it in its own words. */
    fun lightThen(lead: String, instruction: String): String = "$lead, then " + instruction.replaceFirstChar { it.lowercaseChar() }

    /** The SPOKEN form of a sign instruction: the secondary sign destinations after a colon are
     *  dropped ("Take the ramp toward I 5 North: Vancouver British Columbia" -> "... toward I 5
     *  North"). The full sign stays on the banner; speaking it all took so long the next prompt
     *  interrupted mid-sentence (real-drive report 2026-07-16 - Google speaks only the primary
     *  destination too). Default = unchanged; English overrides (the colon convention is
     *  osrmPhrase's own formatting, so per-language overrides are safe as tables adopt it). */
    fun spokenSign(instruction: String): String = instruction

    /** A repeated prompt's SHORT form: the instruction with its sign-destination tail ("toward X")
     *  dropped. Spoken for a step's second/third announcement — the first one already named the
     *  destination, and re-reading the whole sign at every band read as spam off an exit (real-drive
     *  report 2026-07-17). Default = unchanged (a language keeps full repeats until it overrides
     *  with its own destination marker, same English-first pattern as [passLights]). */
    fun repeatShort(instruction: String): String = instruction

    /** [repeatShort] for a ramp, fork or keep, keeping the first route number on its sign: "Keep
     *  slight right toward US 50 East". The voice then names the road the banner shows. Default =
     *  [repeatShort]; a language overrides with its own sign marker. */
    fun repeatWithRoute(instruction: String): String = repeatShort(instruction)

    /** The primary sign destinations of [instruction] (before any ":"), each as written:
     *  "toward I 80 West, Davis: San Francisco" gives ["I 80 West", "Davis"]. Empty when the
     *  language does not mark the sign. */
    fun signParts(instruction: String): List<String> = emptyList()

    /** [instruction], a fork or keep whose sign heads for [route], the road the car is already
     *  on, said as staying on it: "Keep left to stay on I 80 West". Null when the language has no
     *  such line. */
    fun stayOn(instruction: String, route: String): String? = null

    /** A distance phrased for SPEECH, honoring the imperial/metric preference — "500 feet" / "150 mètres". */
    fun spokenDistance(meters: Double, imperial: Boolean): String

    /** The pre-turn frame combining a distance phrase and the instruction — EN "In X, Y" / FR "Dans X, Y". */
    fun inThen(distancePhrase: String, instruction: String): String

    /** The at-arrival spoken callout — EN "You have arrived". */
    fun arrived(): String

    /** Which side the destination is on — EN "Your destination is on the left/right". Used as
     *  the ARRIVE step's banner text and appended to the spoken arrival when OSRM sent a side. */
    fun destinationSide(left: Boolean): String

    /** Spoken when navigation begins — EN "Starting navigation. <first instruction>". */
    fun startNav(firstInstruction: String): String

    /** Spoken as each intermediate stop is passed — EN "You've reached <label>" (blank → "your stop"). */
    fun reachedStop(label: String): String

    /** A stop coming up, framed by [inThen] ("In 500 feet, <this>"). EN "<stop> will be on your
     *  right" (or left), or "<stop> will be ahead" when [left] is null (the side is not known). A
     *  blank [label] is "your stop". Built like [destinationAhead] and [destinationSide]. */
    fun stopAhead(label: String, left: Boolean?): String

    /** [instruction] (the last maneuver before a stop) with the stop after it. EN "Turn left onto
     *  Covell Boulevard, then <stop> will be on your right" (or "will be ahead"). */
    fun thenStop(instruction: String, label: String, left: Boolean?): String

    /** The last turn before a stop when it turns into the parking lot the stop sits in. EN "Turn
     *  left into the parking lot, then <stop> is on your right". [turnLeft] is the turn's
     *  direction, [left] the stop's side (null = ahead). */
    fun intoLotThen(turnLeft: Boolean, label: String, left: Boolean?): String

    /** Two turns too close together to say one at a time: [instruction] with the turn after it.
     *  EN "Turn right onto Elm Street, then turn left onto Oak Avenue". Default = [instruction]
     *  alone: a language leaves this out until it has a line of its own that reads right. */
    fun thenNext(instruction: String, next: String): String = instruction

    /** The last turn of a trip when it turns into the destination's parking lot, as the step's own
     *  text. EN "Turn left into the parking lot". The clause [intoLotThen] opens with. */
    fun intoLot(turnLeft: Boolean): String

    /** Spoken when auto-switching to a faster route — EN "Taking the faster route. <first instruction>". */
    fun fasterRoute(firstInstruction: String): String

    /** The "Test voice" sample — a short nav-style phrase to hear the selected voice. */
    fun voiceTest(): String

    /** "Rerouting" — spoken when the driver leaves the route and a new one is being fetched. */
    fun rerouting(): String = "Rerouting"

    /** The faster-route OFFER ("Faster route available, saving about N minutes"). */
    fun fasterRouteAvailable(minutes: Int): String =
        if (minutes == 1) "Faster route available, saving about a minute"
        else "Faster route available, saving about $minutes minutes"

    /** A reroute landed but couldn't route through the remaining stops. */
    fun stopsNotIncluded(): String = "Couldn't include your stops in this route. I'll keep trying."
    /** The drive was on a saved route and has left it: it goes on by the fastest way. */
    fun leftYourRoute(): String = "You left your route. Taking the fastest way."

    /** Approach cue for the FINAL destination, framed by [inThen] ("In 400 meters, <this>"). */
    fun destinationAhead(): String = "Your destination will be ahead"

    /** The same cue when the destination's side is known: EN "Your destination will be on the
     *  right". A null [left] is [destinationAhead]. The arrival then says [destinationSide], so
     *  the two agree. */
    fun destinationWillBe(left: Boolean?): String

    /** A spoken lane recommendation — EN "Use the right 2 lanes" / "Use the left lane". */
    fun useLanes(side: LaneSide, count: Int): String

    /** Lane guidance spoken as a PREFACE to the maneuver (Google-style: "Use the right 2 lanes to take
     *  exit 172 toward Sacramento"), rather than appended after it. Default is a safe two-sentence,
     *  lanes-first form that works in every language; [EnNavStrings] overrides it with the smooth
     *  "…to <maneuver>" connective. */
    fun useLanesToDo(side: LaneSide, count: Int, instruction: String): String =
        useLanes(side, count) + ". " + instruction

    /**
     * Expand road abbreviations + numbers so the TTS engine SAYS them ("St"→"Street", "128th"→"one
     * twenty-eighth"). English-specific, so it's **opt-in**: the default is identity, and ONLY
     * [EnNavStrings] overrides it. Other languages must leave the text — including road-name DATA —
     * untouched, so an English rule can never mangle a foreign name (foreign road names remain untouched).
     */
    fun expandForSpeech(text: String): String = text
}

/** English (source of truth) — byte-identical to the original `osrmPhrase`, so existing nav tests pass. */
object EnNavStrings : NavStrings {
    override fun ferry() = "Take the ferry"
    override val locale: Locale = Locale.US

    override fun junctionExit(ordinal: Int, direction: String, road: String?, fallbackInstruction: String): String {
        val side = when (direction) {
            "right" -> "to the right"
            "left" -> "to the left"
            else -> "straight ahead"
        }
        val onto = road?.let { " onto $it" }.orEmpty()
        return "At the next junction, take the ${enOrdinal(ordinal)} exit $side$onto"
    }

    override fun phrase(type: String, mod: String?, road: String?, dest: String?, exitNo: String?, rbExit: Int?): String {
        val onto = if (road != null) " onto $road" else ""
        val m = (mod ?: "").trim()
        val toward = when {
            dest != null -> " toward $dest"
            road != null -> " onto $road"
            else -> ""
        }
        val exitTab = if (exitNo != null) " $exitNo" else ""
        return when (type) {
            "depart" -> if (road != null) "Head out on $road" else "Start your route"
            "arrive" -> "Arrive at your destination"
            "turn", "end of road" -> ("Turn $m").trim() + onto
            "continue", "new name" -> if (m.isNotBlank() && m != "straight") ("Bear $m").trim() + onto else "Continue$onto"
            "merge" -> "Merge$toward"
            "on ramp", "ramp" -> when {
                mod?.contains("right") == true -> "Take the ramp on the right$toward"
                mod?.contains("left") == true -> "Take the ramp on the left$toward"
                else -> "Take the ramp$toward"
            }
            "off ramp" -> if (exitNo != null) "Take exit$exitTab$toward" else "Take the exit$toward"
            "fork" -> ("Keep $m").trim() + toward
            // Ordinal, like every other language here and like Google ("take the 2nd exit"): "take
            // exit 2" read as a numbered motorway exit (user 2026-09-05). Straight through is named
            // as such, with the exit count kept as the confirmation.
            "roundabout", "rotary", "exit roundabout", "exit rotary" -> when {
                rbExit == null -> "Enter the roundabout$onto"
                mod == "straight" -> "At the roundabout, go straight through$onto, the ${enOrdinal(rbExit)} exit"
                else -> "At the roundabout, take the ${enOrdinal(rbExit)} exit$onto"
            }
            "roundabout turn" -> ("At the roundabout, turn $m").trim() + onto
            "uturn" -> "Make a U-turn$onto"
            else -> if (m.isNotBlank()) ("Turn $m").trim() + onto else "Continue$onto"
        }
    }

    // Feet under ~0.15 mi, else miles; meters under ~1 km, else kilometers (unchanged from NavEngine).
    override fun spokenDistance(meters: Double, imperial: Boolean): String = if (imperial) {
        val feet = meters * 3.28084
        if (feet < 800) "${(if (feet < 100) maxOf(10, (feet / 10).roundToInt() * 10) else (feet / 50).roundToInt() * 50)} feet"
        else {
            // Sub-mile distances SPEAK at quarter-mile granularity (user 2026-07-16: the voice
            // read "zero point five miles"; a human says "half a mile"). Google phrases the
            // whole band this way; the banner keeps the exact figure. Above a mile, decimals
            // read fine ("1.2 miles").
            when ((meters / 402.336).roundToInt().coerceAtLeast(1)) {
                1 -> "a quarter mile"
                2 -> "half a mile"
                3 -> "three quarters of a mile"
                else -> {
                    val miles = (meters / 1609.34 * 10).roundToInt() / 10.0
                    if (miles == 1.0) "1 mile" else "$miles miles"
                }
            }
        }
    } else {
        if (meters < 950) "${(meters / 10).roundToInt() * 10} meters"
        else {
            val km = (meters / 100).roundToInt() / 10.0
            if (km == 1.0) "1 kilometer" else "$km kilometers"
        }
    }

    override fun inThen(distancePhrase: String, instruction: String): String = "In $distancePhrase, $instruction"

    override fun passLights(count: Int): String = if (count <= 1) "Pass the traffic light" else "Pass $count traffic lights"

    // Trailing SEMICOLON on purpose (spoken-only string — NavEngine Speak, never displayed): bare text
    // gives the Piper voice no final prosody contour and the callout ended oddly; the user A/B'd
    // punctuation and the semicolon's contour sounds best (period OK, semicolon better). 2026-07-06.
    override fun arrived(): String = "You have arrived;"

    override fun destinationSide(left: Boolean): String = if (left) "Your destination is on the left" else "Your destination is on the right"
    override fun destinationWillBe(left: Boolean?): String = left?.let { if (it) "Your destination will be on the left" else "Your destination will be on the right" } ?: destinationAhead()

    override fun startNav(firstInstruction: String): String = "Starting navigation. $firstInstruction"

    override fun reachedStop(label: String): String =
        if (label.isNotBlank()) "You've reached $label" else "You've reached your stop"

    override fun stopAhead(label: String, left: Boolean?): String {
        val who = label.ifBlank { "Your stop" }
        return when (left) { true -> "$who will be on your left"; false -> "$who will be on your right"; null -> "$who will be ahead" }
    }

    override fun thenStop(instruction: String, label: String, left: Boolean?): String =
        "$instruction, then " + stopAhead(label.ifBlank { "your stop" }, left)

    override fun intoLot(turnLeft: Boolean): String = "Turn ${if (turnLeft) "left" else "right"} into the parking lot"

    override fun thenNext(instruction: String, next: String): String = "$instruction, then " + next.replaceFirstChar { it.lowercase() }

    override fun intoLotThen(turnLeft: Boolean, label: String, left: Boolean?): String {
        val where = when (left) { true -> "is on your left"; false -> "is on your right"; null -> "is ahead" }
        return "Turn ${if (turnLeft) "left" else "right"} into the parking lot, then ${label.ifBlank { "your stop" }} $where"
    }

    override fun fasterRoute(firstInstruction: String): String = "Taking the faster route. $firstInstruction"

    override fun voiceTest(): String = "Voice guidance is on. In a quarter mile, turn right."

    override fun useLanes(side: LaneSide, count: Int): String {
        val sideWord = when (side) { LaneSide.LEFT -> "left"; LaneSide.RIGHT -> "right"; LaneSide.CENTER -> "center" }
        return if (count > 1) "Use the $sideWord $count lanes" else "Use the $sideWord lane"
    }

    // Drop the secondary sign destinations: the colon only ever comes from osrmPhrase's own
    // "ref: cities" sign formatting, and only after a " toward " - so a plain colon elsewhere
    // (times, addresses) can never be touched.
    override fun spokenSign(instruction: String): String {
        val t = instruction.indexOf(" toward ")
        if (t < 0) return instruction
        val colon = instruction.indexOf(':', startIndex = t)
        return if (colon > 0) instruction.substring(0, colon) else instruction
    }

    // "Take the ramp on the right toward CA-99 North, Downtown" -> "Take the ramp on the right".
    // The exit number stays ("Take exit 172"); only the sign-destination tail drops. The " toward "
    // marker is exactly what osrmPhrase/ghPhrase emit for EN ramp/exit/fork steps.
    override fun repeatShort(instruction: String): String = instruction.substringBefore(" toward ")

    override fun repeatWithRoute(instruction: String): String {
        val head = repeatShort(instruction)
        val route = signParts(instruction).firstOrNull { app.vela.core.nav.routeKey(it) != null } ?: return head
        return "$head toward $route"
    }

    override fun signParts(instruction: String): List<String> {
        val t = instruction.indexOf(" toward ")
        if (t < 0) return emptyList()
        return spokenSign(instruction).substring(t + " toward ".length).split(", ").map { it.trim() }.filter { it.isNotEmpty() }
    }

    override fun stayOn(instruction: String, route: String): String? = "${repeatShort(instruction)} to stay on $route"

    override fun useLanesToDo(side: LaneSide, count: Int, instruction: String): String {
        val sideWord = when (side) { LaneSide.LEFT -> "left"; LaneSide.RIGHT -> "right"; LaneSide.CENTER -> "center" }
        val lanes = if (count > 1) "the $sideWord $count lanes" else "the $sideWord lane"
        // "Use the right 2 lanes to take exit 172 toward Sacramento" — lowercase the maneuver's first
        // word so it reads as one sentence after the "In <distance>, " frame. A ramp on the side
        // the lanes already name loses its side: "use the right lane to take the ramp toward".
        val said = instruction.replaceFirst("the ramp on the $sideWord", "the ramp")
        return "Use $lanes to " + said.replaceFirstChar { it.lowercaseChar() }
    }

    /** "1st", "2nd", "3rd", "4th"... (11th-13th are "th"). TTS engines read these as the ordinal words. */
    private fun enOrdinal(n: Int): String {
        val suffix = when {
            n % 100 in 11..13 -> "th"
            n % 10 == 1 -> "st"
            n % 10 == 2 -> "nd"
            n % 10 == 3 -> "rd"
            else -> "th"
        }
        return "$n$suffix"
    }

    /** Whole-word road abbreviation → spoken form, "I-80"→"Interstate 80", and 3-digit street ordinals
     *  ("128th"→"one twenty eighth"). Moved here from VoiceGuide.forSpeech so it's English-scoped. */
    override fun expandForSpeech(text: String): String {
        var s = text
        s = Regex("\\bI-(\\d+)").replace(s) { "Interstate ${it.groupValues[1]}" }
        s = Regex("\\bUS-(\\d+)").replace(s) { "US ${it.groupValues[1]}" }
        // State/province highway refs (CA-99, SR-99, NY-17, …) → "State Route N". Reading the bare 2-letter
        // code (e.g. "CA") makes espeak's G2P mangle the K/C onset — a cause of the "the K/T sounds off
        // sometimes" bug. Runs AFTER I-/US- so those keep their spoken forms (US- is already de-hyphenated).
        s = Regex("\\b[A-Z]{2}-(\\d+)").replace(s) { "State Route ${it.groupValues[1]}" }
        // Break the maneuver clause off the sign destination with a comma before " toward ":
        // espeak's G2P is context-sensitive, and on a full "take the ramp toward Woodland" it
        // mis-voweled "take" to "tyke" — chopping "toward Woodland" made it correct (user
        // 2026-07-11). The comma is a fragment boundary (see SpeechText.speechFragments), so the
        // model phonemizes "take the ramp" in isolation, where it's right, and reads the
        // destination as its own beat (Google pauses there too). "toward" only appears on ramp/
        // exit/highway-sign steps, so plain turns ("... onto Main St") are untouched.
        s = Regex(" toward ", RegexOption.IGNORE_CASE).replace(s, ", toward ")
        // "Take exit 186" mis-vowels the same way ("tacake") now that the short repeat forms can
        // end right after the number - "take the ..." is the onset espeak reliably gets right
        // (user ear-report 2026-07-16), so speak it as "take the 186 exit". Display keeps
        // "Take exit 186"; the exit-tab chip is unaffected (it reads the banner text).
        s = Regex("""\btake exit (\w+)""", RegexOption.IGNORE_CASE).replace(s) { "take the ${it.groupValues[1]} exit" }
        EN_SPEECH_WORDS.forEach { (re, rep) -> s = re.replace(s, rep) }
        s = SpeechText.spokenNumbers(s) // "128th" → "one twenty eighth" (space, not hyphen — the compound got a mushy -ty), not a mangled "one hundred and 28th"
        // "5:49 PM" -> "five forty nine PM": the phonemizer reads a colon between numbers as a
        // measurement and said "five foot nine" for an arrival time (user 2026-09-18).
        s = SpeechText.spokenClock(s)
        return s
    }
}

/** Whole-word road-abbreviation → spoken form, applied only by [EnNavStrings.expandForSpeech]. Road-type
 *  suffixes are case-insensitive; the directionals are uppercase (as they appear in road names) and come
 *  LAST so they can't chew into a word an earlier rule expanded. */
private val EN_SPEECH_WORDS: List<Pair<Regex, String>> = listOf(
    Regex("\\bSt\\b", RegexOption.IGNORE_CASE) to "Street",
    Regex("\\bAve\\b", RegexOption.IGNORE_CASE) to "Avenue",
    Regex("\\bBlvd\\b", RegexOption.IGNORE_CASE) to "Boulevard",
    Regex("\\bRd\\b", RegexOption.IGNORE_CASE) to "Road",
    Regex("\\bDr\\b", RegexOption.IGNORE_CASE) to "Drive",
    Regex("\\bLn\\b", RegexOption.IGNORE_CASE) to "Lane",
    Regex("\\bCt\\b", RegexOption.IGNORE_CASE) to "Court",
    Regex("\\bPkwy\\b", RegexOption.IGNORE_CASE) to "Parkway",
    Regex("\\bHwy\\b", RegexOption.IGNORE_CASE) to "Highway",
    Regex("\\bPl\\b", RegexOption.IGNORE_CASE) to "Place",
    Regex("\\bTer\\b", RegexOption.IGNORE_CASE) to "Terrace",
    Regex("\\bCir\\b", RegexOption.IGNORE_CASE) to "Circle",
    Regex("\\bSq\\b", RegexOption.IGNORE_CASE) to "Square",
    Regex("\\bTrl\\b", RegexOption.IGNORE_CASE) to "Trail",
    Regex("\\bExpy\\b", RegexOption.IGNORE_CASE) to "Expressway",
    Regex("\\bFwy\\b", RegexOption.IGNORE_CASE) to "Freeway",
    // Google's exit signs ("toward Bush Intercont'l Arpt").
    Regex("\\bArpt\\b", RegexOption.IGNORE_CASE) to "Airport",
    Regex("\\bInt'?l\\b", RegexOption.IGNORE_CASE) to "International",
    Regex("\\bIntercont'?l\\b", RegexOption.IGNORE_CASE) to "Intercontinental",
    Regex("\\bNE\\b") to "Northeast",
    Regex("\\bNW\\b") to "Northwest",
    Regex("\\bSE\\b") to "Southeast",
    Regex("\\bSW\\b") to "Southwest",
    Regex("\\bN\\b") to "North",
    Regex("\\bS\\b") to "South",
    Regex("\\bE\\b") to "East",
    Regex("\\bW\\b") to "West",
)

/** Persian navigation prompts. Road and destination labels remain source data. */
object FaNavStrings : NavStrings by EnNavStrings {
    override val locale: Locale = Locale.forLanguageTag("fa-IR")
    private fun digits(value: String): String = value
        .replace('0', '۰').replace('1', '۱').replace('2', '۲').replace('3', '۳')
        .replace('4', '۴').replace('5', '۵').replace('6', '۶').replace('7', '۷')
        .replace('8', '۸').replace('9', '۹')
    private fun side(mod: String?): String = when (mod?.lowercase()) {
        "left", "slight left", "sharp left" -> "چپ"
        "right", "slight right", "sharp right" -> "راست"
        "straight" -> "مستقیم"
        else -> ""
    }
    private fun exitOrdinal(number: Int): String = when (number) {
        1 -> "اولین خروجی"
        2 -> "دومین خروجی"
        3 -> "سومین خروجی"
        else -> "خروجی شماره " + digits(number.toString())
    }
    override fun ferry(): String = "با کشتی عبور کنید"
    override fun junctionExit(ordinal: Int, direction: String, road: String?, fallbackInstruction: String): String {
        val exit = exitOrdinal(ordinal)
        val instruction = when (direction) {
            "right" -> "در تقاطع بعدی، از $exit به سمت راست بروید"
            "left" -> "در تقاطع بعدی، از $exit به سمت چپ بروید"
            else -> "در تقاطع بعدی، از $exit مستقیم عبور کنید"
        }
        val roadText = road?.takeIf { it.isNotBlank() }?.let { " و وارد مسیر «$it» شوید" }.orEmpty()
        return instruction + roadText
    }
    override fun phrase(type: String, mod: String?, road: String?, dest: String?, exitNo: String?, rbExit: Int?): String {
        val direction = side(mod)
        val roadText = road?.takeIf { it.isNotBlank() }?.let { " و وارد مسیر «" + it + "» شوید" }.orEmpty()
        val destination = dest?.takeIf { it.isNotBlank() }?.let { " به سمت «" + it + "»" }.orEmpty()
        val exit = exitNo?.let { " شماره " + digits(it) }.orEmpty()
        return when (type) {
            "depart" -> road?.takeIf { it.isNotBlank() }?.let { "از مسیر «" + it + "» حرکت کنید" } ?: "مسیر را آغاز کنید"
            "arrive" -> "به مقصد رسیدید"
            "turn", "end of road" -> if (direction.isNotBlank()) "به سمت " + direction + " بپیچید" + roadText else "به مسیر ادامه دهید" + roadText
            "continue", "new name" -> road?.takeIf { it.isNotBlank() }?.let { "در مسیر «" + it + "» به حرکت ادامه دهید" } ?: "به مسیر ادامه دهید"
            "merge" -> road?.takeIf { it.isNotBlank() }?.let { "به مسیر «" + it + "» بپیوندید" } ?: "به مسیر اصلی بپیوندید"
            "on ramp" -> "وارد رمپ" + exit + " شوید" + destination
            "off ramp" -> "از خروجی" + exit + destination + " خارج شوید"
            "fork" -> when {
                direction.isNotBlank() && dest.isNullOrBlank() -> "در دوراهی، مسیر سمت " + direction + " را انتخاب کنید"
                direction.isNotBlank() -> "در دوراهی، مسیر سمت " + direction + " را به‌سوی «" + dest + "» انتخاب کنید"
                dest.isNullOrBlank() -> "در دوراهی مسیر را ادامه دهید"
                else -> "در دوراهی مسیر به‌سوی «" + dest + "» را انتخاب کنید"
            }
            "roundabout", "rotary" -> "وارد میدان شوید" + (rbExit?.let { " و از " + exitOrdinal(it) + " خارج شوید" }.orEmpty())
            "roundabout turn" -> "از میدان خارج شوید" + roadText
            "notification" -> road?.takeIf { it.isNotBlank() }?.let { "در مسیر «" + it + "» به حرکت ادامه دهید" } ?: "به مسیر ادامه دهید"
            "exit roundabout", "exit rotary" -> "از میدان خارج شوید" + roadText
            "uturn" -> "دور بزنید" + roadText
            else -> if (direction.isNotBlank()) "به سمت " + direction + " بروید" + roadText else "به مسیر ادامه دهید" + roadText
        }
    }
    override fun spokenDistance(meters: Double, imperial: Boolean): String {
        val amount: String
        val unit: String
        if (imperial) {
            val feet = meters * 3.28084
            if (feet >= 5280.0) {
                amount = digits(String.format(Locale.US, "%.1f", feet / 5280.0).trimEnd('0').trimEnd('.'))
                unit = "مایل"
            } else {
                amount = digits(feet.roundToInt().toString())
                unit = "فوت"
            }
        } else if (meters >= 1000.0) {
            amount = digits(String.format(Locale.US, "%.1f", meters / 1000.0).trimEnd('0').trimEnd('.'))
            unit = "کیلومتر"
        } else {
            amount = digits(meters.roundToInt().toString())
            unit = "متر"
        }
        return amount + " " + unit
    }
    override fun inThen(distancePhrase: String, instruction: String): String = distancePhrase + " دیگر، " + instruction
    override fun arrived(): String = "به مقصد رسیدید"
    override fun destinationSide(left: Boolean): String = if (left) "مقصد در سمت چپ شماست" else "مقصد در سمت راست شماست"
    override fun destinationWillBe(left: Boolean?): String = when (left) {
        true -> "مقصد در سمت چپ شما خواهد بود"
        false -> "مقصد در سمت راست شما خواهد بود"
        null -> "مقصد در پیش روی شماست"
    }
    override fun startNav(firstInstruction: String): String = "مسیریابی آغاز شد. " + firstInstruction
    override fun reachedStop(label: String): String = if (label.isBlank()) "به ایستگاه رسیدید" else "به " + label + " رسیدید"
    override fun stopAhead(label: String, left: Boolean?): String {
        val name = label.ifBlank { "ایستگاه شما" }
        return when (left) {
            true -> name + " در سمت چپ شما خواهد بود"
            false -> name + " در سمت راست شما خواهد بود"
            null -> name + " در پیش روی شما خواهد بود"
        }
    }
    override fun thenStop(instruction: String, label: String, left: Boolean?): String = instruction + "، سپس " + stopAhead(label, left)
    override fun intoLot(turnLeft: Boolean): String = if (turnLeft) "به سمت چپ وارد پارکینگ شوید" else "به سمت راست وارد پارکینگ شوید"
    override fun intoLotThen(turnLeft: Boolean, label: String, left: Boolean?): String = intoLot(turnLeft) + "، سپس " + stopAhead(label, left)
    override fun fasterRoute(firstInstruction: String): String = "مسیر سریع‌تر انتخاب شد. " + firstInstruction
    override fun fasterRouteAvailable(minutes: Int): String = "مسیر سریع‌تری با حدود " + digits(minutes.toString()) + " دقیقه صرفه‌جویی در دسترس است"
    override fun rerouting(): String = "در حال محاسبه دوباره مسیر"
    override fun stopsNotIncluded(): String = "امکان افزودن توقف‌ها به این مسیر نبود. دوباره تلاش می‌کنم."
    override fun leftYourRoute(): String = "از مسیر خارج شدید. مسیر سریع‌تر را دنبال می‌کنیم."
    override fun destinationAhead(): String = "مقصد در پیش روی شماست"
    override fun voiceTest(): String = "راهنمای صوتی فعال است. پس از چهارصد متر، به راست بپیچید."
    override fun useLanes(side: LaneSide, count: Int): String {
        val direction = when (side) { LaneSide.LEFT -> "چپ"; LaneSide.RIGHT -> "راست"; LaneSide.CENTER -> "میانی" }
        val n = digits(count.toString())
        return if (count > 1) "از " + n + " خط سمت " + direction + " استفاده کنید" else "از خط سمت " + direction + " استفاده کنید"
    }
    override fun passLights(count: Int): String =
        if (count <= 1) "از چراغ راهنمایی عبور کنید"
        else "از ${digits(count.toString())} چراغ راهنمایی عبور کنید"

    override fun lightThen(lead: String, instruction: String): String =
        if (lead.isBlank()) instruction else "$lead، سپس $instruction"

    override fun thenNext(instruction: String, next: String): String = "$instruction، سپس $next"

    override fun spokenSign(instruction: String): String = instruction
    override fun repeatShort(instruction: String): String = instruction
    override fun repeatWithRoute(instruction: String): String = instruction
    override fun signParts(instruction: String): List<String> = emptyList()
    override fun stayOn(instruction: String, route: String): String? = null

    override fun useLanesToDo(side: LaneSide, count: Int, instruction: String): String =
        useLanes(side, count) + ". " + instruction

    override fun expandForSpeech(text: String): String = text

}

object NavStringsRegistry {
    @Volatile
    private var active: NavStrings = EnNavStrings

    fun current(): NavStrings = active

    fun setLocale(locale: Locale) { active = forLanguage(tagOf(locale)) }

    /** Resolve every locale to one of the two languages shipped by this fork. */
    fun tagOf(locale: Locale): String =
        if (locale.language.equals("fa", ignoreCase = true)) "fa" else "en"

    /** Persian navigation strings; English is the fallback and only other supported language. */
    fun forLanguage(language: String): NavStrings =
        if (language.equals("fa", ignoreCase = true)) FaNavStrings else EnNavStrings
}
