package com.status.simplemarquee

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

internal data class RgbColor(val red: Int, val green: Int, val blue: Int)

internal data class HsvColor(val hue: Float, val saturation: Float, val value: Float)

internal val RainbowHexColors = listOf(
    "#FF3B5C", "#FFA63B", "#FFE45C", "#52E37D",
    "#49C6FF", "#8C7BFF", "#FF61D2", "#FF3B5C",
)

fun isValidHexColor(value: String): Boolean =
    Regex("^#[0-9a-fA-F]{6}$").matches(value.trim())

fun parseHexColor(value: String, fallback: Color = Color.White): Color =
    parseHexRgb(value)?.toColor() ?: fallback

internal fun normalizeHexColor(value: String): String? = parseHexRgb(value)?.toHex()

internal fun parseHexRgb(value: String): RgbColor? {
    if (!isValidHexColor(value)) return null
    val rgb = value.trim().drop(1).toInt(16)
    return RgbColor(rgb shr 16 and 0xFF, rgb shr 8 and 0xFF, rgb and 0xFF)
}

internal fun hsvToRgb(hue: Float, saturation: Float, value: Float): RgbColor {
    val normalizedHue = ((hue % 360f) + 360f) % 360f
    val safeSaturation = saturation.coerceIn(0f, 1f)
    val safeValue = value.coerceIn(0f, 1f)
    val chroma = safeValue * safeSaturation
    val section = normalizedHue / 60f
    val secondary = chroma * (1f - abs(section % 2f - 1f))
    val (red, green, blue) = when (section.toInt()) {
        0 -> Triple(chroma, secondary, 0f)
        1 -> Triple(secondary, chroma, 0f)
        2 -> Triple(0f, chroma, secondary)
        3 -> Triple(0f, secondary, chroma)
        4 -> Triple(secondary, 0f, chroma)
        else -> Triple(chroma, 0f, secondary)
    }
    val match = safeValue - chroma
    return RgbColor(
        ((red + match) * 255f).roundToInt().coerceIn(0, 255),
        ((green + match) * 255f).roundToInt().coerceIn(0, 255),
        ((blue + match) * 255f).roundToInt().coerceIn(0, 255),
    )
}

internal fun rgbToHsv(color: RgbColor): HsvColor {
    val red = color.red / 255f
    val green = color.green / 255f
    val blue = color.blue / 255f
    val maximum = max(red, max(green, blue))
    val minimum = min(red, min(green, blue))
    val delta = maximum - minimum
    val hue = when {
        delta == 0f -> 0f
        maximum == red -> 60f * (((green - blue) / delta) % 6f)
        maximum == green -> 60f * (((blue - red) / delta) + 2f)
        else -> 60f * (((red - green) / delta) + 4f)
    }
    return HsvColor(
        hue = (hue + 360f) % 360f,
        saturation = if (maximum == 0f) 0f else delta / maximum,
        value = maximum,
    )
}

internal fun contrastRatio(first: RgbColor, second: RgbColor): Float {
    val firstLuminance = relativeLuminance(first)
    val secondLuminance = relativeLuminance(second)
    return (max(firstLuminance, secondLuminance) + 0.05f) /
        (min(firstLuminance, secondLuminance) + 0.05f)
}

internal fun ensureContrast(
    color: RgbColor,
    background: RgbColor,
    minimumRatio: Float = 4.5f,
): RgbColor {
    if (contrastRatio(color, background) >= minimumRatio) return color
    val black = RgbColor(0, 0, 0)
    val white = RgbColor(255, 255, 255)
    val target = if (contrastRatio(black, background) >= contrastRatio(white, background)) black else white
    for (step in 1..100) {
        val amount = step / 100f
        val adjusted = RgbColor(
            (color.red + (target.red - color.red) * amount).roundToInt(),
            (color.green + (target.green - color.green) * amount).roundToInt(),
            (color.blue + (target.blue - color.blue) * amount).roundToInt(),
        )
        if (contrastRatio(adjusted, background) >= minimumRatio) return adjusted
    }
    return target
}

internal fun marqueeContrastRatio(preset: MarqueePreset): Float? {
    val background = parseHexRgb(preset.backgroundColor) ?: return null
    return when (preset.themeMode) {
        MarqueeThemeMode.SOLID -> {
            val foreground = parseHexRgb(preset.foregroundColor) ?: return null
            contrastRatio(foreground, background)
        }
        MarqueeThemeMode.RAINBOW,
        MarqueeThemeMode.RAINBOW_ANIMATED,
        -> RainbowHexColors.minOf { contrastRatio(requireNotNull(parseHexRgb(it)), background) }
    }
}

private fun relativeLuminance(color: RgbColor): Float =
    0.2126f * linearize(color.red) +
        0.7152f * linearize(color.green) +
        0.0722f * linearize(color.blue)

private fun linearize(channel: Int): Float {
    val value = channel / 255f
    return if (value <= 0.04045f) value / 12.92f
    else ((value + 0.055f) / 1.055f).pow(2.4f)
}

internal fun RgbColor.toHex(): String = "#%02X%02X%02X".format(red, green, blue)

internal fun RgbColor.toColor(): Color = Color(red, green, blue)

internal fun contrastingTextColor(background: Color): Color {
    val rgb = RgbColor(
        (background.red * 255f).roundToInt(),
        (background.green * 255f).roundToInt(),
        (background.blue * 255f).roundToInt(),
    )
    val black = RgbColor(0, 0, 0)
    val white = RgbColor(255, 255, 255)
    return if (contrastRatio(black, rgb) >= contrastRatio(white, rgb)) Color.Black else Color.White
}
