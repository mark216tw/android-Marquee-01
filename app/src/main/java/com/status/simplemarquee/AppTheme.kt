package com.status.simplemarquee

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppPalette(val displayName: String, val hue: Float) {
    SUNSET("暖陽", 36f),
    LIME("青檸", 88f),
    AURORA("極光", 150f),
    TROPICAL("海島", 184f),
    ELECTRIC("電光", 232f),
    CANDY("糖果", 326f),
}

const val DefaultAppHue = 36f

private data class PaletteColors(
    val darkPrimary: Color,
    val lightPrimary: Color,
    val secondary: Color,
    val darkBackground: Color,
    val lightBackground: Color,
    val darkSurface: Color,
    val lightSurface: Color,
)

private fun colorsForHue(hue: Float): PaletteColors {
    val darkBackground = hsvToRgb(hue, 0.24f, 0.08f)
    val darkSurface = hsvToRgb(hue, 0.20f, 0.12f)
    val lightBackground = hsvToRgb(hue, 0.08f, 0.99f)
    val lightSurface = RgbColor(255, 255, 255)
    return PaletteColors(
        darkPrimary = themePrimaryRgb(hue = hue, darkTheme = true, surface = darkSurface).toColor(),
        lightPrimary = themePrimaryRgb(hue = hue, darkTheme = false, surface = lightSurface).toColor(),
        secondary = ensureContrast(hsvToRgb(hue + 52f, 0.68f, 0.82f), darkSurface, 3f).toColor(),
        darkBackground = darkBackground.toColor(),
        lightBackground = lightBackground.toColor(),
        darkSurface = darkSurface.toColor(),
        lightSurface = lightSurface.toColor(),
    )
}

internal fun themePrimaryRgb(hue: Float, darkTheme: Boolean, surface: RgbColor): RgbColor =
    ensureContrast(
        color = if (darkTheme) hsvToRgb(hue, 0.62f, 0.96f) else hsvToRgb(hue, 0.88f, 0.52f),
        background = surface,
    )

fun palettePreviewColors(hue: Float, darkTheme: Boolean): List<Color> {
    val colors = colorsForHue(hue)
    return listOf(
        if (darkTheme) colors.darkPrimary else colors.lightPrimary,
        colors.secondary,
    )
}

@Composable
fun SimpleMarqueeTheme(
    darkTheme: Boolean,
    hue: Float,
    content: @Composable () -> Unit,
) {
    val colors = colorsForHue(hue)
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.darkPrimary,
            onPrimary = contrastingTextColor(colors.darkPrimary),
            secondary = colors.secondary,
            onSecondary = contrastingTextColor(colors.secondary),
            background = colors.darkBackground,
            onBackground = Color(0xFFF3F5F7),
            surface = colors.darkSurface,
            onSurface = Color(0xFFF3F5F7),
            surfaceVariant = colors.darkSurface.copy(
                red = (colors.darkBackground.red + 0.09f).coerceAtMost(1f),
                green = (colors.darkBackground.green + 0.09f).coerceAtMost(1f),
                blue = (colors.darkBackground.blue + 0.09f).coerceAtMost(1f),
            ),
            onSurfaceVariant = Color(0xFFCBD2D8),
            outline = Color(0xFF66717A),
            error = Color(0xFFFF6B72),
        )
    } else {
        lightColorScheme(
            primary = colors.lightPrimary,
            onPrimary = contrastingTextColor(colors.lightPrimary),
            secondary = colors.secondary,
            onSecondary = contrastingTextColor(colors.secondary),
            background = colors.lightBackground,
            onBackground = Color(0xFF1A1C1E),
            surface = colors.lightSurface,
            onSurface = Color(0xFF1A1C1E),
            surfaceVariant = colors.lightBackground.copy(
                red = (colors.lightBackground.red - 0.06f).coerceAtLeast(0f),
                green = (colors.lightBackground.green - 0.06f).coerceAtLeast(0f),
                blue = (colors.lightBackground.blue - 0.06f).coerceAtLeast(0f),
            ),
            onSurfaceVariant = Color(0xFF46515A),
            outline = Color(0xFF78838C),
            error = Color(0xFFBA1A1A),
        )
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
