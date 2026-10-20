package com.routee.android.feature.recordedit.editor.model

internal sealed interface RecapEditorSideEffect {
    data object NavigateBack : RecapEditorSideEffect

    data object ShowNoRouteToast : RecapEditorSideEffect
}
