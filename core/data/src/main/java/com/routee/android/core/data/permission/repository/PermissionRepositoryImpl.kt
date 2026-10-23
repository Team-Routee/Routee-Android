package com.routee.android.core.data.permission.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.di.PermissionDataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.first

internal class PermissionRepositoryImpl @Inject constructor(
    @param:PermissionDataStore private val dataStore: DataStore<Preferences>,
) : PermissionRepository {
    override suspend fun getLocationPermissionRequested(): Result<Boolean> = suspendRunCatching {
        dataStore.data.first()[LOCATION_REQUESTED] ?: false
    }

    override suspend fun saveLocationPermissionRequested(): Result<Unit> = suspendRunCatching {
        dataStore.edit { it[LOCATION_REQUESTED] = true }
    }

    private companion object {
        val LOCATION_REQUESTED = booleanPreferencesKey("location_requested")
    }
}
