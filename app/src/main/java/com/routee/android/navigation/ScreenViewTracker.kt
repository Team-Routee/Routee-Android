package com.routee.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

// 화면 이동 기록 (Analytics screen_view, Crashlytics 로그)
@Composable
internal fun ScreenViewTracker(
    backStack: List<NavKey>,
    onScreenView: (screenName: String) -> Unit,
) {
    val currentOnScreenView by rememberUpdatedState(onScreenView)
    LaunchedEffect(backStack) {
        snapshotFlow { backStack.lastOrNull()?.let { it::class.simpleName } }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { currentOnScreenView(it) }
    }
}
