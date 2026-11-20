package app.vela.core.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PiperCatalogTest {

    @Test
    fun `ids are unique`() {
        val ids = PiperCatalog.ALL.map { it.id }
        assertEquals("no duplicate voice ids", ids.size, ids.toSet().size)
    }

    @Test
    fun `byId round-trips every English entry`() {
        for (voice in PiperCatalog.ALL) assertEquals(voice, PiperCatalog.byId(voice.id))
        assertEquals(null, PiperCatalog.byId("en_US-does-not-exist"))
    }

    @Test
    fun `download urls match the sherpa asset scheme`() {
        assertEquals(
            "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-en_US-lessac-medium.tar.bz2",
            PiperCatalog.downloadUrl("en_US-lessac-medium"),
        )
        for (voice in PiperCatalog.ALL) {
            val url = PiperCatalog.downloadUrl(voice.id)
            assertTrue(url.startsWith("https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-"))
            assertTrue(url.endsWith("${voice.id}.tar.bz2"))
        }
    }

    @Test
    fun `only English Piper models remain and Persian uses system TTS`() {
        assertEquals(setOf("en"), PiperCatalog.ALL.map { it.langCode }.toSet())
        assertEquals(listOf("en"), PiperCatalog.languageCodes())
        assertTrue(PiperCatalog.hasVoiceFor("en"))
        assertFalse(PiperCatalog.hasVoiceFor("fa"))
        assertEquals("English", PiperCatalog.languageLabel("en"))
        assertEquals("فارسی", PiperCatalog.languageLabel("fa"))
    }

    @Test
    fun `English has one recommended default and Persian falls back safely`() {
        val english = PiperCatalog.defaultFor("en")
        assertEquals("en", english.langCode)
        assertTrue(english.recommended)
        assertEquals(PiperCatalog.byId(VelaPiper.DEFAULT_VOICE_ID), PiperCatalog.defaultFor("fa"))
    }

    @Test
    fun `langCode and region derive from the voice id`() {
        val us = PiperCatalog.byId("en_US-hfc_female-medium")!!
        assertEquals("en", us.langCode)
        assertEquals("US", us.region)
        val gb = PiperCatalog.byId("en_GB-alba-medium")!!
        assertEquals("en", gb.langCode)
        assertEquals("GB", gb.region)
    }

    @Test
    fun `the default voice is in the catalog and recommended`() {
        val voice = PiperCatalog.byId(VelaPiper.DEFAULT_VOICE_ID)
        assertNotNull("the default voice must be a catalog entry", voice)
        assertTrue(voice!!.recommended)
    }

    @Test
    fun `the speaker seed voice is the multi speaker pack`() {
        val seed = PiperCatalog.byId(VelaPiper.LEGACY_ID)
        assertNotNull(seed)
        assertTrue(seed!!.multiSpeaker)
        assertEquals(904, seed.numSpeakers)
    }

    @Test
    fun `sizes and speaker counts are sane`() {
        for (voice in PiperCatalog.ALL) {
            assertTrue("${voice.id} size", voice.sizeMb in 20..200)
            assertTrue("${voice.id} speakers", voice.numSpeakers >= 1)
            assertEquals("${voice.id} gender/speaker agreement", voice.gender == VoiceGender.MULTI, voice.numSpeakers > 1)
        }
    }
}
