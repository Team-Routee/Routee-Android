package com.routee.android.feature.timeline.main.model

internal data class TimelineDraft(
    val title: String?, // null 이면 서버 제목 유지
    val routePoints: List<String>,
)
