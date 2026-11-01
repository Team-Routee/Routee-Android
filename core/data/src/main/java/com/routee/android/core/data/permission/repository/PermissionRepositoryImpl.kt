package com.routee.android.core.data.permission.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.di.PermissionDataStore
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.coroutines.flow.first

internal class PermissionRepositoryImpl @Inject constructor(
    @param:PermissionDataStore private val dataStore: DataStore<Preferences>,
) : PermissionRepository {
    private val isLocationGuideShown = AtomicBoolean(false)

    override suspend fun getLocationPermissionDenied(): Result<Boolean> = suspendRunCatching {
        dataStore.data.first()[LOCATION_DENIED] ?: false
    }

    override suspend fun saveLocationPermissionDenied(): Result<Unit> = suspendRunCatching {
        dataStore.edit { it[LOCATION_DENIED] = true }
    }

    override fun consumeLocationGuideOnLaunch(): Boolean = isLocationGuideShown.compareAndSet(false, true)

    private companion object {
        val LOCATION_DENIED = booleanPreferencesKey("location_denied")
    }
}
