package com.routee.android.core.data.workout.exception

import com.routee.android.core.network.exception.ApiException

sealed class WorkoutException(message: String?) : Exception(message) {

    /** 서버에 기록이 없거나(404) 보낸 값이 잘못됨(400), 다시 보내도 같음 */
    class Rejected(message: String?) : WorkoutException(message)
}

internal fun mapWorkoutException(exception: ApiException): Throwable = when (exception) {
    is ApiException.NotFound, is ApiException.BadRequest -> WorkoutException.Rejected(exception.message)
    else -> exception
}
