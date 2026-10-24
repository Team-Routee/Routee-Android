package com.routee.android.core.data.geocoding.remote

import com.routee.android.core.data.geocoding.remote.dto.response.ReverseGeocodeLand
import com.routee.android.core.data.geocoding.remote.dto.response.ReverseGeocodeResponse
import com.routee.android.core.data.geocoding.remote.dto.response.ReverseGeocodeResult

private const val ROAD_ADDRESS = "roadaddr"
private const val LOT_ADDRESS = "addr"

// TODO P1 기획: 칩에 들어가게 시/도는 빼고 짧게 (iOS는 시/도부터 전부). 도로명 "영등포구 의사당대로 1", 지번 "양평군 옥천면 용천리 123-4"
internal fun ReverseGeocodeResponse.toAddress(): String? =
    results.firstOrNull { it.name == ROAD_ADDRESS }?.roadAddress()
        ?: results.firstOrNull { it.name == LOT_ADDRESS }?.lotAddress()

private fun ReverseGeocodeResult.roadAddress(): String? {
    val land = land ?: return null
    return listOf(region.area2.name, land.name.orEmpty(), land.number()).joinNotBlank()
}

private fun ReverseGeocodeResult.lotAddress(): String? =
    listOf(region.area2.name, region.area3.name, region.area4.name, land?.number().orEmpty()).joinNotBlank()

private fun ReverseGeocodeLand.number(): String = listOf(number1, number2).filter(String::isNotBlank).joinToString("-")

private fun List<String>.joinNotBlank(): String? = filter(
    String::isNotBlank,
).joinToString(" ").takeIf(String::isNotBlank)
