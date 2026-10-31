package com.routee.android.core.data.connectivity.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AwaitNetworkValidationTest {
    @Test
    fun `연결이 없으면 기다리지 않고 false를 반환한다`() = runTest {
        assertFalse(awaitNetworkValidation { NetworkState.DISCONNECTED })
        assertEquals(0L, currentTime)
    }

    @Test
    fun `검증된 네트워크면 기다리지 않고 true를 반환한다`() = runTest {
        assertTrue(awaitNetworkValidation { NetworkState.VALIDATED })
        assertEquals(0L, currentTime)
    }

    @Test
    fun `검증 중에 검증이 끝나면 true를 반환한다`() = runTest {
        val states = ArrayDeque(listOf(NetworkState.VALIDATING, NetworkState.VALIDATING, NetworkState.VALIDATED))

        assertTrue(awaitNetworkValidation { states.removeFirst() })
    }

    @Test
    fun `검증 중에 연결이 끊기면 false를 반환한다`() = runTest {
        val states = ArrayDeque(listOf(NetworkState.VALIDATING, NetworkState.DISCONNECTED))

        assertFalse(awaitNetworkValidation { states.removeFirst() })
    }

    @Test
    fun `검증이 끝나지 않으면 2초 뒤 false를 반환한다`() = runTest {
        assertFalse(awaitNetworkValidation { NetworkState.VALIDATING })
        assertEquals(2_000L, currentTime)
    }
}
