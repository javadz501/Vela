package app.vela.core.search

import app.vela.core.data.ReviewWords
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersianLocalizationTest {
    @Test fun persianCommandsAreParsed() {
        assertEquals(QueryIntent.Home, QueryIntents.parse("منو ببر خونه", "fa"))
        assertEquals(QueryIntent.Work, QueryIntents.parse("منو ببر به محل کار", "fa"))
        assertEquals(QueryIntent.NavigateTo("ایستگاه قطار"), QueryIntents.parse("مسیریابی به ایستگاه قطار", "fa"))
        assertEquals(QueryIntent.Route("تهران", "اصفهان"), QueryIntents.parse("از تهران تا اصفهان", "fa"))
        assertEquals(QueryIntent.Search("داروخانه"), QueryIntents.parse("نزدیک‌ترین داروخانه", "fa"))
        assertEquals(QueryIntent.Search("کافه"), QueryIntents.parse("کافه نزدیک من", "fa"))
        assertEquals(QueryIntent.Eta, QueryIntents.parse("چقدر تا رسیدن مونده", "fa"))
    }

    @Test fun persianVoiceExamplesAreConsistent() {
        val examples = VoiceCommandExamples.forLanguage("fa")
        assertEquals(8, examples.size)
        assertTrue(examples.all { QueryIntents.parse(it.phrase, "fa") != null })
    }

    @Test fun persianReviewLabelsAreRecognized() {
        assertTrue(ReviewWords.isReviewsTab("نظرات"))
        assertTrue(ReviewWords.isMoreReviewsButton("نمایش همه نظرات"))
    }
}
