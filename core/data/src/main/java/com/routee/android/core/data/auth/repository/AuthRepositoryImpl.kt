package com.routee.android.core.data.auth.repository

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.auth.exception.AuthException
import com.routee.android.core.data.auth.model.Agreements
import com.routee.android.core.data.auth.model.LoginResult
import com.routee.android.core.data.auth.model.SocialProvider
import com.routee.android.core.data.auth.remote.dto.request.AgreementsRequest
import com.routee.android.core.data.auth.remote.dto.request.LoginRequest
import com.routee.android.core.data.auth.remote.dto.request.RegisterRequest
import com.routee.android.core.data.auth.remote.service.AuthService
import com.routee.android.core.data.local.TokenLocalDataSource
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.network.extension.mapApiException
import com.routee.android.core.network.util.ApiResponseHandler
import javax.inject.Inject

internal class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val tokenLocalDataSource: TokenLocalDataSource,
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
    }.mapApiException(::mapAuthException)

    private fun mapAuthException(exception: ApiException): Throwable = when (exception.serverCode) {
        INVALID_ID_TOKEN, ID_TOKEN_EXPIRED, INVALID_TOKEN_CLAIMS -> AuthException.InvalidIdToken(exception.message)
        REQUIRED_AGREEMENT_NOT_ACCEPTED -> AuthException.RequiredAgreementNotAccepted(exception.message)
        ALREADY_REGISTERED_MEMBER -> AuthException.AlreadyRegistered(exception.message)
        else -> exception
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
