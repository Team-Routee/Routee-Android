package com.routee.android.core.data.auth.local

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
internal class TokenLocalDataSource @Inject constructor(
    @param:TokenDataStore private val dataStore: DataStore<Preferences>,
    private val tokenCipher: TokenCipher,
) : TokenProvider {

    override suspend fun getAccessToken(): String? = read(ACCESS_TOKEN)

    override suspend fun getRefreshToken(): String? = read(REFRESH_TOKEN)

    override suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = tokenCipher.encrypt(accessToken)
            prefs[REFRESH_TOKEN] = tokenCipher.encrypt(refreshToken)
        }
    }

    override suspend fun clearTokens() {
        dataStore.edit { it.clear() }
    }

    private suspend fun read(key: Preferences.Key<String>): String? = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs -> prefs[key] }
        .first()
        ?.let { encrypted -> withContext(Dispatchers.IO) { decryptOrNull(encrypted) } }

    // Keystore는 BackendBusyException 같은 RuntimeException도 던진다. 어떤 이유든 못 풀면 null (재로그인)
    private fun decryptOrNull(encrypted: String): String? = try {
        tokenCipher.decrypt(encrypted)
    } catch (e: Exception) {
        null
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
}
