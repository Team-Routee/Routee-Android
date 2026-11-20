package com.routee.android.core.analytics

/**
 * 분석 기록 (이벤트는 Mixpanel, 화면 이동은 Firebase Analytics)
 */
interface AnalyticsTracker {
    /**
     * @param properties 이벤트 속성. 키는 snake_case
     */
    fun track(event: AnalyticsEvent, properties: Map<String, Any> = emptyMap())

    /**
     * @param screenName 화면 이름 (NavKey 클래스 이름)
     */
    fun trackScreenView(screenName: String)
}
