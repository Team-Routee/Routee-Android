package com.routee.android.core.data.di

import com.routee.android.core.data.timeline.remote.service.TimelineMapImageService
import com.routee.android.core.data.timeline.repository.TimelineMapImageRepository
import com.routee.android.core.data.timeline.repository.TimelineMapImageRepositoryImpl
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
internal abstract class TimelineMapImageModule {
    @Binds
    abstract fun bindTimelineMapImageRepository(impl: TimelineMapImageRepositoryImpl): TimelineMapImageRepository

    companion object {
        @Provides
        fun provideTimelineMapImageService(@Auth retrofit: Retrofit): TimelineMapImageService = retrofit.create()
    }
}
