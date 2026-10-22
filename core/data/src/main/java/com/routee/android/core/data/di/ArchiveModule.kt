package com.routee.android.core.data.di

import com.routee.android.core.data.archive.remote.service.ArchiveService
import com.routee.android.core.data.archive.repository.ArchiveRepository
import com.routee.android.core.data.archive.repository.ArchiveRepositoryImpl
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
internal abstract class ArchiveModule {
    @Binds
    @Singleton
    abstract fun bindArchiveRepository(impl: ArchiveRepositoryImpl): ArchiveRepository

    companion object {
        @Provides
        @Singleton
        fun provideArchiveService(@Auth retrofit: Retrofit): ArchiveService = retrofit.create()
    }
}
