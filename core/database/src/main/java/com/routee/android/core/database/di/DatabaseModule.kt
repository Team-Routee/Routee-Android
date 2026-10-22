package com.routee.android.core.database.di

import android.content.Context
import androidx.room.Room
import com.routee.android.core.database.RouteeDatabase
import com.routee.android.core.database.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    private const val DATABASE_NAME = "routee.db"

    @Provides
    @Singleton
    fun provideRouteeDatabase(
        @ApplicationContext context: Context,
    ): RouteeDatabase = Room.databaseBuilder(context, RouteeDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideWorkoutDao(database: RouteeDatabase): WorkoutDao = database.workoutDao()
}
