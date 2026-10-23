package com.routee.android.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class TokenDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class PermissionDataStore

@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreModule {
    private const val TOKEN_DATASTORE_NAME = "routee_token"
    private const val PERMISSION_DATASTORE_NAME = "routee_permission"

    @TokenDataStore
    @Provides
    @Singleton
    fun provideTokenDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(TOKEN_DATASTORE_NAME) },
    )

    @PermissionDataStore
    @Provides
    @Singleton
    fun providePermissionDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(PERMISSION_DATASTORE_NAME) },
    )
}
