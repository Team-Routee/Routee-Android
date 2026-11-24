package com.routee.android.core.analytics

/**
 * 분석 이벤트 속성 키
 */
object AnalyticsProperty {
    const val ACTIVITY_ID = "activity_id"
    const val ACTIVITY_TYPE = "activity_type"
    const val ENTRY_POINT = "entry_point"
    const val HAS_EDIT = "has_edit"
    const val LOGIN_PROVIDER = "login_provider"
    const val RECOVERY_REASON = "recovery_reason"

    private const val ACTIVITY_TYPE_HIKING = "HIKING"

    /**
     * 활동 ID 속성
     *
     * @param activityId 서버 활동 ID, 문자로 바꿔 보냄
     */
    fun activity(activityId: Long): Map<String, Any> = mapOf(ACTIVITY_ID to activityId.toString())

    /**
     * 운동 이벤트 공통 속성 (활동 ID, 운동 종류)
     */
    fun workout(activityId: Long): Map<String, Any> = activity(activityId) + (ACTIVITY_TYPE to ACTIVITY_TYPE_HIKING)
}
