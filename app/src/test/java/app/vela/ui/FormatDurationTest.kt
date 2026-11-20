package app.vela.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatDurationTest {
    @Test fun `persian minutes use connected words and Persian digits`() {
        assertEquals("۳ دقیقه", formatPersianDuration(3))
        assertEquals("۵۹ دقیقه", formatPersianDuration(59))
    }

    @Test fun `persian hours and minutes use natural conjunction`() {
        assertEquals("۱ ساعت", formatPersianDuration(60))
        assertEquals("۳ ساعت و ۲۰ دقیقه", formatPersianDuration(200))
    }

    @Test fun `sub-minute duration is readable`() {
        assertEquals("کمتر از ۱ دقیقه", formatPersianDuration(0))
    }
}
