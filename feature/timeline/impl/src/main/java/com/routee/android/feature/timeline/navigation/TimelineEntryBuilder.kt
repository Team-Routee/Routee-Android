package com.routee.android.feature.timeline.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.feature.timeline.api.TimelineNavKey
import com.routee.android.feature.timeline.main.TimelineRoute

fun EntryProviderScope<NavKey>.timelineEntries(navigator: RouteeNavigator) {
    entry<TimelineNavKey> { key ->
        TimelineRoute(
            key = key,
            onBackClick = navigator::navigateBack,
            onCompleteClick = {}, // TODO #75 완료 동작 연결
            onRecordEditClick = {}, // TODO #75 기록 편집 이동 연결
            onPhotoSaveClick = {}, // TODO #80 사진 갤러리 저장 연결
        )
    }
}
