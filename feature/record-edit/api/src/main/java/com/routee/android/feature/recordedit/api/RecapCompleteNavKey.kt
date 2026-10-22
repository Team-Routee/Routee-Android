package com.routee.android.feature.recordedit.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 기록 편집 완료 화면 키
 *
 * @param imagePath 워터마크를 합성해 저장한 리캡 이미지 경로
 */
@Serializable
data class RecapCompleteNavKey(val imagePath: String) : NavKey
