package com.routee.android.feature.recordedit.main.model

internal sealed interface RecordEditMainSideEffect {
    data class NavigateToEditor(val activityId: Long) : RecordEditMainSideEffect

    data object ShowNetworkErrorToast : RecordEditMainSideEffect
}
