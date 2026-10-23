package com.routee.android.core.data.location.local

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.routee.android.core.data.location.model.DeviceLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/** 구글 위치 서비스(FusedLocationProvider)로 받는 기기 위치 */
internal class FusedLocationDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : LocationDataSource {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    /** 구독하는 동안 위치 업데이트를 받고, 구독을 끊으면 요청 해제 */
    @SuppressLint("MissingPermission")
    override fun locations(): Flow<DeviceLocation> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { trySend(it) }
            }
        }
        client.requestLocationUpdates(LOCATION_REQUEST, callback, Looper.getMainLooper())
            .addOnFailureListener { close(it) }
        awaitClose { client.removeLocationUpdates(callback) }
    }.map { it.toDeviceLocation(context) }
        .flowOn(Dispatchers.IO)

    /** 기기에 남아 있는 마지막 위치. 새로 측정하지 않아 빠르고, 없으면 null */
    @SuppressLint("MissingPermission")
    override suspend fun lastLocation(): DeviceLocation? {
        val location = suspendCancellableCoroutine<Location?> { cont ->
            client.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        } ?: return null
        return withContext(Dispatchers.IO) { location.toDeviceLocation(context) }
    }

    private companion object {
        const val INTERVAL_MILLIS = 1_000L
        const val MIN_DISTANCE_METERS = 3f

        // 높은 정확도, 1초 간격, 3m 이상 움직였을 때만
        val LOCATION_REQUEST: LocationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            INTERVAL_MILLIS,
        )
            .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            .build()
    }
}
