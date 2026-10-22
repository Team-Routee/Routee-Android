package com.routee.android.core.data.di

import com.routee.android.core.data.workout.repository.WorkoutRecordRepository
import com.routee.android.core.data.workout.repository.WorkoutRecordRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class WorkoutModule {
    @Binds
    @Singleton
    abstract fun bindWorkoutRecordRepository(impl: WorkoutRecordRepositoryImpl): WorkoutRecordRepository
}
