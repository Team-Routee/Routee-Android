package com.routee.android.core.data.di

import com.routee.android.core.data.appnotice.repository.AppNoticeRepository
import com.routee.android.core.data.appnotice.repository.AppNoticeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AppNoticeModule {
    @Binds
    @Singleton
    abstract fun bindAppNoticeRepository(impl: AppNoticeRepositoryImpl): AppNoticeRepository
}
