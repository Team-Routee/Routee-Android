package com.routee.android.feature.setting.profile.model

internal sealed interface ProfileEditSideEffect {
    data object ShowSavedToast : ProfileEditSideEffect
}
