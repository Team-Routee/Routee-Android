package com.routee.android.feature.recordedit.complete.model

import android.net.Uri

internal sealed interface RecapCompleteSideEffect {
    data object NavigateBack : RecapCompleteSideEffect

    data object NavigateToHome : RecapCompleteSideEffect

    data object RequestStoragePermission : RecapCompleteSideEffect

    data class ShareImage(val imageUri: Uri) : RecapCompleteSideEffect

    data object ShowSaveSuccessToast : RecapCompleteSideEffect

    data object ShowSaveFailureToast : RecapCompleteSideEffect

    data object ShowNoPermissionToast : RecapCompleteSideEffect
}
