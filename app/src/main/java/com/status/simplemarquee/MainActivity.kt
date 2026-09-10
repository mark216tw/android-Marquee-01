package com.status.simplemarquee

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val marqueeViewModel: MarqueeViewModel = viewModel()
            SimpleMarqueeTheme(
                darkTheme = marqueeViewModel.isDarkTheme,
                hue = marqueeViewModel.appHue,
            ) {
                AppSystemBars(marqueeViewModel.isDarkTheme)
                MarqueeApp(marqueeViewModel)
            }
        }
    }
}

@Composable
private fun AppSystemBars(darkTheme: Boolean) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    val barColor = MaterialTheme.colorScheme.background.toArgb()
    SideEffect {
        activity.enableEdgeToEdge(
            statusBarStyle = if (darkTheme) {
                SystemBarStyle.dark(barColor)
            } else {
                SystemBarStyle.light(barColor, barColor)
            },
            navigationBarStyle = if (darkTheme) {
                SystemBarStyle.dark(barColor)
            } else {
                SystemBarStyle.light(barColor, barColor)
            },
        )
    }
}

@Composable
private fun MarqueeApp(viewModel: MarqueeViewModel = viewModel()) {
    var screen by rememberSaveable { mutableStateOf("home") }
    var selectedId by rememberSaveable { mutableLongStateOf(-1L) }
    val activity = requireNotNull(LocalActivity.current)

    fun openEditor(id: Long?) {
        viewModel.beginEdit(id)
        selectedId = id ?: -1L
        screen = "editor"
    }

    fun leavePlayer() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        screen = "home"
    }

    when (screen) {
        "editor" -> {
            if (viewModel.editorDraft == null) {
                LaunchedEffect(selectedId) {
                    viewModel.beginEdit(selectedId.takeIf { it >= 0L })
                }
            }
            viewModel.editorDraft?.let { draft ->
                EditorScreen(
                    draft = draft,
                    isCreating = viewModel.isCreatingPreset,
                    hasChanges = viewModel.hasUnsavedChanges,
                    onChange = viewModel::updateDraft,
                    onSave = {
                        viewModel.saveDraft()
                        screen = "home"
                    },
                    onClose = {
                        viewModel.cancelEdit()
                        screen = "home"
                    },
                )
            }
        }

        "player" -> {
            val preset = viewModel.preset(selectedId)
            if (preset == null) {
                LaunchedEffect(Unit) { screen = "home" }
            } else {
                BackHandler { leavePlayer() }
                PlayerScreen(
                    preset = preset,
                    keepScreenOn = viewModel.keepScreenOn,
                    onExit = ::leavePlayer,
                )
            }
        }

        "settings" -> SettingsScreen(
            darkTheme = viewModel.isDarkTheme,
            appHue = viewModel.appHue,
            keepScreenOn = viewModel.keepScreenOn,
            versionName = BuildConfig.VERSION_NAME,
            buildId = BuildConfig.BUILD_ID,
            onDarkThemeChange = viewModel::updateDarkTheme,
            onHueChange = viewModel::updateAppHue,
            onKeepScreenOnChange = viewModel::updateKeepScreenOn,
            onBack = { screen = "home" },
        )

        else -> HomeScreen(
            presets = viewModel.presets,
            onSettings = { screen = "settings" },
            onExitApp = { activity.finishAndRemoveTask() },
            onAdd = { openEditor(null) },
            onEdit = { openEditor(it) },
            onPlay = {
                selectedId = it
                screen = "player"
            },
            onDuplicate = viewModel::duplicate,
            onDelete = viewModel::delete,
            onMove = viewModel::movePreset,
            onSaveOrder = viewModel::savePresetOrder,
        )
    }
}
