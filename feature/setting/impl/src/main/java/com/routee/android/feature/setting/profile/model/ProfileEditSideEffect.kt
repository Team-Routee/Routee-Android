package com.routee.android.feature.setting.profile.model

internal sealed interface ProfileEditSideEffect {
    data object OpenGallery : ProfileEditSideEffect

    data object ShowSavedToast : ProfileEditSideEffect

    data object ShowNetworkErrorToast : ProfileEditSideEffect

    data object ShowUnknownErrorToast : ProfileEditSideEffect
}
