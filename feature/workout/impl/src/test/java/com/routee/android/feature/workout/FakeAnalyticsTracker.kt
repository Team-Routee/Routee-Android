package com.routee.android.feature.workout

import com.routee.android.core.analytics.AnalyticsEvent
import com.routee.android.core.analytics.AnalyticsTracker

class FakeAnalyticsTracker : AnalyticsTracker {
    override fun track(
        event: AnalyticsEvent,
        properties: Map<String, Any>,
    ) = Unit

    override fun trackScreenView(screenName: String) = Unit
}
