package com.status.simplemarquee

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorUtilsTest {
    @Test
    fun hexColorOnlyAcceptsOpaqueRgb() {
        assertTrue(isValidHexColor("#12aBcF"))
        assertFalse(isValidHexColor("#8012ABCF"))
        assertFalse(isValidHexColor("12ABCF"))
        assertEquals("#12ABCF", normalizeHexColor("#12aBcF"))
    }

    @Test
    fun blackAndWhiteHaveMaximumContrast() {
        val ratio = contrastRatio(
            requireNotNull(parseHexRgb("#000000")),
            requireNotNull(parseHexRgb("#FFFFFF")),
        )

        assertEquals(21f, ratio, 0.01f)
    }

    @Test
    fun identicalColorsHaveNoContrast() {
        val color = requireNotNull(parseHexRgb("#3A78C2"))

        assertEquals(1f, contrastRatio(color, color), 0.01f)
    }

    @Test
    fun hsvRoundTripKeepsColorClose() {
        val original = requireNotNull(parseHexRgb("#35B87A"))
        val hsv = rgbToHsv(original)
        val converted = hsvToRgb(hsv.hue, hsv.saturation, hsv.value)

        assertTrue(kotlin.math.abs(original.red - converted.red) <= 1)
        assertTrue(kotlin.math.abs(original.green - converted.green) <= 1)
        assertTrue(kotlin.math.abs(original.blue - converted.blue) <= 1)
    }

    @Test
    fun generatedThemePrimaryMeetsContrastAcrossAllHues() {
        val lightSurface = RgbColor(255, 255, 255)
        for (hue in 0..360) {
            val darkSurface = hsvToRgb(hue.toFloat(), 0.20f, 0.12f)
            assertTrue(contrastRatio(themePrimaryRgb(hue.toFloat(), true, darkSurface), darkSurface) >= 4.5f)
            assertTrue(contrastRatio(themePrimaryRgb(hue.toFloat(), false, lightSurface), lightSurface) >= 4.5f)
        }
    }
}
