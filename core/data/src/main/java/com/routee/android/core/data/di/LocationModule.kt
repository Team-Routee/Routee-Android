package com.routee.android.core.data.di

import com.routee.android.core.data.location.local.FusedLocationDataSource
import com.routee.android.core.data.location.local.LocationDataSource
import com.routee.android.core.data.location.repository.LocationRepository
import com.routee.android.core.data.location.repository.LocationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LocationModule {
    @Binds
    abstract fun bindLocationDataSource(impl: FusedLocationDataSource): LocationDataSource

    @Binds
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository
}
