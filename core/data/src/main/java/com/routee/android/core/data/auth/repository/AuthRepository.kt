package com.routee.android.core.data.auth.repository

import com.routee.android.core.data.auth.model.Agreements
import com.routee.android.core.data.auth.model.LoginResult
import com.routee.android.core.data.auth.model.SocialProvider

interface AuthRepository {
    suspend fun login(
        idToken: String,
        provider: SocialProvider,
    ): Result<LoginResult>

    suspend fun register(
        nickname: String,
        idToken: String,
        provider: SocialProvider,
        agreements: Agreements,
    ): Result<Unit>

    /**
     * 로그아웃. 서버 요청이 실패해도 저장된 토큰은 지운다
     */
    suspend fun logout(): Result<Unit>
}
