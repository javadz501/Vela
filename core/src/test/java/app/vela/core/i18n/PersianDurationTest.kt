package app.vela.core.i18n

import org.junit.Assert.assertEquals
import org.junit.Test

class PersianDurationTest {
    @Test fun minutesUseConnectedWordsAndPersianDigits() {
        assertEquals("۳ دقیقه", formatPersianDuration(3))
        assertEquals("۵۹ دقیقه", formatPersianDuration(59))
    }

    @Test fun hoursAndMinutesUseNaturalConjunction() {
        assertEquals("۱ ساعت", formatPersianDuration(60))
        assertEquals("۳ ساعت و ۲۰ دقیقه", formatPersianDuration(200))
    }

    @Test fun subMinuteDurationIsReadable() {
        assertEquals("کمتر از ۱ دقیقه", formatPersianDuration(0))
    }
}
