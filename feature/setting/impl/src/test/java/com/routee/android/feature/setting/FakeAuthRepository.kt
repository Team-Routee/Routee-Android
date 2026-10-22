package com.routee.android.feature.setting

import com.routee.android.core.data.auth.model.Agreements
import com.routee.android.core.data.auth.model.LoginResult
import com.routee.android.core.data.auth.model.SocialProvider
import com.routee.android.core.data.auth.repository.AuthRepository
import kotlinx.coroutines.CompletableDeferred

internal class FakeAuthRepository(
    private val logoutResult: Result<Unit> = Result.success(Unit),
    private val withdrawResult: Result<Unit> = Result.success(Unit),
) : AuthRepository {
    var logoutCount = 0
    var withdrawCount = 0
    var responseGate: CompletableDeferred<Unit>? = null

    override suspend fun login(
        idToken: String,
        provider: SocialProvider,
    ): Result<LoginResult> = error("login not expected")

    override suspend fun register(
        nickname: String,
        idToken: String,
        provider: SocialProvider,
        agreements: Agreements,
    ): Result<Unit> = error("register not expected")

    override suspend fun logout(): Result<Unit> {
        logoutCount++
        responseGate?.await()
        return logoutResult
    }

    override suspend fun withdraw(): Result<Unit> {
        withdrawCount++
        responseGate?.await()
        return withdrawResult
    }
}
