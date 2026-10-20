package com.routee.android.core.data.di

import com.routee.android.core.data.timeline.remote.service.TimelineCompletionService
import com.routee.android.core.data.timeline.repository.TimelineCompletionRepository
import com.routee.android.core.data.timeline.repository.TimelineCompletionRepositoryImpl
import com.routee.android.core.network.di.Auth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TimelineCompletionModule {
    @Binds
    abstract fun bindTimelineCompletionRepository(impl: TimelineCompletionRepositoryImpl): TimelineCompletionRepository

    companion object {
        @Provides
        fun provideTimelineCompletionService(@Auth retrofit: Retrofit): TimelineCompletionService = retrofit.create()
    }
}
