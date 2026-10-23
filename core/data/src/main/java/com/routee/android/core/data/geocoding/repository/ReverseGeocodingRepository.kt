package com.routee.android.core.data.geocoding.repository

/** 좌표를 주소로 바꾸는 저장소 (네이버 Reverse Geocoding) */
interface ReverseGeocodingRepository {
    /** 도로명 주소, 없으면 지번 주소 (시/도 제외). 둘 다 없으면 실패 */
    suspend fun getAddress(latitude: Double, longitude: Double): Result<String>
}
