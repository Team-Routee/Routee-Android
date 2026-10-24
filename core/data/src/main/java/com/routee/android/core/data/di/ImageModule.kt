package com.routee.android.core.data.di

import com.routee.android.core.data.image.repository.ImageCacheRepository
import com.routee.android.core.data.image.repository.ImageCacheRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ImageModule {
    @Binds
    abstract fun bindImageCacheRepository(impl: ImageCacheRepositoryImpl): ImageCacheRepository
}
