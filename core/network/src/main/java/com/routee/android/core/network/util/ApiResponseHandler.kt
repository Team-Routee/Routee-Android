package com.routee.android.core.network.util

import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.network.model.BaseResponse
import com.routee.android.core.network.model.ErrorResponse
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import timber.log.Timber

/**
 * API 호출 결과를 [Result]로 감싸고, 실패 시 예외를 [ApiException]으로 변환한다
 *
 * Repository는 이 핸들러로 API를 호출해, 실패값이 항상 [ApiException]임을 보장받는다
 */
@Singleton
class ApiResponseHandler @Inject constructor(
    private val json: Json,
) {

    /**
     * [BaseResponse]를 반환하는 API를 호출하고 [BaseResponse.data]만 꺼내 반환한다
     *
     * 성공 응답의 data가 null이면 [ApiException.EmptyData]로 실패한다
     */
    suspend fun <T> safeApiCall(block: suspend () -> BaseResponse<T>): Result<T> =
        suspendRunCatching {
            block().data ?: throw ApiException.EmptyData()
        }.recoverCatching { throwable ->
            throw mapThrowable(throwable)
        }

    /**
     * 응답 데이터를 사용하지 않는 API를 호출한다
     */
    suspend fun safeUnitApiCall(block: suspend () -> Unit): Result<Unit> =
        suspendRunCatching {
            block()
        }.recoverCatching { throwable ->
            throw mapThrowable(throwable)
        }

    private fun mapThrowable(throwable: Throwable): ApiException {
        val apiException = when (throwable) {
            is ApiException -> throwable
            is HttpException -> parseHttpException(throwable)
            is IOException -> ApiException.NetworkConnection(throwable)
            else -> ApiException.Unknown(throwable)
        }
        Timber.w(throwable, "API 호출 실패: ${apiException::class.simpleName} (${apiException.serverCode})")
        return apiException
    }

    private fun parseHttpException(exception: HttpException): ApiException {
        val errorResponse = exception.response()?.errorBody()?.string()?.let { errorBody ->
            runCatching { json.decodeFromString<ErrorResponse>(errorBody) }.getOrNull()
        }
        val code = errorResponse?.code
        val message = errorResponse?.message

        return when (val statusCode = exception.code()) {
            400 -> ApiException.BadRequest(code, message)
            401 -> ApiException.Unauthorized(code, message)
            403 -> ApiException.Forbidden(code, message)
            404 -> ApiException.NotFound(code, message)
            409 -> ApiException.Conflict(code, message)
            in 500..599 -> ApiException.ServerError(statusCode, code, message)
            else -> ApiException.Http(statusCode, code, message)
        }
    }
}
