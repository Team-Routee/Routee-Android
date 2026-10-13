package com.routee.android.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.routee.android.core.data.di.TokenDataStore
import com.routee.android.core.network.token.TokenProvider
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
internal class TokenLocalDataSource @Inject constructor(
    @param:TokenDataStore private val dataStore: DataStore<Preferences>,
) : TokenProvider {

    override suspend fun getAccessToken(): String? = read(ACCESS_TOKEN)

    suspend fun getRefreshToken(): String? = read(REFRESH_TOKEN)

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearTokens() {
        dataStore.edit { it.clear() }
    }

    private suspend fun read(key: Preferences.Key<String>): String? = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs -> prefs[key] }
        .first()

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
}
