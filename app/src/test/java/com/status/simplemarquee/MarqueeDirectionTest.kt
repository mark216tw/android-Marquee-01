package com.status.simplemarquee

import org.junit.Assert.assertEquals
import org.junit.Test

class MarqueeDirectionTest {
    @Test
    fun normalTextKeepsSelectedDirection() {
        assertEquals(
            MarqueeDirection.LEFT,
            effectiveMarqueeDirection(MarqueeDirection.LEFT, mirrorText = false),
        )
        assertEquals(
            MarqueeDirection.RIGHT,
            effectiveMarqueeDirection(MarqueeDirection.RIGHT, mirrorText = false),
        )
    }

    @Test
    fun mirroredTextReversesSelectedDirection() {
        assertEquals(
            MarqueeDirection.RIGHT,
            effectiveMarqueeDirection(MarqueeDirection.LEFT, mirrorText = true),
        )
        assertEquals(
            MarqueeDirection.LEFT,
            effectiveMarqueeDirection(MarqueeDirection.RIGHT, mirrorText = true),
        )
    }
}
