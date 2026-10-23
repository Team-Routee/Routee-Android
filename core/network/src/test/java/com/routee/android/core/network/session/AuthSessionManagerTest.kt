package com.routee.android.core.network.session

import com.routee.android.core.network.token.TokenProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthSessionManagerTest {
    /**
     * GIVEN: 저장된 Access Token이 있을 때
     * WHEN: 세션을 확인하면
     * THEN: 세션이 있다
     */
    @Test
    fun `토큰이 있으면 세션이 있다`() = runTest {
        assertTrue(AuthSessionManager(FakeTokenProvider(accessToken = "access")).hasSession())
    }

    /**
     * GIVEN: 앱 데이터 삭제나 새로 설치해서 토큰이 없을 때
     * WHEN: 세션을 확인하면
     * THEN: 세션이 없다
     */
    @Test
    fun `토큰이 없으면 세션이 없다`() = runTest {
        assertFalse(AuthSessionManager(FakeTokenProvider(accessToken = null)).hasSession())
    }

    private class FakeTokenProvider(private val accessToken: String?) : TokenProvider {
        override suspend fun getAccessToken(): String? = accessToken

        override suspend fun getRefreshToken(): String? = null

        override suspend fun saveTokens(
            accessToken: String,
            refreshToken: String,
        ) = Unit

        override suspend fun clearTokens() = Unit
    }
}
