package com.routee.android.core.designsystem.component.message

/**
 * 전역 메시지 (토스트, 스낵바)
 *
 * [RouteeMessageState.showToast], [RouteeMessageState.showSnackbar]로 띄운다
 */
sealed interface RouteeMessage {
    val duration: RouteeMessageDuration

    /**
     * 문구만 있는 토스트
     */
    data class Toast(
        val text: String,
        override val duration: RouteeMessageDuration = RouteeMessageDuration.SHORT,
    ) : RouteeMessage

    /**
     * 스낵바. [actionLabel]이 null이면 버튼 없이 문구만 보여준다
     */
    data class Snackbar(
        val text: String,
        val actionLabel: String? = null,
        override val duration: RouteeMessageDuration = RouteeMessageDuration.UNDO,
    ) : RouteeMessage
}

/**
 * 메시지 노출 시간
 */
enum class RouteeMessageDuration(val millis: Long) {
    BRIEF(600),
    SHORT(2_000),
    MEDIUM(2_500),
    LONG(3_000),
    UNDO(3_500),
}

/**
 * 메시지가 닫힌 이유
 */
enum class RouteeMessageResult {
    DISMISSED,
    ACTION_PERFORMED,
}
