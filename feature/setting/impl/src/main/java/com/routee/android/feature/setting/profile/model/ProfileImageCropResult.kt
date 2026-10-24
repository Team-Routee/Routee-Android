package com.routee.android.feature.setting.profile.model

/**
 * 프로필 사진 자르기 화면에서 프로필 변경 화면으로 돌려주는 결과
 */
internal sealed interface ProfileImageCropResult {
    data class Complete(val imageUri: String) : ProfileImageCropResult

    data object Cancel : ProfileImageCropResult
}
