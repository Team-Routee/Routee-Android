package com.routee.android.core.analytics

import android.content.Context
import android.net.Uri
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse
import com.android.installreferrer.api.InstallReferrerStateListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber

internal class InstallReferrerReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun readUtm(): Map<String, String> = suspendCancellableCoroutine { continuation ->
        val client = InstallReferrerClient.newBuilder(context).build()
        continuation.invokeOnCancellation { client.endConnection() }
        client.startConnection(
            object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    val referrer = if (responseCode == InstallReferrerResponse.OK) {
                        runCatching { client.installReferrer.installReferrer }
                            .onFailure { Timber.w(it, "설치 경로 조회 실패") }
                            .getOrNull()
                    } else {
                        Timber.w("설치 경로 조회 실패: $responseCode")
                        null
                    }
                    client.endConnection()
                    if (continuation.isActive) continuation.resume(referrer?.let(::parseUtm).orEmpty())
                }

                // 연결이 다시 되면 onInstallReferrerSetupFinished가 다시 불림
                override fun onInstallReferrerServiceDisconnected() {
                    Timber.w("설치 경로 서비스 연결 끊김")
                }
            },
        )
    }

    private fun parseUtm(referrer: String): Map<String, String> {
        val uri = Uri.Builder().encodedQuery(referrer).build()
        return UTM_KEYS.mapNotNull { key -> uri.getQueryParameter(key)?.let { key to it } }.toMap()
    }

    private companion object {
        val UTM_KEYS = listOf("utm_source", "utm_medium", "utm_campaign")
    }
}
