package com.status.simplemarquee

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreensTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val preset = MarqueePreset(
        id = 1L,
        name = "測試",
        text = "跑馬燈測試",
        themeMode = MarqueeThemeMode.RAINBOW_ANIMATED,
    )

    @Test
    fun unchangedNewDraftClosesWithoutConfirmation() {
        var closed = false
        composeRule.setContent {
            SimpleMarqueeTheme(darkTheme = false, hue = DefaultAppHue) {
                EditorScreen(
                    draft = preset,
                    isCreating = true,
                    hasChanges = false,
                    onChange = {},
                    onSave = {},
                    onClose = { closed = true },
                )
            }
        }

        composeRule.onNodeWithText("取消").performClick()

        composeRule.runOnIdle { assertTrue(closed) }
        composeRule.onNodeWithText("放棄變更？").assertDoesNotExist()
    }

    @Test
    fun changedNewDraftAsksForConfirmation() {
        var closed = false
        composeRule.setContent {
            SimpleMarqueeTheme(darkTheme = false, hue = DefaultAppHue) {
                EditorScreen(
                    draft = preset.copy(name = "已修改"),
                    isCreating = true,
                    hasChanges = true,
                    onChange = {},
                    onSave = {},
                    onClose = { closed = true },
                )
            }
        }

        composeRule.onNodeWithText("取消").performClick()

        composeRule.runOnIdle { assertFalse(closed) }
        composeRule.onNodeWithText("放棄變更？").assertExists()
    }

    @Test
    fun playerControlsHideAfterThreeSecondsAndCanBeShownAgain() {
        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.setContent {
                PlayerScreen(preset = preset, keepScreenOn = true, onExit = {})
            }

            composeRule.onNodeWithText("暫停").assertExists()
            composeRule.mainClock.advanceTimeBy(2_999)
            composeRule.onNodeWithText("暫停").assertExists()
            composeRule.mainClock.advanceTimeBy(2)
            composeRule.waitForIdle()
            composeRule.onNodeWithText("暫停").assertDoesNotExist()

            composeRule.onNodeWithTag("player-screen").performClick()
            composeRule.onNodeWithText("暫停").assertExists()
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun pausedPlayerControlsRemainVisible() {
        composeRule.mainClock.autoAdvance = false
        try {
            composeRule.setContent {
                PlayerScreen(preset = preset, keepScreenOn = true, onExit = {})
            }

            composeRule.onNodeWithText("暫停").performClick()
            composeRule.mainClock.advanceTimeBy(5_000)
            composeRule.waitForIdle()

            composeRule.onNodeWithText("繼續").assertExists()
            composeRule.onNodeWithText("播放中").assertDoesNotExist()
            composeRule.onNodeWithText("已暫停").assertDoesNotExist()
        } finally {
            composeRule.mainClock.autoAdvance = true
        }
    }

    @Test
    fun settingsShowVersionAndBuildInformation() {
        composeRule.setContent {
            SimpleMarqueeTheme(darkTheme = false, hue = DefaultAppHue) {
                SettingsScreen(
                    darkTheme = false,
                    appHue = DefaultAppHue,
                    keepScreenOn = true,
                    versionName = "1.0.2",
                    buildId = "20260909.153012.123",
                    onDarkThemeChange = {},
                    onHueChange = {},
                    onKeepScreenOnChange = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("版本 1.0.2").assertExists()
        composeRule.onNodeWithText("Build 20260909.153012.123").assertExists()
        composeRule.onNodeWithText("深色模式").assertExists()
        composeRule.onNodeWithText("明亮模式").assertDoesNotExist()
        composeRule.onNodeWithText("播放畫面保持常亮").assertExists()
    }

    @Test
    fun playerCanStartPausedWithoutVisibleStatusText() {
        composeRule.setContent {
            PlayerScreen(
                preset = preset.copy(startPaused = true),
                keepScreenOn = false,
                onExit = {},
            )
        }

        composeRule.onNodeWithText("繼續").assertExists()
        composeRule.onNodeWithText("播放中").assertDoesNotExist()
        composeRule.onNodeWithText("已暫停").assertDoesNotExist()
    }

    @Test
    fun homeUsesIconActionsAndHidesLowFrequencyCardActions() {
        var addRequested = false
        var settingsOpened = false
        var exitRequested = false
        composeRule.setContent {
            SimpleMarqueeTheme(darkTheme = false, hue = DefaultAppHue) {
                HomeScreen(
                    presets = listOf(preset),
                    onSettings = { settingsOpened = true },
                    onExitApp = { exitRequested = true },
                    onAdd = { addRequested = true },
                    onEdit = {},
                    onPlay = {},
                    onDuplicate = {},
                    onDelete = {},
                    onMove = { _, _ -> },
                    onSaveOrder = {},
                )
            }
        }

        val addButton = composeRule.onNodeWithContentDescription("新增跑馬燈")
        val settingsButton = composeRule.onNodeWithContentDescription("設定")
        assertTrue(addButton.fetchSemanticsNode().boundsInRoot.left < settingsButton.fetchSemanticsNode().boundsInRoot.left)
        addButton.performClick()
        settingsButton.performClick()
        composeRule.onNodeWithContentDescription("離開應用程式").performClick()
        composeRule.runOnIdle {
            assertTrue(addRequested)
            assertTrue(settingsOpened)
            assertTrue(exitRequested)
        }
        composeRule.onNodeWithText("＋ 新增跑馬燈").assertDoesNotExist()
        composeRule.onNodeWithText("複製").assertDoesNotExist()
        composeRule.onNodeWithText("刪除").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("測試的更多操作").performClick()
        composeRule.onNodeWithText("複製").assertExists()
        composeRule.onNodeWithText("刪除").assertExists()
    }

    @Test
    fun editorShowsColorPickerAndContrastStatus() {
        composeRule.setContent {
            SimpleMarqueeTheme(darkTheme = false, hue = DefaultAppHue) {
                EditorScreen(
                    draft = preset.copy(themeMode = MarqueeThemeMode.SOLID),
                    isCreating = false,
                    hasChanges = false,
                    onChange = {},
                    onSave = {},
                    onClose = {},
                )
            }
        }

        composeRule.onNodeWithText("對比", substring = true).assertExists()
        composeRule.onNodeWithText("文字色").performClick()
        composeRule.onNodeWithText("選擇文字色").assertExists()
    }
}
