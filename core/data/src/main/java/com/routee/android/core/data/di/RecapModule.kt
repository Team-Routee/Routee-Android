package com.routee.android.core.data.di

import com.routee.android.core.data.recap.remote.service.RecapService
import com.routee.android.core.data.recap.repository.RecapRepository
import com.routee.android.core.data.recap.repository.RecapRepositoryImpl
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
internal abstract class RecapModule {
    @Binds
    @Singleton
    abstract fun bindRecapRepository(recapRepositoryImpl: RecapRepositoryImpl): RecapRepository

    companion object {
        @Provides
        @Singleton
        fun provideRecapService(
            @Auth retrofit: Retrofit,
        ): RecapService = retrofit.create()
    }
}
