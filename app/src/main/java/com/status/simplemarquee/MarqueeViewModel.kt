package com.status.simplemarquee

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class MarqueeViewModel(application: Application) : AndroidViewModel(application) {
    private val store = PresetStore(application)
    private val appPreferences = AppPreferences(application)

    var presets by mutableStateOf(store.load())
        private set

    var editorDraft by mutableStateOf<MarqueePreset?>(null)
        private set

    var editorInitialDraft by mutableStateOf<MarqueePreset?>(null)
        private set

    var isCreatingPreset by mutableStateOf(false)
        private set

    val hasUnsavedChanges: Boolean
        get() = hasDraftChanges(editorInitialDraft, editorDraft)

    var isDarkTheme by mutableStateOf(appPreferences.isDarkTheme())
        private set

    var appHue by mutableStateOf(appPreferences.hue())
        private set

    var keepScreenOn by mutableStateOf(appPreferences.keepScreenOn())
        private set

    fun updateDarkTheme(enabled: Boolean) {
        isDarkTheme = enabled
        appPreferences.setDarkTheme(enabled)
    }

    fun updateAppHue(hue: Float) {
        appHue = hue.coerceIn(0f, 360f)
        appPreferences.setHue(appHue)
    }

    fun updateKeepScreenOn(enabled: Boolean) {
        keepScreenOn = enabled
        appPreferences.setKeepScreenOn(enabled)
    }

    fun preset(id: Long?): MarqueePreset? = presets.firstOrNull { it.id == id }

    fun beginEdit(id: Long?) {
        isCreatingPreset = id == null
        val draft = if (id == null) {
            MarqueePreset(
                id = nextId(),
                name = "我的跑馬燈",
                text = "輸入想顯示的文字",
            )
        } else {
            preset(id)
        }
        editorInitialDraft = draft
        editorDraft = draft
    }

    fun updateDraft(transform: (MarqueePreset) -> MarqueePreset) {
        editorDraft = editorDraft?.let(transform)
    }

    fun saveDraft() {
        val draft = editorDraft ?: return
        val existingIndex = presets.indexOfFirst { it.id == draft.id }
        presets = if (existingIndex >= 0) {
            presets.toMutableList().apply { set(existingIndex, draft) }
        } else {
            listOf(draft) + presets
        }
        store.save(presets)
        clearEditor()
    }

    fun cancelEdit() {
        clearEditor()
    }

    private fun clearEditor() {
        editorDraft = null
        editorInitialDraft = null
        isCreatingPreset = false
    }

    fun duplicate(id: Long) {
        val source = preset(id) ?: return
        val copy = source.copy(id = nextId(), name = "${source.name} 副本")
        val sourceIndex = presets.indexOf(source)
        presets = presets.toMutableList().apply { add(sourceIndex + 1, copy) }
        store.save(presets)
    }

    fun delete(id: Long) {
        presets = presets.filterNot { it.id == id }
        store.save(presets)
    }

    fun movePreset(fromIndex: Int, toIndex: Int) {
        presets = moveItem(presets, fromIndex, toIndex)
    }

    fun savePresetOrder() {
        store.save(presets)
    }

    private fun nextId(): Long {
        val now = System.currentTimeMillis()
        return maxOf(now, (presets.maxOfOrNull { it.id } ?: 0L) + 1L)
    }
}

internal fun hasDraftChanges(initial: MarqueePreset?, current: MarqueePreset?): Boolean =
    initial != current

internal fun <T> moveItem(items: List<T>, fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex !in items.indices || toIndex !in items.indices || fromIndex == toIndex) return items
    return items.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
}
