package com.routee.android.core.data.auth.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import java.security.ProviderException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TokenLocalDataSourceTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    /**
     * GIVEN: 토큰을 저장했을 때
     * WHEN: DataStore에 저장된 값을 직접 읽으면
     * THEN: 원문이 아닌 암호화된 값이다
     */
    @Test
    fun `토큰은 암호화되어 저장된다`() = runTest {
        val dataStore = dataStore()
        val tokenStore = TokenLocalDataSource(dataStore, FakeTokenCipher())

        tokenStore.saveTokens(accessToken = "access", refreshToken = "refresh")

        val prefs = dataStore.data.first()
        assertNotEquals("access", prefs[ACCESS_TOKEN])
        assertNotEquals("refresh", prefs[REFRESH_TOKEN])
    }

    /**
     * GIVEN: 토큰을 저장했을 때
     * WHEN: 토큰을 읽으면
     * THEN: 복호화된 원문을 반환한다
     */
    @Test
    fun `저장한 토큰을 원문으로 읽는다`() = runTest {
        val tokenStore = TokenLocalDataSource(dataStore(), FakeTokenCipher())

        tokenStore.saveTokens(accessToken = "access", refreshToken = "refresh")

        assertEquals("access", tokenStore.getAccessToken())
        assertEquals("refresh", tokenStore.getRefreshToken())
    }

    /**
     * GIVEN: 복호화할 수 없는 값이 저장되어 있을 때 (암호화 전 버전의 원문, 키 분실)
     * WHEN: 토큰을 읽으면
     * THEN: null을 반환한다
     */
    @Test
    fun `복호화할 수 없는 값은 null로 읽는다`() = runTest {
        val dataStore = dataStore()
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = "plain-access"
            prefs[REFRESH_TOKEN] = "plain-refresh"
        }
        val tokenStore = TokenLocalDataSource(dataStore, FakeTokenCipher())

        assertNull(tokenStore.getAccessToken())
        assertNull(tokenStore.getRefreshToken())
    }

    /**
     * GIVEN: Keystore가 RuntimeException(BackendBusyException 등)을 던질 때
     * WHEN: 토큰을 읽으면
     * THEN: 앱이 죽지 않고 null을 반환한다
     */
    @Test
    fun `Keystore 런타임 예외도 null로 읽는다`() = runTest {
        val dataStore = dataStore()
        TokenLocalDataSource(dataStore, FakeTokenCipher()).saveTokens(accessToken = "access", refreshToken = "refresh")
        val tokenStore = TokenLocalDataSource(dataStore, FakeTokenCipher(decryptException = ProviderException()))

        assertNull(tokenStore.getAccessToken())
        assertNull(tokenStore.getRefreshToken())
    }

    private fun TestScope.dataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = backgroundScope,
    ) {
        File(temporaryFolder.newFolder(), "token.preferences_pb")
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
}
