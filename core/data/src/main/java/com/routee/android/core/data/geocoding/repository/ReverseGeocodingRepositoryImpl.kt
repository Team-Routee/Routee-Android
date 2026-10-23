package com.routee.android.core.data.geocoding.repository

import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.geocoding.remote.service.NaverMapsService
import com.routee.android.core.data.geocoding.remote.toAddress
import javax.inject.Inject

internal class ReverseGeocodingRepositoryImpl @Inject constructor(
    private val naverMapsService: NaverMapsService,
) : ReverseGeocodingRepository {
    override suspend fun getAddress(latitude: Double, longitude: Double): Result<String> = suspendRunCatching {
        val response = naverMapsService.reverseGeocode(coords = "$longitude,$latitude")
        checkNotNull(response.toAddress()) { "주소 결과 없음" }
    }
}
