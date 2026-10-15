package com.routee.android.core.data.di

import com.routee.android.core.data.auth.remote.service.AuthService
import com.routee.android.core.data.auth.repository.AuthRepository
import com.routee.android.core.data.auth.repository.AuthRepositoryImpl
import com.routee.android.core.network.di.NoAuth
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
internal abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    companion object {
        @Provides
        @Singleton
        fun provideAuthService(
            @NoAuth retrofit: Retrofit,
        ): AuthService = retrofit.create()
    }
}
