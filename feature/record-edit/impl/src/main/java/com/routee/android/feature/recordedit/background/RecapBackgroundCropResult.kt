package com.routee.android.feature.recordedit.background

/**
 * 배경 자르기 화면에서 편집 화면으로 돌려주는 결과
 */
internal sealed interface RecapBackgroundCropResult {
    data class Complete(val imageUri: String) : RecapBackgroundCropResult

    data object Cancel : RecapBackgroundCropResult
}
