package com.routee.android.core.data.connectivity.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveConnectionValidationTest {
    @Test
    fun validatingThenValidatedDoesNotEmitOffline() = runTest {
        val states = MutableSharedFlow<NetworkState>()
        val connections = mutableListOf<Boolean>()
        backgroundScope.launch { states.validatedConnection().collect { connections.add(it) } }
        runCurrent()

        states.emit(NetworkState.VALIDATING)
        advanceTimeBy(1_000)
        assertEquals(emptyList<Boolean>(), connections)
        states.emit(NetworkState.VALIDATED)
        runCurrent()
        advanceTimeBy(2_000)
        runCurrent()

        assertEquals(listOf(true), connections)
    }

    @Test
    fun validationTimeoutEmitsOfflineAndCanRecover() = runTest {
        val states = MutableSharedFlow<NetworkState>()
        val connections = mutableListOf<Boolean>()
        backgroundScope.launch { states.validatedConnection().collect { connections.add(it) } }
        runCurrent()

        states.emit(NetworkState.VALIDATED)
        runCurrent()
        states.emit(NetworkState.VALIDATING)
        advanceTimeBy(1_999)
        assertEquals(listOf(true), connections)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(true, false), connections)
        states.emit(NetworkState.VALIDATED)
        runCurrent()

        assertEquals(listOf(true, false, true), connections)
    }

    @Test
    fun disconnectionCancelsValidationWaitImmediately() = runTest {
        val states = MutableSharedFlow<NetworkState>()
        val connections = mutableListOf<Boolean>()
        backgroundScope.launch { states.validatedConnection().collect { connections.add(it) } }
        runCurrent()

        states.emit(NetworkState.VALIDATING)
        advanceTimeBy(100)
        states.emit(NetworkState.DISCONNECTED)
        runCurrent()

        assertEquals(listOf(false), connections)
    }
}
