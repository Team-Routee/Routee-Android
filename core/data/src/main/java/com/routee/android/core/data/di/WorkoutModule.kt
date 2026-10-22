package com.routee.android.core.data.di

import com.routee.android.core.data.workout.repository.WorkoutRecordRepository
import com.routee.android.core.data.workout.repository.WorkoutRecordRepositoryImpl
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepository
import com.routee.android.core.data.workout.repository.WorkoutTrackingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class WorkoutModule {
    @Binds
    @Singleton
    abstract fun bindWorkoutRecordRepository(impl: WorkoutRecordRepositoryImpl): WorkoutRecordRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutTrackingRepository(impl: WorkoutTrackingRepositoryImpl): WorkoutTrackingRepository

    companion object {
        @Provides
        fun provideClock(): Clock = Clock.systemUTC()
    }
}
