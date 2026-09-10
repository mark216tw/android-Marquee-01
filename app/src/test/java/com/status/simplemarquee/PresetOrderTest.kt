package com.status.simplemarquee

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class PresetOrderTest {
    @Test
    fun movesFirstItemToLast() {
        assertEquals(listOf("B", "C", "A"), moveItem(listOf("A", "B", "C"), 0, 2))
    }

    @Test
    fun movesLastItemToFirst() {
        assertEquals(listOf("C", "A", "B"), moveItem(listOf("A", "B", "C"), 2, 0))
    }

    @Test
    fun invalidMoveKeepsOriginalList() {
        val items = listOf("A", "B")

        assertSame(items, moveItem(items, -1, 1))
        assertSame(items, moveItem(items, 0, 3))
        assertSame(items, moveItem(items, 1, 1))
    }
}
