package com.routee.android.core.data.appnotice.model

/**
 * 업데이트, 점검 안내 설정 (Remote Config android_app_notice)
 *
 * @param maintenance     null 이면 점검 아님
 * @param minimumVersion  이보다 낮으면 강제 업데이트. null 이면 확인 안 함
 * @param latestVersion   이보다 낮으면 권장 업데이트. null 이면 확인 안 함
 */
data class AppNoticeConfig(
    val maintenance: AppNotice.Maintenance? = null,
    val minimumVersion: AppVersion? = null,
    val forceUpdate: AppNotice.ForceUpdate = AppNotice.ForceUpdate(),
    val latestVersion: AppVersion? = null,
    val optionalUpdate: AppNotice.OptionalUpdate = AppNotice.OptionalUpdate(),
) {
    /** 앱 버전에 맞는 안내. 점검 > 강제 > 권장, 없으면 null */
    fun noticeFor(appVersion: String): AppNotice? {
        if (maintenance != null) return maintenance
        val current = AppVersion.parse(appVersion) ?: return null
        return when {
            minimumVersion != null && current < minimumVersion -> forceUpdate
            latestVersion != null && current < latestVersion -> optionalUpdate
            else -> null
        }
    }
}
