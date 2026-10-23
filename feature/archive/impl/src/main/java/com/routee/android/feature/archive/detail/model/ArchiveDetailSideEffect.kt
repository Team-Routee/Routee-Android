package com.routee.android.feature.archive.detail.model

internal sealed interface ArchiveDetailSideEffect {
    data object RecordChanged : ArchiveDetailSideEffect
    data object NavigateBack : ArchiveDetailSideEffect
    data object Deleted : ArchiveDetailSideEffect
    data object TitleSaveFailed : ArchiveDetailSideEffect
    data object DeleteFailed : ArchiveDetailSideEffect
}
