package com.routee.android.core.data.appnotice.remote.dto.response

import com.routee.android.core.data.appnotice.model.AppNotice
import com.routee.android.core.data.appnotice.model.AppNoticeConfig
import com.routee.android.core.data.appnotice.model.AppVersion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Remote Config android_app_notice 값. 빠진 필드는 기본값
@Serializable
internal data class AppNoticeResponse(
    @SerialName("maintenance") val maintenance: MaintenanceResponse? = null,
    @SerialName("forceUpdate") val forceUpdate: ForceUpdateResponse? = null,
    @SerialName("optionalUpdate") val optionalUpdate: OptionalUpdateResponse? = null,
)

@Serializable
internal data class MaintenanceResponse(
    @SerialName("enabled") val enabled: Boolean = false,
    @SerialName("title") val title: String? = null,
    @SerialName("message") val message: String? = null,
)

@Serializable
internal data class ForceUpdateResponse(
    @SerialName("minimumVersion") val minimumVersion: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("message") val message: String? = null,
)

@Serializable
internal data class OptionalUpdateResponse(
    @SerialName("latestVersion") val latestVersion: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("message") val message: String? = null,
)

internal fun AppNoticeResponse.toAppNoticeConfig(): AppNoticeConfig = AppNoticeConfig(
    maintenance = maintenance?.takeIf { it.enabled }?.let {
        AppNotice.Maintenance(title = it.title.orNullIfBlank(), message = it.message.orNullIfBlank())
    },
    minimumVersion = forceUpdate?.minimumVersion?.let(AppVersion::parse),
    forceUpdate = AppNotice.ForceUpdate(
        title = forceUpdate?.title.orNullIfBlank(),
        message = forceUpdate?.message.orNullIfBlank(),
    ),
    latestVersion = optionalUpdate?.latestVersion?.let(AppVersion::parse),
    optionalUpdate = AppNotice.OptionalUpdate(
        title = optionalUpdate?.title.orNullIfBlank(),
        message = optionalUpdate?.message.orNullIfBlank(),
    ),
)

private fun String?.orNullIfBlank(): String? = this?.takeIf { it.isNotBlank() }
