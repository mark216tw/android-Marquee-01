package com.status.simplemarquee

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class PresetStore(context: Context) {
    private val preferences = context.getSharedPreferences("marquee_presets", Context.MODE_PRIVATE)

    fun load(): List<MarqueePreset> {
        val source = preferences.getString(KEY_PRESETS, null) ?: return samplePresets()
        return runCatching {
            val array = JSONArray(source)
            buildList {
                for (index in 0 until array.length()) {
                    add(array.getJSONObject(index).toPreset())
                }
            }
        }.getOrElse { samplePresets() }
    }

    fun save(presets: List<MarqueePreset>) {
        val array = JSONArray()
        presets.forEach { array.put(it.toJson()) }
        preferences.edit().putString(KEY_PRESETS, array.toString()).apply()
    }

    private fun MarqueePreset.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("text", text)
        put("fontSize", fontSize.toDouble())
        put("letterSpacing", letterSpacing.toDouble())
        put("fontWeight", fontWeight)
        put("foregroundColor", foregroundColor)
        put("backgroundColor", backgroundColor)
        put("themeMode", themeMode.name)
        put("speed", speed.toDouble())
        put("direction", direction.name)
        put("loopGap", loopGap.toDouble())
        put("orientation", orientation.name)
        put("startPaused", startPaused)
        put("mirrorText", mirrorText)
        put("textEffect", textEffect.name)
    }

    private fun JSONObject.toPreset() = MarqueePreset(
        id = getLong("id"),
        name = optString("name", "未命名"),
        text = optString("text", "跑馬燈"),
        fontSize = optDouble("fontSize", 72.0).toFloat(),
        letterSpacing = optDouble("letterSpacing", 0.0).toFloat(),
        fontWeight = optInt("fontWeight", 700),
        foregroundColor = optString("foregroundColor", "#FFFFFF"),
        backgroundColor = optString("backgroundColor", "#000000"),
        themeMode = enumValueOrDefault(optString("themeMode"), MarqueeThemeMode.SOLID),
        speed = optDouble("speed", 140.0).toFloat(),
        direction = enumValueOrDefault(optString("direction"), MarqueeDirection.LEFT),
        loopGap = optDouble("loopGap", 64.0).toFloat().coerceAtLeast(32f),
        orientation = enumValueOrDefault(optString("orientation"), MarqueeOrientation.AUTO),
        startPaused = getBoolean("startPaused"),
        mirrorText = getBoolean("mirrorText"),
        textEffect = enumValueOrDefault(getString("textEffect"), MarqueeTextEffect.NONE),
    )

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, default: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: default

    private fun samplePresets() = listOf(
        MarqueePreset(
            id = 1L,
            name = "歡迎光臨",
            text = "歡迎光臨，祝您今天有美好的一天！",
            foregroundColor = "#FFD54F",
            backgroundColor = "#050505",
        ),
        MarqueePreset(
            id = 2L,
            name = "加油應援",
            text = "全力以赴！我們一起加油！",
            fontSize = 88f,
            foregroundColor = "#FF3D71",
            backgroundColor = "#160A20",
            speed = 180f,
        ),
        MarqueePreset(
            id = 3L,
            name = "彩虹派對",
            text = "HAPPY PARTY！讓快樂不停前進！",
            fontSize = 80f,
            backgroundColor = "#000000",
            themeMode = MarqueeThemeMode.RAINBOW_ANIMATED,
            speed = 160f,
            orientation = MarqueeOrientation.LANDSCAPE,
        ),
    )

    private companion object {
        const val KEY_PRESETS = "presets"
    }
}
