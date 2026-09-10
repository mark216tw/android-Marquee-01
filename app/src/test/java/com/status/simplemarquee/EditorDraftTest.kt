package com.status.simplemarquee

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorDraftTest {
    private val initial = MarqueePreset(
        id = 1L,
        name = "我的跑馬燈",
        text = "輸入想顯示的文字",
    )

    @Test
    fun unchangedNewDraftHasNoChanges() {
        assertFalse(hasDraftChanges(initial, initial.copy()))
    }

    @Test
    fun editedNewDraftHasChanges() {
        assertTrue(hasDraftChanges(initial, initial.copy(name = "活動訊息")))
    }

    @Test
    fun revertedDraftHasNoChanges() {
        val edited = initial.copy(text = "更新內容")

        assertTrue(hasDraftChanges(initial, edited))
        assertFalse(hasDraftChanges(initial, edited.copy(text = initial.text)))
    }
}
