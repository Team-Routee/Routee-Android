package com.routee.android.feature.recordedit.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 기록 편집 화면 키
 *
 * @param entryPoint 들어온 곳 (운동 직후 타임라인, 기록 편집 탭)
 */
@Serializable
data class RecapEditorNavKey(
    val activityId: Long,
    val entryPoint: RecapEditorEntryPoint,
) : NavKey

/** 기록 편집 화면에 들어온 곳 */
@Serializable
enum class RecapEditorEntryPoint {
    POST_WORKOUT,
    RECORD_EDIT_TAB,
}
