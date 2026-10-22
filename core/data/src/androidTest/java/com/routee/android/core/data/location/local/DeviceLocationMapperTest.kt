package com.routee.android.core.data.location.local

import android.location.Location
import androidx.core.location.LocationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceLocationMapperTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * GIVEN: 서울에서 타원체 기준 고도 100m인 위치일 때
     * WHEN: 기기 위치로 바꾸면
     * THEN: 해수면 기준으로 바뀌어 지오이드 높이(약 15~35m)만큼 낮아진다
     */
    @Test
    fun ellipsoidAltitudeBecomesMeanSeaLevel() {
        val altitude = requireNotNull(location(altitude = 100.0).toDeviceLocation(context).altitude)

        val geoidHeight = 100.0 - altitude
        assertTrue("지오이드 높이 $geoidHeight", geoidHeight in 15.0..35.0)
    }

    /**
     * GIVEN: 이미 해수면 기준 고도가 있는 위치일 때
     * WHEN: 기기 위치로 바꾸면
     * THEN: 그 고도를 그대로 쓴다
     */
    @Test
    fun existingMeanSeaLevelAltitudeIsKept() {
        val location = location(altitude = 100.0).apply { LocationCompat.setMslAltitudeMeters(this, 80.0) }

        assertEquals(80.0, requireNotNull(location.toDeviceLocation(context).altitude), 0.0)
    }

    /**
     * GIVEN: 고도가 없는 위치일 때
     * WHEN: 기기 위치로 바꾸면
     * THEN: 고도와 고도 오차가 null이다
     */
    @Test
    fun missingAltitudeIsNull() {
        val deviceLocation = location(altitude = null).toDeviceLocation(context)

        assertNull(deviceLocation.altitude)
        assertNull(deviceLocation.altitudeAccuracyMeters)
    }

    private fun location(altitude: Double?) = Location("test").apply {
        latitude = SEOUL_LATITUDE
        longitude = SEOUL_LONGITUDE
        accuracy = 5f
        time = 1_000_000L
        if (altitude != null) {
            this.altitude = altitude
            verticalAccuracyMeters = 5f
        }
    }

    private companion object {
        const val SEOUL_LATITUDE = 37.5665
        const val SEOUL_LONGITUDE = 126.978
    }
}
