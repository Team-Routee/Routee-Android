package com.routee.android.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class PresignedUpload

@Module
@InstallIn(SingletonComponent::class)
internal object PresignedUploadModule {
    @Provides
    @Singleton
    @PresignedUpload
    fun provideImageUploadClient(): OkHttpClient = OkHttpClient.Builder().build()
}
