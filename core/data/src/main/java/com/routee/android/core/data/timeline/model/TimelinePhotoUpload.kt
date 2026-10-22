package com.routee.android.core.data.timeline.model

import java.io.File

/** 촬영 사진 한 장의 업로드·타임라인 등록에 필요한 정보 */
data class TimelinePhotoUpload(
    val clientId: String,
    val file: File,
    val capturedAtEpochMillis: Long,
    val title: String,
    val trackPointIndex: Int,
    val longitude: Double,
    val latitude: Double,
    val elevation: Int,
    val pointIndex: Int,
    val status: TimelinePhotoStatus,
)

/** 사진 타임라인 등록 상태 */
enum class TimelinePhotoStatus { SUCCESSFUL_CREATED, CREATION_IN_PROGRESS }

/** 실패한 사진 재시도에 사용할 식별자와 사진별 등록 결과 */
data class TimelinePhotoUploadResult(
    val clientId: String,
    val result: Result<Long>,
)
