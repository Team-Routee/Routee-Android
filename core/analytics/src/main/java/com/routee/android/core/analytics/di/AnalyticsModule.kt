package com.routee.android.core.analytics.di

import com.routee.android.core.analytics.AnalyticsTracker
import com.routee.android.core.analytics.AnalyticsTrackerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AnalyticsModule {
    @Binds
    abstract fun bindAnalyticsTracker(tracker: AnalyticsTrackerImpl): AnalyticsTracker
}
