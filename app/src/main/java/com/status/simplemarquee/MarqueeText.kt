package com.status.simplemarquee

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import kotlin.math.abs
import kotlin.math.roundToInt

private val RainbowColors = RainbowHexColors.map { parseHexColor(it) }

@Composable
fun MarqueeText(
    preset: MarqueePreset,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    var containerWidth by remember { mutableIntStateOf(0) }
    var textWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val effectiveGap = preset.loopGap.coerceAtLeast(32f)
    val gapPx = with(density) { effectiveGap.dp.toPx() }
    val velocityPx = with(density) { preset.speed.dp.toPx() }.coerceAtLeast(1f)
    val cycleDistance = textWidth.toFloat() + gapPx
    val direction = effectiveMarqueeDirection(preset.direction, preset.mirrorText)
    val start = when (direction) {
        MarqueeDirection.LEFT -> 0f
        MarqueeDirection.RIGHT -> -cycleDistance
    }
    val end = when (direction) {
        MarqueeDirection.LEFT -> -cycleDistance
        MarqueeDirection.RIGHT -> 0f
    }
    val position = remember(
        preset.text,
        preset.fontSize,
        preset.letterSpacing,
        preset.fontWeight,
        direction,
        preset.loopGap,
        textWidth,
    ) {
        Animatable(start)
    }

    LaunchedEffect(position, isPlaying, preset.speed) {
        if (!isPlaying || containerWidth == 0 || textWidth == 0) return@LaunchedEffect
        while (true) {
            val distance = abs(end - position.value)
            val duration = ((distance / velocityPx) * 1_000f).roundToInt().coerceAtLeast(1)
            position.animateTo(
                targetValue = end,
                animationSpec = tween(durationMillis = duration, easing = LinearEasing),
            )
            position.snapTo(start)
        }
    }

    val brush = marqueeBrush(preset.themeMode, isPlaying)
    val foreground = parseHexColor(preset.foregroundColor)
    val effectColor = if (foreground.luminance() > 0.5f) Color.Black else Color.White
    val baseStyle = if (brush == null) {
        TextStyle(
            color = foreground,
            fontSize = preset.fontSize.sp,
            letterSpacing = preset.letterSpacing.em,
            fontWeight = FontWeight(preset.fontWeight.coerceIn(100, 900)),
        )
    } else {
        TextStyle(
            brush = brush,
            fontSize = preset.fontSize.sp,
            letterSpacing = preset.letterSpacing.em,
            fontWeight = FontWeight(preset.fontWeight.coerceIn(100, 900)),
        )
    }
    val textStyle = when (preset.textEffect) {
        MarqueeTextEffect.NEON -> baseStyle.copy(
            shadow = Shadow(
                color = if (brush == null) foreground else Color.White,
                offset = Offset.Zero,
                blurRadius = with(density) { 12.dp.toPx() },
            ),
        )
        else -> baseStyle
    }
    val brightness = remember(preset.textEffect) { Animatable(1f) }
    LaunchedEffect(brightness, isPlaying) {
        if (preset.textEffect != MarqueeTextEffect.BREATHING_BRIGHTNESS || !isPlaying) {
            return@LaunchedEffect
        }
        while (true) {
            brightness.animateTo(
                targetValue = 0.45f,
                animationSpec = tween(durationMillis = 1_200, easing = FastOutSlowInEasing),
            )
            brightness.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1_200, easing = FastOutSlowInEasing),
            )
        }
    }
    val outlineStyle = TextStyle(
        color = effectColor,
        fontSize = preset.fontSize.sp,
        letterSpacing = preset.letterSpacing.em,
        fontWeight = FontWeight(preset.fontWeight.coerceIn(100, 900)),
        drawStyle = Stroke(
            width = (preset.fontSize * density.density * density.fontScale * 0.06f).coerceIn(2f, 10f),
        ),
    )
    val repeatCount = if (textWidth > 0) {
        (containerWidth / (textWidth + gapPx).coerceAtLeast(1f)).toInt() + 3
    } else {
        2
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { containerWidth = it.width },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier
                .offset { IntOffset(position.value.roundToInt(), 0) }
                .wrapContentWidth(unbounded = true),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(repeatCount) { index ->
                MarqueeGlyph(
                    preset = preset,
                    style = textStyle,
                    outlineStyle = outlineStyle,
                    brightness = brightness.value,
                    modifier = if (index == 0) {
                        Modifier.onSizeChanged { textWidth = it.width }
                    } else {
                        Modifier
                    },
                )
                Spacer(Modifier.width(effectiveGap.dp))
            }
        }
    }
}

@Composable
private fun MarqueeGlyph(
    preset: MarqueePreset,
    style: TextStyle,
    outlineStyle: TextStyle,
    brightness: Float,
    modifier: Modifier = Modifier,
) {
    val mirroredModifier = modifier.graphicsLayer {
        scaleX = if (preset.mirrorText) -1f else 1f
        alpha = brightness
    }
    Box(modifier = mirroredModifier, contentAlignment = Alignment.Center) {
        if (preset.textEffect == MarqueeTextEffect.OUTLINE) {
            Text(
                text = preset.text.replace('\n', ' '),
                modifier = Modifier.clearAndSetSemantics {},
                maxLines = 1,
                softWrap = false,
                style = outlineStyle,
            )
        }
        Text(
            text = preset.text.replace('\n', ' '),
            maxLines = 1,
            softWrap = false,
            style = style,
        )
    }
}

internal fun effectiveMarqueeDirection(
    direction: MarqueeDirection,
    mirrorText: Boolean,
): MarqueeDirection = if (!mirrorText) {
    direction
} else {
    when (direction) {
        MarqueeDirection.LEFT -> MarqueeDirection.RIGHT
        MarqueeDirection.RIGHT -> MarqueeDirection.LEFT
    }
}

@Composable
private fun marqueeBrush(mode: MarqueeThemeMode, isPlaying: Boolean): Brush? {
    if (mode == MarqueeThemeMode.SOLID) return null
    if (mode == MarqueeThemeMode.RAINBOW) {
        return remember { Brush.horizontalGradient(RainbowColors) }
    }

    val shift = remember { Animatable(0f) }
    LaunchedEffect(shift, isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (true) {
            val remainingFraction = (1_200f - shift.value) / 1_200f
            shift.animateTo(
                targetValue = 1_200f,
                animationSpec = tween(
                    durationMillis = (2_800 * remainingFraction).roundToInt().coerceAtLeast(1),
                    easing = LinearEasing,
                ),
            )
            shift.snapTo(0f)
        }
    }
    return Brush.linearGradient(
        colors = RainbowColors,
        start = Offset(shift.value - 1_200f, 0f),
        end = Offset(shift.value, 0f),
        tileMode = TileMode.Repeated,
    )
}
