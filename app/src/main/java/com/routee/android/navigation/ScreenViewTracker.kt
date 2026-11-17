package com.routee.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.navigation3.runtime.NavKey
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import timber.log.Timber

// 화면 이동 기록 (Analytics screen_view, Crashlytics 로그)
@Composable
internal fun ScreenViewTracker(backStack: List<NavKey>) {
    LaunchedEffect(backStack) {
        snapshotFlow { backStack.lastOrNull()?.let { it::class.simpleName } }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { screenName ->
                Timber.i("화면: $screenName")
                Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                    param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                }
            }
    }
}
