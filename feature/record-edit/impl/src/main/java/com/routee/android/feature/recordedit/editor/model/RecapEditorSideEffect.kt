package com.routee.android.feature.recordedit.editor.model

internal sealed interface RecapEditorSideEffect {
    data object NavigateBack : RecapEditorSideEffect

    data object ShowNoRouteToast : RecapEditorSideEffect

    data object ShowNetworkErrorToast : RecapEditorSideEffect

    data object OpenGallery : RecapEditorSideEffect

    data object CaptureRecap : RecapEditorSideEffect

    data class NavigateToComplete(val imagePath: String) : RecapEditorSideEffect
}
