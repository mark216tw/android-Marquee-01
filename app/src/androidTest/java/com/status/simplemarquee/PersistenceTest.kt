package com.status.simplemarquee

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenceTest {
    private lateinit var context: Context

    @Before
    fun clearPreferences() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        clearStoredData()
    }

    @After
    fun cleanUp() {
        clearStoredData()
    }

    @Test
    fun presetEffectsAndPlaybackOptionsRoundTrip() {
        val preset = MarqueePreset(
            id = 42L,
            name = "新版格式",
            text = "鏡像呼吸",
            startPaused = true,
            mirrorText = true,
            textEffect = MarqueeTextEffect.BREATHING_BRIGHTNESS,
        )

        PresetStore(context).save(listOf(preset))

        assertEquals(listOf(preset), PresetStore(context).load())
    }

    @Test
    fun keepScreenOnDefaultsToEnabledAndPersists() {
        assertTrue(AppPreferences(context).keepScreenOn())

        AppPreferences(context).setKeepScreenOn(false)

        assertFalse(AppPreferences(context).keepScreenOn())
    }

    private fun clearStoredData() {
        context.getSharedPreferences("marquee_presets", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("app_appearance", Context.MODE_PRIVATE).edit().clear().commit()
    }
}
