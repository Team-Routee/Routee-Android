package com.routee.android.core.data.geocoding.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ReverseGeocodeResponse(
    @SerialName("results") val results: List<ReverseGeocodeResult> = emptyList(),
)

@Serializable
internal data class ReverseGeocodeResult(
    @SerialName("name") val name: String,
    @SerialName("region") val region: ReverseGeocodeRegion,
    @SerialName("land") val land: ReverseGeocodeLand? = null,
)

@Serializable
internal data class ReverseGeocodeRegion(
    @SerialName("area2") val area2: ReverseGeocodeArea,
    @SerialName("area3") val area3: ReverseGeocodeArea,
    @SerialName("area4") val area4: ReverseGeocodeArea,
)

@Serializable
internal data class ReverseGeocodeArea(
    @SerialName("name") val name: String,
)

@Serializable
internal data class ReverseGeocodeLand(
    @SerialName("name") val name: String? = null,
    @SerialName("number1") val number1: String = "",
    @SerialName("number2") val number2: String = "",
)
