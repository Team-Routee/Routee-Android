package com.routee.android.core.data.auth.repository

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.auth.exception.AuthException
import com.routee.android.core.data.auth.local.TokenLocalDataSource
import com.routee.android.core.data.auth.model.Agreements
import com.routee.android.core.data.auth.model.LoginResult
import com.routee.android.core.data.auth.model.SocialProvider
import com.routee.android.core.data.auth.remote.dto.request.AgreementsRequest
import com.routee.android.core.data.auth.remote.dto.request.LoginRequest
import com.routee.android.core.data.auth.remote.dto.request.LogoutRequest
import com.routee.android.core.data.auth.remote.dto.request.RegisterRequest
import com.routee.android.core.data.auth.remote.dto.request.WithdrawRequest
import com.routee.android.core.data.auth.remote.service.AuthService
import com.routee.android.core.data.auth.remote.service.LogoutService
import com.routee.android.core.data.auth.remote.service.WithdrawService
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.network.extension.mapApiException
import com.routee.android.core.network.session.AuthSessionManager
import com.routee.android.core.network.util.ApiResponseHandler
import javax.inject.Inject

internal class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val logoutService: LogoutService,
    private val withdrawService: WithdrawService,
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val authSessionManager: AuthSessionManager,
    private val apiResponseHandler: ApiResponseHandler,
) : AuthRepository {

    override suspend fun login(
        idToken: String,
        provider: SocialProvider,
    ): Result<LoginResult> = apiResponseHandler.safeApiCall {
        authService.login(LoginRequest(idToken = idToken, provider = provider.name))
    }.fold(
        onSuccess = { token ->
            suspendRunCatching {
                tokenLocalDataSource.saveTokens(accessToken = token.accessToken, refreshToken = token.refreshToken)
                LoginResult.Success
            }
        },
        onFailure = { throwable ->
            if (throwable is ApiException.NotFound && throwable.serverCode == MEMBER_NOT_FOUND) {
                Result.success(LoginResult.NeedRegister(idToken = idToken, provider = provider))
            } else {
                Result.failure(throwable)
            }
        },
    ).mapApiException(::mapAuthException)

    override suspend fun register(
        nickname: String,
        idToken: String,
        provider: SocialProvider,
        agreements: Agreements,
    ): Result<Unit> = apiResponseHandler.safeUnitApiCall {
        authService.register(
            RegisterRequest(
                nickname = nickname,
                idToken = idToken,
                provider = provider.name,
                agreements = agreements.toRequest(),
            ),
        )
    }.fold(
        onSuccess = {
            executeLogin(idToken = idToken, provider = provider)
        },
        onFailure = { throwable ->
            if (throwable is ApiException && throwable.serverCode == ALREADY_REGISTERED_MEMBER) {
                executeLogin(idToken = idToken, provider = provider)
            } else {
                Result.failure(throwable)
            }
        },
    ).mapApiException(::mapAuthException)

    override suspend fun logout(): Result<Unit> {
        tokenLocalDataSource.getRefreshToken()?.let { refreshToken ->
            apiResponseHandler.safeUnitApiCall { logoutService.logout(LogoutRequest(refreshToken = refreshToken)) }
        }
        // TODO 기기 운동 기록 삭제
        return suspendRunCatching { authSessionManager.clearSession() }
    }

    override suspend fun withdraw(): Result<Unit> {
        val refreshToken = suspendRunCatching { tokenLocalDataSource.getRefreshToken() }
            .getOrElse { return Result.failure(it) }
            ?: return Result.failure(IllegalStateException("Refresh token is missing"))
        return apiResponseHandler.safeUnitApiCall {
            withdrawService.withdraw(WithdrawRequest(refreshToken = refreshToken))
        }.mapApiException(::mapAuthException)
            .fold(
                onSuccess = {
                    // TODO 기기 운동 기록 삭제
                    suspendRunCatching { authSessionManager.clearSession() }
                },
                onFailure = { Result.failure(it) },
            )
    }

    private suspend fun executeLogin(
        idToken: String,
        provider: SocialProvider,
    ): Result<Unit> = apiResponseHandler.safeApiCall {
        authService.login(LoginRequest(idToken = idToken, provider = provider.name))
    }.fold(
        onSuccess = { token ->
            suspendRunCatching {
                tokenLocalDataSource.saveTokens(accessToken = token.accessToken, refreshToken = token.refreshToken)
            }
        },
        onFailure = { throwable ->
            Result.failure(throwable)
        },
    )

    private fun mapAuthException(exception: ApiException): Throwable {
        if (exception is ApiException.NetworkConnection) return AuthException.NetworkUnavailable(exception.message)
        return when (exception.serverCode) {
            INVALID_ID_TOKEN, ID_TOKEN_EXPIRED, INVALID_TOKEN_CLAIMS -> AuthException.InvalidIdToken(exception.message)
            REQUIRED_AGREEMENT_NOT_ACCEPTED -> AuthException.RequiredAgreementNotAccepted(exception.message)
            ALREADY_REGISTERED_MEMBER -> AuthException.AlreadyRegistered(exception.message)
            else -> exception
        }
    }

    private fun Agreements.toRequest() = AgreementsRequest(
        serviceTerms = serviceTerms,
        privacyPolicy = privacyPolicy,
        locationServiceTerms = locationServiceTerms,
        over14 = over14,
        marketingConsent = marketingConsent,
    )

    private companion object {
        const val MEMBER_NOT_FOUND = "MEMBER_NOT_FOUND"
        const val INVALID_ID_TOKEN = "INVALID_ID_TOKEN"
        const val ID_TOKEN_EXPIRED = "ID_TOKEN_EXPIRED"
        const val INVALID_TOKEN_CLAIMS = "INVALID_TOKEN_CLAIMS"
        const val REQUIRED_AGREEMENT_NOT_ACCEPTED = "REQUIRED_AGREEMENT_NOT_ACCEPTED"
        const val ALREADY_REGISTERED_MEMBER = "ALREADY_REGISTERED_MEMBER"
    }
}
