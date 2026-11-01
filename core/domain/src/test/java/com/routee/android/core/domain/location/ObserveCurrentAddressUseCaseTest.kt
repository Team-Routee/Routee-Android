package com.routee.android.core.domain.location

import com.routee.android.core.data.geocoding.repository.ReverseGeocodingRepository
import com.routee.android.core.data.location.model.DeviceLocation
import com.routee.android.core.data.location.repository.LocationRepository
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveCurrentAddressUseCaseTest {
    private val geocoding = FakeReverseGeocodingRepository()

    /**
     * GIVEN: 처음 위치, 20m 옮긴 위치, 120m 옮긴 위치가 차례로 올 때
     * WHEN: 현재 주소를 구독하면
     * THEN: 50m 안의 위치는 건너뛰고 두 번만 조회한다
     */
    @Test
    fun `50m 넘게 움직였을 때만 다시 조회한다`() = runTest {
        val useCase = useCase(location(0.0), location(0.00018), location(0.00108))

        val addresses = useCase().toList()

        assertEquals(listOf("주소 1", "주소 2"), addresses)
        assertEquals(2, geocoding.requestCount)
    }

    /**
     * GIVEN: 첫 조회가 실패할 때
     * WHEN: 20m 옮긴 위치가 오면
     * THEN: 실패를 알리고, 받은 주소가 없어서 다시 조회한다
     */
    @Test
    fun `실패하면 가까운 다음 위치에서 다시 조회한다`() = runTest {
        geocoding.failFirst = true
        val useCase = useCase(location(0.0), location(0.00018))

        val failures = mutableListOf<Throwable>()

        val addresses = useCase(onAddressFailure = { failures.add(it) }).toList()

        assertEquals(listOf("주소 2"), addresses)
        assertEquals(1, failures.size)
    }

    private fun useCase(vararg locations: DeviceLocation) =
        ObserveCurrentAddressUseCase(FakeLocationRepository(flowOf(*locations)), geocoding)

    // 위도 0.0009도 = 약 100m
    private fun location(latitudeOffset: Double) = DeviceLocation(
        latitude = 37.0 + latitudeOffset,
        longitude = 127.0,
        altitude = null,
        altitudeAccuracyMeters = null,
        accuracyMeters = 5f,
        time = Instant.EPOCH,
    )

    private class FakeLocationRepository(private val flow: Flow<DeviceLocation>) : LocationRepository {
        override fun observeLocations(): Flow<DeviceLocation> = flow

        override fun observeLocationEnabled(): Flow<Boolean> = flowOf(true)

        override suspend fun getLastLocation(): Result<DeviceLocation?> = Result.success(null)
    }

    private class FakeReverseGeocodingRepository : ReverseGeocodingRepository {
        var requestCount = 0
        var failFirst = false

        override suspend fun getAddress(latitude: Double, longitude: Double): Result<String> {
            requestCount++
            if (failFirst && requestCount == 1) return Result.failure(IOException())
            return Result.success("주소 $requestCount")
        }
    }
}
