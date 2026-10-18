package com.routee.android.core.ui.loading

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 전역 로딩 상태
 *
 * [RouteeLoadingOverlay]를 호출한 화면이 하나라도 남아 있으면 로딩 중이다
 */
@Stable
class RouteeLoadingState {
    private var requestCount by mutableIntStateOf(0)

    val isLoading: Boolean get() = requestCount > 0

    internal fun show() {
        requestCount++
    }

    internal fun hide() {
        requestCount = (requestCount - 1).coerceAtLeast(0)
    }
}

/**
 * 앱 루트에서 제공하는 전역 로딩 상태
 */
val LocalRouteeLoadingState = staticCompositionLocalOf { RouteeLoadingState() }
