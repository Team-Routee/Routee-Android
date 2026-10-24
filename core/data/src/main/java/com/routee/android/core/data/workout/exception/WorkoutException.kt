package com.routee.android.core.data.workout.exception

import com.routee.android.core.network.exception.ApiException

sealed class WorkoutException(message: String?) : Exception(message) {

    /** 서버에 기록이 없음(404), 다시 보내도 같음. 400은 앱을 고치면 보낼 수 있어서 넣지 않음 */
    class Rejected(message: String?) : WorkoutException(message)
}

internal fun mapWorkoutException(exception: ApiException): Throwable = when (exception) {
    is ApiException.NotFound -> WorkoutException.Rejected(exception.message)
    else -> exception
}
