package com.routee.android.core.data.appnotice.model

/**
 * 앱 시작 시 띄우는 안내. [title], [message]가 null 이면 앱 기본 문구
 */
sealed interface AppNotice {
    val title: String?
    val message: String?

    /** true 이면 앱을 쓸 수 없음 (점검, 강제 업데이트) */
    val blocksApp: Boolean get() = this is Maintenance || this is ForceUpdate

    /** 서비스 점검 */
    data class Maintenance(
        override val title: String? = null,
        override val message: String? = null,
    ) : AppNotice

    /** 강제 업데이트 */
    data class ForceUpdate(
        override val title: String? = null,
        override val message: String? = null,
    ) : AppNotice

    /** 권장 업데이트 */
    data class OptionalUpdate(
        override val title: String? = null,
        override val message: String? = null,
    ) : AppNotice
}
