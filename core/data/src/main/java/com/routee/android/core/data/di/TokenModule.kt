package com.routee.android.core.data.di

import com.routee.android.core.data.auth.local.TokenLocalDataSource
import com.routee.android.core.network.token.TokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TokenModule {
    @Binds
    abstract fun bindTokenProvider(tokenLocalDataSource: TokenLocalDataSource): TokenProvider
}
