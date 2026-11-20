package com.routee.android.core.analytics

/**
 * 분석 이벤트 (Mixpanel 이벤트 이름)
 */
enum class AnalyticsEvent(val eventName: String) {
    SIGN_UP_COMPLETED("sign_up_completed"),
    ONBOARDING_COMPLETED("onboarding_completed"),

    WORKOUT_STARTED("workout_started"),
    WORKOUT_RECORDING_ENDED("workout_recording_ended"),
    WORKOUT_COMPLETED("workout_completed"),
    WORKOUT_COMPLETE_FAILED("workout_complete_failed"),
    WORKOUT_RECOVERY_SHOWN("workout_recovery_shown"),

    RECAP_EDITOR_OPENED("recap_editor_opened"),
    RECAP_EDITED("recap_edited"),
    RECAP_COMPLETED("recap_completed"),
    RECAP_ABANDONED("recap_abandoned"),
    RECAP_SAVED("recap_saved"),
    RECAP_SHARED("recap_shared"),
}
