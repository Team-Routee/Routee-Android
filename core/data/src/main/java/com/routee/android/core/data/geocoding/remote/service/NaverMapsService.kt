package com.routee.android.core.data.geocoding.remote.service

import com.routee.android.core.data.geocoding.remote.dto.response.ReverseGeocodeResponse
import retrofit2.http.GET
import retrofit2.http.Query

// 네이버 지도 Open API (NCP Maps)
internal interface NaverMapsService {
    @GET("map-reversegeocode/v2/gc")
    suspend fun reverseGeocode(
        @Query("coords") coords: String,
        @Query("orders") orders: String = "roadaddr,addr",
        @Query("output") output: String = "json",
    ): ReverseGeocodeResponse
}
