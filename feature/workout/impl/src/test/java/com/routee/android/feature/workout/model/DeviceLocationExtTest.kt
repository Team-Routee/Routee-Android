package com.routee.android.feature.workout.model

import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.feature.workout.FakeLocationRepository
import com.routee.android.feature.workout.START
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceLocationExtTest {
    /**
     * GIVEN: 기기에 마지막 위치가 있을 때
     * WHEN: 지도 내 위치를 구독하면
     * THEN: 마지막 위치를 먼저 보내고 이어서 새 위치를 보낸다
     */
    @Test
    fun `마지막 위치를 먼저 보낸다`() = runTest {
        val repository = FakeLocationRepository(locations = flowOf(location(37.1)), lastLocation = location(37.0))

        assertEquals(listOf(location(37.0), location(37.1)), repository.observeLocationsFromLast().toList())
    }

    /**
     * GIVEN: 기기에 마지막 위치가 없을 때
     * WHEN: 지도 내 위치를 구독하면
     * THEN: 새 위치만 보낸다
     */
    @Test
    fun `마지막 위치가 없으면 새 위치만 보낸다`() = runTest {
        val repository = FakeLocationRepository(locations = flowOf(location(37.1)))

        assertEquals(listOf(location(37.1)), repository.observeLocationsFromLast().toList())
    }

    private fun location(latitude: Double) = DeviceLocation(
        latitude = latitude,
        longitude = 127.0,
        altitude = null,
        altitudeAccuracyMeters = null,
        accuracyMeters = 5f,
        time = START,
    )
}
