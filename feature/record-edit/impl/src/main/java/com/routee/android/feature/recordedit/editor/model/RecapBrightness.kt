package com.routee.android.feature.recordedit.editor.model

/**
 * 배경 밝기 슬라이더 값과 배경 위 검은 레이어 투명도 (RT-EDIT-005)
 *
 * 슬라이더 가운데가 기본 투명도. 왼쪽 끝은 완전히 어둡게(1), 오른쪽 끝은 어둡게 처리 없음(0)
 */
internal object RecapBrightness {
    const val DEFAULT_SLIDER_VALUE = 0.5f

    // 배경이 기본 지도일 때
    const val MAP_BASE_OPACITY = 0.5f

    const val PHOTO_BASE_OPACITY = 0.4f

    fun overlayOpacity(
        sliderValue: Float,
        baseOpacity: Float,
    ): Float {
        val value = sliderValue.coerceIn(0f, 1f)
        return if (value < DEFAULT_SLIDER_VALUE) {
            val progress = (DEFAULT_SLIDER_VALUE - value) / DEFAULT_SLIDER_VALUE
            baseOpacity + (1f - baseOpacity) * progress
        } else {
            val progress = (value - DEFAULT_SLIDER_VALUE) / DEFAULT_SLIDER_VALUE
            baseOpacity * (1f - progress)
        }
    }
}
