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
}
