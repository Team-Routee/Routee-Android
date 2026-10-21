package com.routee.android.feature.recordedit.di

import com.routee.android.feature.recordedit.complete.RecapImageRepository
import com.routee.android.feature.recordedit.complete.RecapImageRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RecordEditModule {
    @Binds
    abstract fun bindRecapImageRepository(recapImageRepositoryImpl: RecapImageRepositoryImpl): RecapImageRepository
}
