package com.routee.android.core.data.location.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** 기기 위치 서비스(설정 > 위치) 켜짐 여부 */
internal class LocationServiceDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val locationManager = checkNotNull(context.getSystemService(LocationManager::class.java))

    /** 지금 값을 바로 보내고, 켜고 끌 때마다 다시 보냄 */
    fun observeEnabled(): Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent,
            ) {
                trySend(isEnabled())
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        trySend(isEnabled())
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()

    private fun isEnabled(): Boolean = LocationManagerCompat.isLocationEnabled(locationManager)
}
