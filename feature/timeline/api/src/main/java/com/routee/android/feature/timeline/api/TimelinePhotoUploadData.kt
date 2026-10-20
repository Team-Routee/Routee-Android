package com.routee.android.feature.timeline.api

import kotlinx.serialization.Serializable

/** [TimelineNavKey]에서 사진 등록에 사용하는 원본 파일 경로와 촬영 위치 정보 */
@Serializable
data class TimelinePhotoUploadData(
    val clientId: String,
    val filePath: String,
    val capturedAtEpochMillis: Long,
    val title: String? = null,
    val trackPointIndex: Int,
    val longitude: Double,
    val latitude: Double,
    val elevation: Int,
    val pointIndex: Int,
    val creationInProgress: Boolean = false,
)
