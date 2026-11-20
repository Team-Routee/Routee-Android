package com.routee.android

import android.app.Application
import com.routee.android.core.analytics.AnalyticsInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class RouteeApplication : Application() {
    @Inject
    lateinit var analyticsInitializer: AnalyticsInitializer

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        Timber.plant(CrashlyticsTree())
        analyticsInitializer.initialize()
    }
}
