package app.vela.core.i18n

import app.vela.core.model.LaneSide
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class NavStringsTest {

    @After
    fun resetToEnglish() = NavStringsRegistry.setLocale(Locale.ENGLISH)

    @Test
    fun `english navigation templates remain unchanged`() {
        assertEquals(
            "Turn right onto the local street",
            EnNavStrings.phrase("turn", "right", "the local street", null, null, null),
        )
        assertEquals(
            "Take exit 15 toward Sacramento",
            EnNavStrings.phrase("off ramp", null, null, "Sacramento", "15", null),
        )
        assertEquals(
            "At the roundabout, take the 2nd exit onto Elm St",
            EnNavStrings.phrase("roundabout", null, "Elm St", null, null, 2),
        )
        assertEquals("Make a U-turn onto Main St", EnNavStrings.phrase("uturn", null, "Main St", null, null, null))
        assertEquals("You have arrived;", EnNavStrings.arrived())
    }

    @Test
    fun `persian navigation localizes maneuver types and keeps road names intact`() {
        assertEquals("به سمت راست بپیچید", FaNavStrings.phrase("turn", "right", null, null, null, null))
        assertEquals(
            "به سمت راست بپیچید و وارد مسیر «امام خمینی» شوید",
            FaNavStrings.phrase("turn", "right", "امام خمینی", null, null, null),
        )
        assertEquals(
            "در مسیر «خیابان آزادی» به حرکت ادامه دهید",
            FaNavStrings.phrase("continue", "straight", "خیابان آزادی", null, null, null),
        )
        assertEquals(
            "از خروجی شماره ۲ به سمت «مرکز شهر» خارج شوید",
            FaNavStrings.phrase("off ramp", null, null, "مرکز شهر", "2", null),
        )
        assertEquals("دور بزنید", FaNavStrings.phrase("uturn", null, null, null, null, null))
        assertEquals(
            "از میدان خارج شوید و وارد مسیر «خیابان آزادی» شوید",
            FaNavStrings.phrase("exit roundabout", null, "خیابان آزادی", null, null, null),
        )
        assertEquals(
            "وارد میدان شوید و از دومین خروجی خارج شوید",
            FaNavStrings.phrase("roundabout", null, null, null, null, 2),
        )
        assertEquals(
            "در دوراهی، مسیر سمت راست را به‌سوی «مرکز شهر» انتخاب کنید",
            FaNavStrings.phrase("fork", "right", null, "مرکز شهر", null, null),
        )
    }

    @Test
    fun `persian spoken distances and lane guidance use Persian digits`() {
        assertEquals("۴۰۰ متر", FaNavStrings.spokenDistance(400.0, imperial = false))
        assertEquals("۱.۵ کیلومتر", FaNavStrings.spokenDistance(1500.0, imperial = false))
        assertEquals("از ۲ خط سمت راست استفاده کنید", FaNavStrings.useLanes(LaneSide.RIGHT, 2))
        assertEquals("از خط سمت چپ استفاده کنید", FaNavStrings.useLanes(LaneSide.LEFT, 1))
        assertEquals(
            "از ۲ خط سمت راست استفاده کنید. از خروجی شماره ۲ خارج شوید",
            FaNavStrings.useLanesToDo(
                LaneSide.RIGHT,
                2,
                FaNavStrings.phrase("off ramp", null, null, null, "2", null),
            ),
        )
    }

    @Test
    fun `persian arrival stop and rerouting prompts are localized`() {
        assertEquals("به مقصد رسیدید", FaNavStrings.arrived())
        assertEquals("مقصد در سمت چپ شماست", FaNavStrings.destinationSide(left = true))
        assertEquals("مقصد در سمت راست شما خواهد بود", FaNavStrings.destinationWillBe(left = false))
        assertEquals("مقصد در پیش روی شماست", FaNavStrings.destinationAhead())
        assertEquals("مسیریابی آغاز شد. به مسیر ادامه دهید", FaNavStrings.startNav("به مسیر ادامه دهید"))
        assertEquals("به Costco رسیدید", FaNavStrings.reachedStop("Costco"))
        assertEquals("به ایستگاه رسیدید", FaNavStrings.reachedStop(""))
        assertEquals("ایستگاه شما در سمت راست شما خواهد بود", FaNavStrings.stopAhead("", left = false))
        assertEquals("در حال محاسبه دوباره مسیر", FaNavStrings.rerouting())
        assertEquals(
            "مسیر سریع‌تری با حدود ۳ دقیقه صرفه‌جویی در دسترس است",
            FaNavStrings.fasterRouteAvailable(3),
        )
        assertEquals("راهنمای صوتی فعال است. پس از چهارصد متر، به راست بپیچید.", FaNavStrings.voiceTest())
        assertEquals("با کشتی عبور کنید", FaNavStrings.ferry())
    }

    @Test
    fun `persian generated prompts do not fall back to english templates`() {
        val prompts = listOf(
            FaNavStrings.inThen("۴۰۰ متر", "به راست بپیچید"),
            FaNavStrings.startNav("به راست بپیچید"),
            FaNavStrings.fasterRoute("به راست بپیچید"),
            FaNavStrings.rerouting(),
            FaNavStrings.stopsNotIncluded(),
            FaNavStrings.leftYourRoute(),
            FaNavStrings.destinationAhead(),
            FaNavStrings.useLanes(LaneSide.RIGHT, 2),
        )
        val englishFragments = listOf("Turn", "Continue", "Starting navigation", "Taking the faster route", "Rerouting", "Your destination")
        for (prompt in prompts) {
            assertTrue(prompt.isNotBlank())
            for (fragment in englishFragments) assertFalse("Unexpected '$fragment' in: $prompt", prompt.contains(fragment))
        }
        assertEquals("Main St", FaNavStrings.expandForSpeech("Main St"))
    }

    @Test
    fun `registry exposes only english and persian with english fallback`() {
        assertEquals(EnNavStrings, NavStringsRegistry.forLanguage("en"))
        assertEquals(FaNavStrings, NavStringsRegistry.forLanguage("fa"))
        assertEquals(EnNavStrings, NavStringsRegistry.forLanguage("fr"))
        assertEquals(EnNavStrings, NavStringsRegistry.forLanguage("ja"))
        assertEquals(EnNavStrings, NavStringsRegistry.forLanguage("zh-TW"))
        assertEquals("fa", NavStringsRegistry.tagOf(Locale.forLanguageTag("fa-IR")))
        assertEquals("en", NavStringsRegistry.tagOf(Locale.FRANCE))

        NavStringsRegistry.setLocale(Locale.forLanguageTag("fa-IR"))
        assertEquals(FaNavStrings, NavStringsRegistry.current())
        NavStringsRegistry.setLocale(Locale.FRANCE)
        assertEquals(EnNavStrings, NavStringsRegistry.current())
    }

    @Test
    fun `only supported language implementations remain in the registry`() {
        assertEquals("en", EnNavStrings.locale.language)
        assertEquals("fa", FaNavStrings.locale.language)
        for (code in listOf("en", "fa", "fr", "de", "es", "it", "pt", "nl", "ru", "pl", "sv", "uk", "zh", "ja", "he", "hu", "ro")) {
            val strings = NavStringsRegistry.forLanguage(code)
            assertTrue(strings === EnNavStrings || strings === FaNavStrings)
        }
    }
}
