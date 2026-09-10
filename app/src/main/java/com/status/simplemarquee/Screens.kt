package com.status.simplemarquee

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.compose.LocalActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

private data class ThemeChoice(
    val name: String,
    val foreground: String,
    val background: String,
    val mode: MarqueeThemeMode,
)

private val ThemeChoices = listOf(
    ThemeChoice("黑白", "#FFFFFF", "#000000", MarqueeThemeMode.SOLID),
    ThemeChoice("警示黃", "#FFD54F", "#050505", MarqueeThemeMode.SOLID),
    ThemeChoice("螢光綠", "#46FFB6", "#071B18", MarqueeThemeMode.SOLID),
    ThemeChoice("霓虹粉", "#FF4FA3", "#210817", MarqueeThemeMode.SOLID),
    ThemeChoice("海洋藍", "#66D9FF", "#061A26", MarqueeThemeMode.SOLID),
    ThemeChoice("烈焰橘", "#FF6B35", "#210900", MarqueeThemeMode.SOLID),
    ThemeChoice("紫電", "#C8A8FF", "#130A29", MarqueeThemeMode.SOLID),
    ThemeChoice("冰雪", "#E9F8FF", "#12364D", MarqueeThemeMode.SOLID),
    ThemeChoice("紅白應援", "#FFFFFF", "#C90035", MarqueeThemeMode.SOLID),
    ThemeChoice("薄荷檸檬", "#D9FF57", "#10230B", MarqueeThemeMode.SOLID),
    ThemeChoice("靜態彩虹", "#FFFFFF", "#000000", MarqueeThemeMode.RAINBOW),
    ThemeChoice("動態彩虹", "#FFFFFF", "#000000", MarqueeThemeMode.RAINBOW_ANIMATED),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    presets: List<MarqueePreset>,
    onSettings: () -> Unit,
    onExitApp: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onPlay: (Long) -> Unit,
    onDuplicate: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onMove: (Int, Int) -> Unit,
    onSaveOrder: () -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<MarqueePreset?>(null) }
    var draggedId by remember { mutableStateOf<Long?>(null) }
    var draggedOffset by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val currentPresets by rememberUpdatedState(presets)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("簡單跑馬燈", fontWeight = FontWeight.Bold)
                        Text(
                            "選一則訊息，立即全螢幕播放",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAdd) {
                        Icon(Icons.Default.Add, contentDescription = "新增跑馬燈")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "設定")
                    }
                    IconButton(onClick = onExitApp) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "離開應用程式")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        if (presets.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("還沒有跑馬燈", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    "建立第一則訊息，之後就能一鍵播放。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))
                Button(onClick = onAdd) { Text("建立跑馬燈") }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding()),
                state = listState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                itemsIndexed(presets, key = { _, preset -> preset.id }) { index, preset ->
                    PresetCard(
                        preset = preset,
                        modifier = Modifier.graphicsLayer {
                            translationY = if (draggedId == preset.id) draggedOffset else 0f
                        },
                        canMoveUp = index > 0,
                        canMoveDown = index < presets.lastIndex,
                        onPlay = { onPlay(preset.id) },
                        onEdit = { onEdit(preset.id) },
                        onDuplicate = { onDuplicate(preset.id) },
                        onDelete = { pendingDelete = preset },
                        onMoveUp = {
                            onMove(index, index - 1)
                            onSaveOrder()
                        },
                        onMoveDown = {
                            onMove(index, index + 1)
                            onSaveOrder()
                        },
                        dragHandleModifier = Modifier.pointerInput(preset.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggedId = preset.id
                                    draggedOffset = 0f
                                },
                                onDragCancel = {
                                    draggedId = null
                                    draggedOffset = 0f
                                    onSaveOrder()
                                },
                                onDragEnd = {
                                    draggedId = null
                                    draggedOffset = 0f
                                    onSaveOrder()
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    draggedOffset += dragAmount.y
                                    val currentIndex = currentPresets.indexOfFirst { it.id == preset.id }
                                    val currentItem = listState.layoutInfo.visibleItemsInfo
                                        .firstOrNull { it.key == preset.id }
                                    if (currentIndex < 0 || currentItem == null) return@detectDragGesturesAfterLongPress
                                    val draggedCenter = currentItem.offset + draggedOffset + currentItem.size / 2f
                                    val target = listState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
                                        item.key != preset.id &&
                                            draggedCenter >= item.offset &&
                                            draggedCenter <= item.offset + item.size
                                    }
                                    if (target != null && target.index in currentPresets.indices) {
                                        onMove(currentIndex, target.index)
                                        draggedOffset += currentItem.offset - target.offset
                                    }
                                    val viewportStart = listState.layoutInfo.viewportStartOffset + 72f
                                    val viewportEnd = listState.layoutInfo.viewportEndOffset - 72f
                                    when {
                                        draggedCenter < viewportStart -> scope.launch { listState.scrollBy(-24f) }
                                        draggedCenter > viewportEnd -> scope.launch { listState.scrollBy(24f) }
                                    }
                                },
                            )
                        },
                    )
                }
            }
        }
    }

    pendingDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("刪除「${preset.name}」？") },
            text = { Text("刪除後無法復原。") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(preset.id)
                    pendingDelete = null
                }) { Text("刪除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    appHue: Float,
    keepScreenOn: Boolean,
    versionName: String,
    buildId: String,
    onDarkThemeChange: (Boolean) -> Unit,
    onHueChange: (Float) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("介面設定", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                top = padding.calculateTopPadding() + 12.dp,
                end = 16.dp,
                bottom = padding.calculateBottomPadding() + 48.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SettingCard("顯示模式") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("深色模式", fontWeight = FontWeight.Bold)
                            Text(
                                "導覽列與狀態列會同步切換。",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
                    }
                }
            }
            item {
                SettingCard("播放設定") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("播放畫面保持常亮", fontWeight = FontWeight.Bold)
                            Text(
                                "播放跑馬燈時不自動關閉螢幕。",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(
                            checked = keepScreenOn,
                            onCheckedChange = onKeepScreenOnChange,
                            modifier = Modifier.testTag("keep-screen-on-switch"),
                        )
                    }
                }
            }
            item {
                SettingCard("介面主題") {
                    Text(
                        "選擇快捷色，或拖曳色相滑桿自訂介面色彩。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(14.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AppPalette.entries.forEach { palette ->
                            HuePresetOption(
                                palette = palette,
                                selected = kotlin.math.abs(appHue - palette.hue) < 0.5f,
                                onClick = { onHueChange(palette.hue) },
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        "自訂色相 ${appHue.roundToInt()}°",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Spacer(Modifier.height(8.dp))
                    HueSlider(hue = appHue, onHueChange = onHueChange)
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "版本 $versionName",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "Build $buildId",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun HuePresetOption(
    palette: AppPalette,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.semantics {
            contentDescription = "${palette.displayName}主題"
            this.selected = selected
            role = Role.RadioButton
        },
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = if (selected) 3.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(hsvToRgb(palette.hue, 0.78f, 0.9f).toColor()),
            )
            Text(palette.displayName, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PresetCard(
    preset: MarqueePreset,
    modifier: Modifier = Modifier,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    dragHandleModifier: Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(preset.backgroundColor)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (preset.themeMode == MarqueeThemeMode.SOLID) {
                                    SolidColor(parseHexColor(preset.foregroundColor))
                                } else {
                                    Brush.linearGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Magenta))
                                },
                            ),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(preset.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        preset.text.replace('\n', ' '),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = dragHandleModifier.size(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.DragHandle, contentDescription = "拖曳排序：${preset.name}")
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(onClick = onPlay, modifier = Modifier.fillMaxWidth()) {
                Text("播放")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onEdit) { Text("編輯") }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "${preset.name}的更多操作")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("複製") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onDuplicate()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("上移") },
                            enabled = canMoveUp,
                            onClick = {
                                menuExpanded = false
                                onMoveUp()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("下移") },
                            enabled = canMoveDown,
                            onClick = {
                                menuExpanded = false
                                onMoveDown()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("刪除", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    draft: MarqueePreset,
    isCreating: Boolean,
    hasChanges: Boolean,
    onChange: ((MarqueePreset) -> MarqueePreset) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    var colorPickerTarget by rememberSaveable { mutableStateOf<String?>(null) }
    var showAdvancedColors by rememberSaveable { mutableStateOf(false) }
    val foregroundValid = isValidHexColor(draft.foregroundColor)
    val backgroundValid = isValidHexColor(draft.backgroundColor)
    val contrast = marqueeContrastRatio(draft)
    val canSave = draft.name.isNotBlank() && draft.text.isNotBlank() && foregroundValid && backgroundValid

    fun requestClose() {
        if (hasChanges) showDiscardDialog = true else onClose()
    }

    BackHandler { requestClose() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCreating) "新增跑馬燈" else "編輯跑馬燈") },
                navigationIcon = {
                    TextButton(onClick = ::requestClose) { Text("取消") }
                },
                actions = {
                    TextButton(onClick = onSave, enabled = canSave) {
                        Text("儲存", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                end = 16.dp,
                bottom = 72.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = parseHexColor(draft.backgroundColor, Color.Black),
                    ),
                ) {
                    MarqueeText(
                        preset = draft,
                        isPlaying = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                    )
                }
            }
            item {
                SettingCard("內容") {
                    OutlinedTextField(
                        value = draft.name,
                        onValueChange = { value -> onChange { it.copy(name = value) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("預設名稱") },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = draft.text,
                        onValueChange = { value -> onChange { it.copy(text = value) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("顯示文字") },
                        minLines = 3,
                    )
                }
            }
            item {
                SettingCard("播放方向") {
                    Text(
                        "只在全螢幕播放時套用。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ChoiceChip(
                            label = "跟隨系統",
                            selected = draft.orientation == MarqueeOrientation.AUTO,
                            onClick = { onChange { it.copy(orientation = MarqueeOrientation.AUTO) } },
                        )
                        ChoiceChip(
                            label = "鎖定橫屏",
                            selected = draft.orientation == MarqueeOrientation.LANDSCAPE,
                            onClick = { onChange { it.copy(orientation = MarqueeOrientation.LANDSCAPE) } },
                        )
                        ChoiceChip(
                            label = "鎖定直屏",
                            selected = draft.orientation == MarqueeOrientation.PORTRAIT,
                            onClick = { onChange { it.copy(orientation = MarqueeOrientation.PORTRAIT) } },
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("開始時為靜止狀態", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Switch(
                            checked = draft.startPaused,
                            onCheckedChange = { value -> onChange { it.copy(startPaused = value) } },
                            modifier = Modifier.testTag("start-paused-switch"),
                        )
                    }
                }
            }
            item {
                SettingCard("主題色彩") {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ThemeChoices.forEach { choice ->
                            val selected = draft.themeMode == choice.mode &&
                                draft.foregroundColor.equals(choice.foreground, true) &&
                                draft.backgroundColor.equals(choice.background, true)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onChange {
                                        it.copy(
                                            foregroundColor = choice.foreground,
                                            backgroundColor = choice.background,
                                            themeMode = choice.mode,
                                        )
                                    }
                                },
                                label = { Text(choice.name) },
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ColorSelector(
                            label = "文字色",
                            color = draft.foregroundColor,
                            enabled = draft.themeMode == MarqueeThemeMode.SOLID,
                            onClick = { colorPickerTarget = "foreground" },
                            modifier = Modifier.weight(1f),
                        )
                        ColorSelector(
                            label = "背景色",
                            color = draft.backgroundColor,
                            onClick = { colorPickerTarget = "background" },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    contrast?.let { ratio ->
                        val lowContrast = ratio < 4.5f
                        Surface(
                            color = if (lowContrast) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = if (lowContrast) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text = if (lowContrast) {
                                    "對比 %.1f:1，低於 WCAG AA 建議的 4.5:1".format(Locale.US, ratio)
                                } else {
                                    "對比 %.1f:1，符合 WCAG AA".format(Locale.US, ratio)
                                },
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    TextButton(onClick = { showAdvancedColors = !showAdvancedColors }) {
                        Text(if (showAdvancedColors) "隱藏進階 Hex" else "進階 Hex")
                    }
                    if (showAdvancedColors) {
                        OutlinedTextField(
                            value = draft.foregroundColor,
                            onValueChange = { value ->
                                onChange { it.copy(foregroundColor = value, themeMode = MarqueeThemeMode.SOLID) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("文字色 Hex") },
                            singleLine = true,
                            isError = !foregroundValid,
                            supportingText = if (!foregroundValid) ({ Text("格式：#RRGGBB") }) else null,
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = draft.backgroundColor,
                            onValueChange = { value -> onChange { it.copy(backgroundColor = value) } },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("背景色 Hex") },
                            singleLine = true,
                            isError = !backgroundValid,
                            supportingText = if (!backgroundValid) ({ Text("格式：#RRGGBB") }) else null,
                        )
                    }
                }
            }
            item {
                SettingCard("文字效果") {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ChoiceChip(
                            label = "無效果",
                            selected = draft.textEffect == MarqueeTextEffect.NONE,
                            onClick = { onChange { it.copy(textEffect = MarqueeTextEffect.NONE) } },
                        )
                        ChoiceChip(
                            label = "描邊",
                            selected = draft.textEffect == MarqueeTextEffect.OUTLINE,
                            onClick = { onChange { it.copy(textEffect = MarqueeTextEffect.OUTLINE) } },
                        )
                        ChoiceChip(
                            label = "呼吸亮度",
                            selected = draft.textEffect == MarqueeTextEffect.BREATHING_BRIGHTNESS,
                            onClick = {
                                onChange { it.copy(textEffect = MarqueeTextEffect.BREATHING_BRIGHTNESS) }
                            },
                        )
                        ChoiceChip(
                            label = "霓虹",
                            selected = draft.textEffect == MarqueeTextEffect.NEON,
                            onClick = { onChange { it.copy(textEffect = MarqueeTextEffect.NEON) } },
                        )
                    }
                }
            }
            item {
                SettingCard("文字樣式") {
                    ValueSlider(
                        label = "字體大小",
                        valueLabel = "${draft.fontSize.roundToInt()} sp",
                        value = draft.fontSize,
                        range = 24f..180f,
                        onValueChange = { value -> onChange { it.copy(fontSize = value) } },
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    ValueSlider(
                        label = "字距",
                        valueLabel = String.format(Locale.US, "%.2f em", draft.letterSpacing),
                        value = draft.letterSpacing,
                        range = -0.05f..0.30f,
                        onValueChange = { value -> onChange { it.copy(letterSpacing = value) } },
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    ValueSlider(
                        label = "字重",
                        valueLabel = draft.fontWeight.toString(),
                        value = draft.fontWeight.toFloat(),
                        range = 100f..900f,
                        steps = 7,
                        onValueChange = { value ->
                            onChange { it.copy(fontWeight = (value / 100f).roundToInt() * 100) }
                        },
                    )
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("鏡像文字模式", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Switch(
                            checked = draft.mirrorText,
                            onCheckedChange = { value -> onChange { it.copy(mirrorText = value) } },
                            modifier = Modifier.testTag("mirror-text-switch"),
                        )
                    }
                }
            }
            item {
                SettingCard("滾動") {
                    ValueSlider(
                        label = "速度",
                        valueLabel = speedLabel(draft.speed),
                        value = draft.speed,
                        range = 40f..400f,
                        onValueChange = { value -> onChange { it.copy(speed = value) } },
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    ValueSlider(
                        label = "循環間距",
                        valueLabel = "${draft.loopGap.roundToInt()} dp",
                        value = draft.loopGap,
                        range = 32f..240f,
                        onValueChange = { value -> onChange { it.copy(loopGap = value) } },
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("方向", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChoiceChip(
                            label = "向左",
                            selected = draft.direction == MarqueeDirection.LEFT,
                            onClick = { onChange { it.copy(direction = MarqueeDirection.LEFT) } },
                        )
                        ChoiceChip(
                            label = "向右",
                            selected = draft.direction == MarqueeDirection.RIGHT,
                            onClick = { onChange { it.copy(direction = MarqueeDirection.RIGHT) } },
                        )
                    }
                }
            }
        }
    }

    colorPickerTarget?.let { target ->
        val foreground = target == "foreground"
        ColorPickerDialog(
            title = if (foreground) "選擇文字色" else "選擇背景色",
            initialColor = if (foreground) draft.foregroundColor else draft.backgroundColor,
            onColorSelected = { color ->
                onChange {
                    if (foreground) {
                        it.copy(foregroundColor = color, themeMode = MarqueeThemeMode.SOLID)
                    } else {
                        it.copy(backgroundColor = color)
                    }
                }
            },
            onDismiss = { colorPickerTarget = null },
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("放棄變更？") },
            text = { Text("尚未儲存的內容將會遺失。") },
            confirmButton = {
                TextButton(onClick = onClose) {
                    Text("放棄", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("繼續編輯") }
            },
        )
    }
}

@Composable
private fun ColorSelector(
    label: String,
    color: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(parseHexColor(color, Color.Transparent)),
            )
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium)
                Text(
                    if (enabled) color.uppercase(Locale.US) else "彩虹模式",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun ValueSlider(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(valueLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
    Slider(
        value = value.coerceIn(range),
        onValueChange = onValueChange,
        valueRange = range,
        steps = steps,
    )
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

private fun speedLabel(speed: Float): String = when {
    speed < 100f -> "慢 ${speed.roundToInt()}"
    speed < 220f -> "適中 ${speed.roundToInt()}"
    else -> "快 ${speed.roundToInt()}"
}

@Composable
fun PlayerScreen(preset: MarqueePreset, keepScreenOn: Boolean, onExit: () -> Unit) {
    val activity = requireNotNull(LocalActivity.current)
    val view = LocalView.current
    var isPlaying by rememberSaveable(preset.id, preset.startPaused) { mutableStateOf(!preset.startPaused) }
    var controlsVisible by rememberSaveable(preset.id) { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(preset.orientation) {
        activity.requestedOrientation = when (preset.orientation) {
            MarqueeOrientation.AUTO -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            MarqueeOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            MarqueeOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }
    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(3_000)
            controlsVisible = false
        }
    }
    DisposableEffect(view, keepScreenOn) {
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, view)
        view.keepScreenOn = keepScreenOn
        WindowCompat.setDecorFitsSystemWindows(window, false)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            view.keepScreenOn = false
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("player-screen")
            .background(parseHexColor(preset.backgroundColor, Color.Black))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { controlsVisible = !controlsVisible },
            ),
        contentAlignment = Alignment.Center,
    ) {
        MarqueeText(
            preset = preset,
            isPlaying = isPlaying,
            modifier = Modifier.fillMaxSize(),
        )

        if (controlsVisible) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .safeDrawingPadding()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.82f),
                contentColor = Color.White,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onExit,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                    ) {
                        Text("返回")
                    }
                    Button(
                        onClick = {
                            isPlaying = !isPlaying
                            controlsVisible = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black,
                        ),
                    ) {
                        Text(if (isPlaying) "暫停" else "繼續")
                    }
                }
            }
        }
    }
}
