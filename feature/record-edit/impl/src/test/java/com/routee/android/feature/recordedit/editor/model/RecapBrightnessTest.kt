package com.routee.android.feature.recordedit.editor.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RecapBrightnessTest {

    /**
     * GIVEN: 슬라이더가 가운데일 때
     * WHEN: 레이어 투명도를 구하면
     * THEN: 배경 기본 투명도다 (지도 50%)
     */
    @Test
    fun `가운데는 기본 투명도다`() {
        assertEquals(0.5f, overlay(0.5f), DELTA)
    }

    /**
     * GIVEN: 슬라이더가 오른쪽 끝일 때
     * WHEN: 레이어 투명도를 구하면
     * THEN: 어둡게 처리하지 않는다
     */
    @Test
    fun `오른쪽 끝은 투명도 0이다`() {
        assertEquals(0f, overlay(1f), DELTA)
    }

    /**
     * GIVEN: 슬라이더가 왼쪽 끝일 때
     * WHEN: 레이어 투명도를 구하면
     * THEN: 완전히 어둡다
     */
    @Test
    fun `왼쪽 끝은 투명도 1이다`() {
        assertEquals(1f, overlay(0f), DELTA)
    }

    /**
     * GIVEN: 슬라이더 값이 범위를 벗어날 때
     * WHEN: 레이어 투명도를 구하면
     * THEN: 끝 값으로 막힌다
     */
    @Test
    fun `범위를 벗어난 값은 끝 값으로 막힌다`() {
        assertEquals(1f, overlay(-0.3f), DELTA)
        assertEquals(0f, overlay(1.4f), DELTA)
    }

    private fun overlay(sliderValue: Float) =
        RecapBrightness.overlayOpacity(sliderValue, baseOpacity = RecapBrightness.MAP_BASE_OPACITY)

    private companion object {
        const val DELTA = 0.0001f
    }
}
