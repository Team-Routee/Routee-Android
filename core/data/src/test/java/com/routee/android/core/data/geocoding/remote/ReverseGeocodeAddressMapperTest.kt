package com.routee.android.core.data.geocoding.remote

import com.routee.android.core.data.geocoding.remote.dto.response.ReverseGeocodeArea
import com.routee.android.core.data.geocoding.remote.dto.response.ReverseGeocodeLand
import com.routee.android.core.data.geocoding.remote.dto.response.ReverseGeocodeRegion
import com.routee.android.core.data.geocoding.remote.dto.response.ReverseGeocodeResponse
import com.routee.android.core.data.geocoding.remote.dto.response.ReverseGeocodeResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReverseGeocodeAddressMapperTest {
    /**
     * GIVEN: 도로명, 지번 결과가 둘 다 있을 때
     * WHEN: 주소로 바꾸면
     * THEN: 시/도와 동을 뺀 도로명 주소를 쓴다
     */
    @Test
    fun `도로명 주소를 먼저 쓴다`() {
        val response = ReverseGeocodeResponse(
            results = listOf(
                result(name = "addr", area3 = "여의도동", land = ReverseGeocodeLand(number1 = "1")),
                result(name = "roadaddr", area3 = "여의도동", land = ReverseGeocodeLand(name = "의사당대로", number1 = "1")),
            ),
        )

        assertEquals("영등포구 의사당대로 1", response.toAddress())
    }

    /**
     * GIVEN: 산처럼 도로명 결과가 없을 때
     * WHEN: 주소로 바꾸면
     * THEN: 구, 읍면동, 리와 번지를 쓴다
     */
    @Test
    fun `도로명이 없으면 지번 주소를 쓴다`() {
        val response = ReverseGeocodeResponse(
            results = listOf(
                result(
                    name = "addr",
                    area2 = "양평군",
                    area3 = "옥천면",
                    area4 = "용천리",
                    land = ReverseGeocodeLand(number1 = "123", number2 = "4"),
                ),
            ),
        )

        assertEquals("양평군 옥천면 용천리 123-4", response.toAddress())
    }

    /**
     * GIVEN: 바다처럼 결과가 없을 때
     * WHEN: 주소로 바꾸면
     * THEN: null
     */
    @Test
    fun `결과가 없으면 null`() {
        assertNull(ReverseGeocodeResponse().toAddress())
    }

    private fun result(
        name: String,
        area2: String = "영등포구",
        area3: String = "",
        area4: String = "",
        land: ReverseGeocodeLand? = null,
    ) = ReverseGeocodeResult(
        name = name,
        region = ReverseGeocodeRegion(
            area2 = ReverseGeocodeArea(area2),
            area3 = ReverseGeocodeArea(area3),
            area4 = ReverseGeocodeArea(area4),
        ),
        land = land,
    )
}
