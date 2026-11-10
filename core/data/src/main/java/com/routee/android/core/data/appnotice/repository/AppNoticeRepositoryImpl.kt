package com.routee.android.core.data.appnotice.repository

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.BuildConfig
import com.routee.android.core.data.appnotice.model.AppNoticeConfig
import com.routee.android.core.data.appnotice.remote.dto.response.AppNoticeResponse
import com.routee.android.core.data.appnotice.remote.dto.response.toAppNoticeConfig
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json

internal class AppNoticeRepositoryImpl @Inject constructor(
    private val json: Json,
) : AppNoticeRepository {
    override suspend fun getAppNoticeConfig(): Result<AppNoticeConfig> = withContext(Dispatchers.IO) {
        suspendRunCatching {
            val remoteConfig = Firebase.remoteConfig
            refreshRemoteConfig(remoteConfig)
            val value = remoteConfig.getString(KEY_APP_NOTICE)
            if (value.isBlank()) return@suspendRunCatching AppNoticeConfig()
            json.decodeFromString<AppNoticeResponse>(value).toAppNoticeConfig()
        }
    }

    private suspend fun refreshRemoteConfig(remoteConfig: FirebaseRemoteConfig) {
        suspendRunCatching {
            withTimeout(FETCH_TIMEOUT) {
                remoteConfig.setConfigSettingsAsync(FETCH_SETTINGS).await()
                remoteConfig.fetchAndActivate().await()
            }
        }
    }

    private companion object {
        const val KEY_APP_NOTICE = "android_app_notice"
        val FETCH_TIMEOUT = 3.seconds
        val FETCH_SETTINGS = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0L else 3_600L
        }
    }
}
