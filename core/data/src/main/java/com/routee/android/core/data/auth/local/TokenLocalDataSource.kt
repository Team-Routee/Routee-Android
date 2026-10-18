package com.routee.android.core.data.auth.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.routee.android.core.data.di.TokenDataStore
import com.routee.android.core.network.token.TokenProvider
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@Singleton
internal class TokenLocalDataSource @Inject constructor(
    @param:TokenDataStore private val dataStore: DataStore<Preferences>,
    private val tokenCipher: TokenCipher,
) : TokenProvider {
    // 요청마다 Keystore 복호화를 하지 않도록 처음 읽은 값을 둔다. null 이면 아직 읽지 않음
    private val cachedTokens = AtomicReference<Tokens?>(null)

    override suspend fun getAccessToken(): String? = tokens().accessToken

    override suspend fun getRefreshToken(): String? = tokens().refreshToken

    override suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = tokenCipher.encrypt(accessToken)
            prefs[REFRESH_TOKEN] = tokenCipher.encrypt(refreshToken)
        }
        cachedTokens.set(Tokens(accessToken = accessToken, refreshToken = refreshToken))
    }

    override suspend fun clearTokens() {
        dataStore.edit { it.clear() }
        cachedTokens.set(Tokens(accessToken = null, refreshToken = null))
    }

    private suspend fun tokens(): Tokens {
        cachedTokens.get()?.let { return it }
        // 읽는 사이 저장, 삭제된 값이 있으면 그 값을 덮어쓰지 않는다
        cachedTokens.compareAndSet(null, readTokens())
        return checkNotNull(cachedTokens.get())
    }

    private suspend fun readTokens(): Tokens {
        val prefs = dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .first()
        return withContext(Dispatchers.IO) {
            Tokens(
                accessToken = prefs[ACCESS_TOKEN]?.let(::decryptOrNull),
                refreshToken = prefs[REFRESH_TOKEN]?.let(::decryptOrNull),
            )
        }
    }

    // Keystore는 BackendBusyException 같은 RuntimeException도 던진다. 어떤 이유든 못 풀면 null (재로그인)
    private fun decryptOrNull(encrypted: String): String? = try {
        tokenCipher.decrypt(encrypted)
    } catch (e: Exception) {
        null
    }

    private data class Tokens(
        val accessToken: String?,
        val refreshToken: String?,
    )

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
}
