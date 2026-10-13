package com.routee.android.core.network.extension

import com.routee.android.core.network.exception.ApiException

/**
 * 실패한 [Result]가 [ApiException]이면 [transform]으로 API별 예외로 다시 매핑한다
 *
 * Repository에서 serverCode에 따라 분기가 필요할 때 사용하며, [ApiException]이 아닌 예외는 그대로 전파한다
 */
inline fun <T> Result<T>.mapApiException(
    transform: (ApiException) -> Throwable,
): Result<T> = recoverCatching { throwable ->
    throw if (throwable is ApiException) transform(throwable) else throwable
}
