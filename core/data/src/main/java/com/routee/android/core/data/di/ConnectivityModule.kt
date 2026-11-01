package com.routee.android.core.data.di

import com.routee.android.core.data.connectivity.repository.ConnectivityRepository
import com.routee.android.core.data.connectivity.repository.ConnectivityRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ConnectivityModule {
    @Binds
    abstract fun bindConnectivityRepository(impl: ConnectivityRepositoryImpl): ConnectivityRepository
}
