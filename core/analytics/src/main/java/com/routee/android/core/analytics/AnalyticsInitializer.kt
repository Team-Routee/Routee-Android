package com.routee.android.core.analytics

import com.routee.android.core.common.di.ApplicationScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 앱 시작 시 분석 설정. [initialize]는 Application.onCreate 에서 호출
 */
class AnalyticsInitializer @Inject internal constructor(
    private val tracker: AnalyticsTrackerImpl,
    @ApplicationScope private val scope: CoroutineScope,
) {
    fun initialize() {
        scope.launch {
            runCatching { tracker.registerInstallReferrer() }
                .onFailure { Timber.w(it, "설치 경로 등록 실패") }
        }
    }
}
