package com.routee.android.core.data.appnotice.repository

import com.routee.android.core.data.appnotice.model.AppNoticeConfig

/**
 * 업데이트, 점검 안내 설정 저장소 (Firebase Remote Config)
 */
interface AppNoticeRepository {
    /** 받아오기가 실패하거나 늦으면 마지막으로 받은 값, 처음이면 빈 설정. 값을 읽지 못하면 실패 */
    suspend fun getAppNoticeConfig(): Result<AppNoticeConfig>
}
