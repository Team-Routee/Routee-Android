package com.routee.android.core.designsystem.component.skeleton

import androidx.compose.animation.core.Easing
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * [from] → [to] 그라디언트 색 정지점을 [easing] 곡선에 맞춰 나눈다
 *
 * @param steps 구간 수, 정지점은 steps + 1 개
 */
internal fun easedGradientColors(
    from: Color,
    to: Color,
    easing: Easing,
    steps: Int = 8,
): List<Color> = List(steps + 1) { index -> lerp(from, to, easing.transform(index / steps.toFloat())) }

// 피그마의 "같은 색 0% → 다른 색 100%" 그라디언트는 밝기가 제곱 곡선으로 변함 (Android는 직선이라 더 퍼져 보임)
internal val FigmaAlphaFadeEasing = Easing { fraction -> fraction * fraction }
