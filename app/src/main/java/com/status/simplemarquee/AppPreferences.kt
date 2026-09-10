package com.status.simplemarquee

import android.content.Context
import androidx.core.content.edit

class AppPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("app_appearance", Context.MODE_PRIVATE)

    fun isDarkTheme(): Boolean = preferences.getBoolean(KEY_DARK_THEME, false)

    fun hue(): Float = preferences.getFloat(KEY_HUE, DefaultAppHue)

    fun keepScreenOn(): Boolean = preferences.getBoolean(KEY_KEEP_SCREEN_ON, true)

    fun setDarkTheme(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_DARK_THEME, enabled) }
    }

    fun setHue(hue: Float) {
        preferences.edit { putFloat(KEY_HUE, hue.coerceIn(0f, 360f)) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_KEEP_SCREEN_ON, enabled) }
    }

    private companion object {
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_HUE = "theme_hue_v2"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
    }
}
