package com.routee.android.feature.timeline.main.model

internal sealed interface TimelineSideEffect {
    data class TrackLoadFailed(val result: TimelineRequestResult) : TimelineSideEffect
}
