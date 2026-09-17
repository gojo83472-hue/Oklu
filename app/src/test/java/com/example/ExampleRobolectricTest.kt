package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.action.ActionParser
import com.example.action.AndroidAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Voice Assistant", appName)
    }

    @Test
    fun `test action parser recognizes camera`() {
        val res = ActionParser.parse("open camera")
        assertTrue(res.action is AndroidAction.OpenCamera)
    }

    @Test
    fun `test action parser recognizes phone call`() {
        val res = ActionParser.parse("call 555-0199")
        assertTrue(res.action is AndroidAction.MakeCall)
        val call = res.action as AndroidAction.MakeCall
        assertEquals("555-0199", call.phoneNumber)
    }

    @Test
    fun `test action parser recognizes gallery`() {
        val res = ActionParser.parse("show my photos")
        assertTrue(res.action is AndroidAction.OpenGallery)
    }

    @Test
    fun `test action parser recognizes youtube`() {
        val res = ActionParser.parse("search youtube for coding tutorials")
        assertTrue(res.action is AndroidAction.OpenYouTube)
        assertEquals("coding tutorials", (res.action as AndroidAction.OpenYouTube).query)
    }

    @Test
    fun `test action parser recognizes flashlight`() {
        val res = ActionParser.parse("turn on flashlight")
        assertTrue(res.action is AndroidAction.ToggleFlashlight)
        assertTrue((res.action as AndroidAction.ToggleFlashlight).enable)
    }

    @Test
    fun `test action parser recognizes dictation`() {
        val res = ActionParser.parse("type exactly what I say: The meeting starts at noon")
        assertTrue(res.action is AndroidAction.Dictation)
        assertEquals("The meeting starts at noon", (res.action as AndroidAction.Dictation).transcribedText)
    }

    @Test
    fun `test language detection for Tamil`() {
        val lang = ActionParser.detectLanguage("வணக்கம் எப்படி இருக்கீங்க")
        assertEquals(ActionParser.LanguageType.TAMIL, lang)
    }

    @Test
    fun `test language detection for Thanglish`() {
        val lang = ActionParser.detectLanguage("Vanakkam boss, epdi irukinga?")
        assertEquals(ActionParser.LanguageType.THANGLISH, lang)
    }

    @Test
    fun `test pet mode and persona preferences`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = com.example.data.local.PreferencesManager(context)

        // Default pet mode is emoji
        assertEquals("emoji", prefs.petMode)
        assertEquals("robot", prefs.petEmojiPersona)

        // Change pet settings
        prefs.petMode = "droid"
        prefs.petEmojiPersona = "cat"
        assertEquals("droid", prefs.petMode)
        assertEquals("cat", prefs.petEmojiPersona)

        // Reset to emoji
        prefs.petMode = "emoji"
        assertEquals("emoji", prefs.petMode)
    }

    @Test
    fun `test logo drawable loads successfully`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_ai_human_logo)
        assertTrue("Logo drawable should exist and load", drawable != null)
    }
}

