package app.vela.core.i18n

/** Format a duration in Persian without ICU's platform-dependent short-unit glyph shaping. */
fun formatPersianDuration(totalMinutes: Int): String {
    fun digits(value: Int): String = value.toString().map { ch ->
        if (ch in '0'..'9') ('۰'.code + (ch.code - '0'.code)).toChar() else ch
    }.joinToString("")

    if (totalMinutes < 1) return "کمتر از ۱ دقیقه"
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0 -> "${digits(minutes)} دقیقه"
        minutes == 0 -> "${digits(hours)} ساعت"
        else -> "${digits(hours)} ساعت و ${digits(minutes)} دقیقه"
    }
}
