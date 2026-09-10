package com.status.simplemarquee

enum class MarqueeThemeMode {
    SOLID,
    RAINBOW,
    RAINBOW_ANIMATED,
}

enum class MarqueeDirection {
    LEFT,
    RIGHT,
}

enum class MarqueeOrientation {
    AUTO,
    LANDSCAPE,
    PORTRAIT,
}

enum class MarqueeTextEffect {
    NONE,
    OUTLINE,
    BREATHING_BRIGHTNESS,
    NEON,
}

data class MarqueePreset(
    val id: Long,
    val name: String,
    val text: String,
    val fontSize: Float = 72f,
    val letterSpacing: Float = 0f,
    val fontWeight: Int = 700,
    val foregroundColor: String = "#FFFFFF",
    val backgroundColor: String = "#000000",
    val themeMode: MarqueeThemeMode = MarqueeThemeMode.SOLID,
    val speed: Float = 140f,
    val direction: MarqueeDirection = MarqueeDirection.LEFT,
    val loopGap: Float = 64f,
    val orientation: MarqueeOrientation = MarqueeOrientation.AUTO,
    val startPaused: Boolean = false,
    val mirrorText: Boolean = false,
    val textEffect: MarqueeTextEffect = MarqueeTextEffect.NONE,
)
