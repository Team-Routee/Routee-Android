package com.routee.android.core.data.di

import com.routee.android.core.data.timeline.remote.service.TimelinePhotoService
import com.routee.android.core.data.timeline.repository.TimelinePhotoRepository
import com.routee.android.core.data.timeline.repository.TimelinePhotoRepositoryImpl
import com.routee.android.core.network.di.Auth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TimelinePhotoModule {
    @Binds
    @Singleton
    abstract fun bindTimelinePhotoRepository(impl: TimelinePhotoRepositoryImpl): TimelinePhotoRepository

    companion object {
        @Provides
        @Singleton
        fun provideTimelinePhotoService(@Auth retrofit: Retrofit): TimelinePhotoService = retrofit.create()
    }
}
