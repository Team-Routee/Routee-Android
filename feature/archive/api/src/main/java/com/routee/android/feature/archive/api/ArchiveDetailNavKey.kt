package com.routee.android.feature.archive.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 저장된 운동 기록 상세 화면을 여는 키 */
@Serializable
data class ArchiveDetailNavKey(val activityId: Long) : NavKey
