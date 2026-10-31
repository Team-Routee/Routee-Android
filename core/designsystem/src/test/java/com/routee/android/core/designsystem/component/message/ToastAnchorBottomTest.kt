package com.routee.android.core.designsystem.component.message

import org.junit.Assert.assertEquals
import org.junit.Test

class ToastAnchorBottomTest {
    @Test
    fun incomingAnchorTakesPriorityOverExitingAnchor() {
        val anchors = listOf(MessageAnchor(240) { false }, MessageAnchor(120) { true })

        assertEquals(120, toastAnchorBottom(anchors, 800))
    }

    @Test
    fun visibleExitingAnchorIsUsedUntilIncomingAnchorIsPlaced() {
        assertEquals(120, toastAnchorBottom(listOf(MessageAnchor(120) { false }), 800))
    }

    @Test
    fun offscreenOrRemovedAnchorsDoNotLeaveAnOffset() {
        val anchors = listOf(MessageAnchor(-10) { false }, MessageAnchor(900) { false })

        assertEquals(0, toastAnchorBottom(anchors, 800))
        assertEquals(0, toastAnchorBottom(emptyList(), 800))
    }
}
