package com.routee.android.core.network.exception

/**
 * API 호출 실패를 나타내는 예외
 *
 * [com.routee.android.core.network.util.ApiResponseHandler]를 거친 실패는 항상 이 타입이다
 *
 * @param serverCode 서버가 내려준 상세 코드 (예: INVALID_TOKEN). 서버 응답이 없으면 null
 */
sealed class ApiException(
    val serverCode: String?,
    message: String?,
    cause: Throwable? = null,
) : Exception(message, cause) {

    class BadRequest(serverCode: String?, message: String?) : ApiException(serverCode, message)

    class Unauthorized(serverCode: String?, message: String?) : ApiException(serverCode, message)

    class Forbidden(serverCode: String?, message: String?) : ApiException(serverCode, message)

    class NotFound(serverCode: String?, message: String?) : ApiException(serverCode, message)

    class Conflict(serverCode: String?, message: String?) : ApiException(serverCode, message)

    class ServerError(
        val statusCode: Int,
        serverCode: String?,
        message: String?,
    ) : ApiException(serverCode, message)

    class Http(
        val statusCode: Int,
        serverCode: String?,
        message: String?,
    ) : ApiException(serverCode, message)

    class NetworkConnection(cause: Throwable? = null) : ApiException(null, "Network connection failed", cause)

    class EmptyData : ApiException(null, "Response data is null")

    class Unknown(cause: Throwable? = null) : ApiException(null, "Unknown error", cause)
}
