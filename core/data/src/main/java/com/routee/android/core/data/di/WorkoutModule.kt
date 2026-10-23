package com.routee.android.core.data.di

import com.routee.android.core.data.workout.remote.service.WorkoutService
import com.routee.android.core.data.workout.repository.WorkoutRecordRepository
import com.routee.android.core.data.workout.repository.WorkoutRecordRepositoryImpl
import com.routee.android.core.data.workout.repository.WorkoutRemoteRepository
import com.routee.android.core.data.workout.repository.WorkoutRemoteRepositoryImpl
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepositoryImpl
import com.routee.android.core.network.di.Auth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
internal abstract class WorkoutModule {
    @Binds
    @Singleton
    abstract fun bindWorkoutRecordRepository(impl: WorkoutRecordRepositoryImpl): WorkoutRecordRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutTrackingRepository(impl: WorkoutTrackingRepositoryImpl): WorkoutTrackingRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRemoteRepository(impl: WorkoutRemoteRepositoryImpl): WorkoutRemoteRepository

    companion object {
        @Provides
        fun provideClock(): Clock = Clock.systemUTC()

        @Provides
        @Singleton
        fun provideWorkoutService(
            @Auth retrofit: Retrofit,
        ): WorkoutService = retrofit.create()
    }
}
