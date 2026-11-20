package com.routee.android.core.analytics

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
internal class AnalyticsTrackerImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val installReferrerReader: InstallReferrerReader,
) : AnalyticsTracker {
    private val mixpanel: MixpanelAPI? = BuildConfig.MIXPANEL_TOKEN
        .takeIf { it.isNotEmpty() }
        ?.let { token -> MixpanelAPI.getInstance(context, token, false) }
        ?.apply { setEnableLogging(BuildConfig.DEBUG) }
        ?: run {
            Timber.w("Mixpanel 토큰 없음, 이벤트 전송 안 함")
            null
        }

    override fun track(event: AnalyticsEvent, properties: Map<String, Any>) {
        mixpanel?.trackMap(event.eventName, properties)
    }

    override fun trackScreenView(screenName: String) {
        Timber.i("화면: $screenName")
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }

    suspend fun registerInstallReferrer() {
        val mixpanel = mixpanel ?: return
        if (mixpanel.superProperties.has(UTM_SOURCE)) return
        val utm = installReferrerReader.readUtm()
        if (utm.isNotEmpty()) mixpanel.registerSuperPropertiesOnceMap(utm)
    }

    private companion object {
        const val UTM_SOURCE = "utm_source"
    }
}
