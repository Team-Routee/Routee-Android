package com.routee.android.core.network.util

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SuspendRunCatchingTest {

    /**
     * GIVEN: 블록 안의 withTimeout이 시간 초과될 때
     * WHEN: suspendRunCatching으로 실행하면
     * THEN: TimeoutCancellationException을 실패로 반환한다
     */
    @Test
    fun `블록 안의 타임아웃은 실패로 반환한다`() = runTest {
        val result = suspendRunCatching {
            withTimeout(100) { awaitCancellation() }
        }

        assertTrue(result.exceptionOrNull() is TimeoutCancellationException)
    }

    /**
     * GIVEN: 바깥 withTimeout이 시간 초과될 때
     * WHEN: 그 안에서 suspendRunCatching이 실행 중이면
     * THEN: 실패로 삼키지 않고 바깥으로 취소를 전파한다
     */
    @Test
    fun `바깥 코루틴의 타임아웃은 취소로 전파한다`() = runTest {
        var continuedAfterTimeout = false

        try {
            withTimeout(100) {
                suspendRunCatching { awaitCancellation() }
                continuedAfterTimeout = true
            }
            fail("바깥 타임아웃이 전파되어야 한다")
        } catch (e: TimeoutCancellationException) {
            assertTrue(!continuedAfterTimeout)
        }
    }
}
