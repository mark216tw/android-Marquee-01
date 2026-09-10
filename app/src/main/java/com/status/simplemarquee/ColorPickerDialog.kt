package com.status.simplemarquee

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
internal fun ColorPickerDialog(
    title: String,
    initialColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val original = rememberSaveable { normalizeHexColor(initialColor) ?: "#FFFFFF" }
    val initialHsv = remember { rgbToHsv(requireNotNull(parseHexRgb(original))) }
    var hue by rememberSaveable { mutableFloatStateOf(initialHsv.hue) }
    var saturation by rememberSaveable { mutableFloatStateOf(initialHsv.saturation) }
    var value by rememberSaveable { mutableFloatStateOf(initialHsv.value) }
    var hex by rememberSaveable { mutableStateOf(original) }
    var showAdvanced by rememberSaveable { mutableStateOf(false) }

    fun updateColor(newHue: Float = hue, newSaturation: Float = saturation, newValue: Float = value) {
        hue = newHue.coerceIn(0f, 360f)
        saturation = newSaturation.coerceIn(0f, 1f)
        value = newValue.coerceIn(0f, 1f)
        hex = hsvToRgb(hue, saturation, value).toHex()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SaturationValuePicker(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onChange = { newSaturation, newValue ->
                        updateColor(newSaturation = newSaturation, newValue = newValue)
                    },
                )
                Text("飽和度 ${(saturation * 100f).roundToInt()}%", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = saturation,
                    onValueChange = { updateColor(newSaturation = it) },
                    modifier = Modifier.semantics { contentDescription = "飽和度" },
                )
                Text("亮度 ${(value * 100f).roundToInt()}%", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = value,
                    onValueChange = { updateColor(newValue = it) },
                    modifier = Modifier.semantics { contentDescription = "亮度" },
                )
                Text("色相 ${hue.toInt()}°", style = MaterialTheme.typography.labelLarge)
                HueSlider(hue = hue, onHueChange = { updateColor(newHue = it) })
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = parseHexColor(hex),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                        ),
                    ) {}
                    Text(hex, style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider()
                TextButton(onClick = { showAdvanced = !showAdvanced }) {
                    Text(if (showAdvanced) "隱藏進階 Hex" else "進階 Hex")
                }
                if (showAdvanced) {
                    OutlinedTextField(
                        value = hex,
                        onValueChange = { input ->
                            hex = input
                            parseHexRgb(input)?.let { rgb ->
                                val hsv = rgbToHsv(rgb)
                                hue = hsv.hue
                                saturation = hsv.saturation
                                value = hsv.value
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Hex 顏色") },
                        supportingText = if (!isValidHexColor(hex)) {
                            { Text("格式：#RRGGBB") }
                        } else {
                            null
                        },
                        isError = !isValidHexColor(hex),
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onColorSelected(requireNotNull(normalizeHexColor(hex)))
                    onDismiss()
                },
                enabled = isValidHexColor(hex),
            ) { Text("套用") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

@Composable
internal fun HueSlider(
    hue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val updateFromX: (Float, Float) -> Unit = { x, width ->
        if (width > 0f) onHueChange((x / width).coerceIn(0f, 1f) * 360f)
    }
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .semantics {
                contentDescription = "色相"
                progressBarRangeInfo = ProgressBarRangeInfo(hue, 0f..360f)
                setProgress { target ->
                    onHueChange(target.coerceIn(0f, 360f))
                    true
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset -> updateFromX(offset.x, size.width.toFloat()) }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    updateFromX(change.position.x, size.width.toFloat())
                }
            },
    ) {
        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Red,
                    Color.Yellow,
                    Color.Green,
                    Color.Cyan,
                    Color.Blue,
                    Color.Magenta,
                    Color.Red,
                ),
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f),
        )
        val thumbX = (hue.coerceIn(0f, 360f) / 360f) * size.width
        drawCircle(Color.White, radius = size.height * 0.34f, center = Offset(thumbX, size.height / 2f))
        drawCircle(
            Color.Black.copy(alpha = 0.65f),
            radius = size.height * 0.34f,
            center = Offset(thumbX, size.height / 2f),
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

@Composable
private fun SaturationValuePicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onChange: (Float, Float) -> Unit,
) {
    val hueColor = hsvToRgb(hue, 1f, 1f).toColor()
    val updateFromPosition: (Offset, androidx.compose.ui.geometry.Size) -> Unit = { offset, size ->
        if (size.width > 0f && size.height > 0f) {
            onChange(
                (offset.x / size.width).coerceIn(0f, 1f),
                1f - (offset.y / size.height).coerceIn(0f, 1f),
            )
        }
    }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color.Transparent, RoundedCornerShape(12.dp))
            .semantics { contentDescription = "飽和度與亮度色盤" }
            .pointerInput(hue) {
                detectTapGestures { offset ->
                    updateFromPosition(offset, androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat()))
                }
            }
            .pointerInput(hue) {
                detectDragGestures { change, _ ->
                    change.consume()
                    updateFromPosition(
                        change.position,
                        androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat()),
                    )
                }
            },
    ) {
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(Color.White, hueColor)),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
        )
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
        )
        val marker = Offset(saturation * size.width, (1f - value) * size.height)
        drawCircle(Color.White, radius = 9.dp.toPx(), center = marker, style = Stroke(3.dp.toPx()))
        drawCircle(Color.Black.copy(alpha = 0.65f), radius = 11.dp.toPx(), center = marker, style = Stroke(1.dp.toPx()))
    }
}
