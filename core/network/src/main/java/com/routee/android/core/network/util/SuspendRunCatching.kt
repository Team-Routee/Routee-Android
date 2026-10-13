package com.routee.android.core.network.util

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * suspend 블록을 실행하고 결과를 [Result]로 반환한다
 *
 * [runCatching]과 달리 [CancellationException]은 다시 던져 코루틴 취소가 전파되도록 한다
 * 블록 안의 [kotlinx.coroutines.withTimeout]이 시간 초과된 경우만 실패로 반환하고, 바깥 코루틴이 취소된 경우에는 취소를 전파한다
 */
suspend inline fun <R> suspendRunCatching(crossinline block: suspend () -> R): Result<R> =
    try {
        Result.success(block())
    } catch (t: TimeoutCancellationException) {
        currentCoroutineContext().ensureActive()
        Result.failure(t)
    } catch (c: CancellationException) {
        throw c
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        Result.failure(e)
    }
