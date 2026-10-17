package com.routee.android.feature.recordedit.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.feature.recordedit.api.RecordEditMainNavKey
import com.routee.android.feature.recordedit.main.RecordEditMainRoute
import com.routee.android.feature.workout.api.WorkoutMainNavKey

fun EntryProviderScope<NavKey>.recordEditEntries(navigator: RouteeNavigator) {
    entry<RecordEditMainNavKey> {
        RecordEditMainRoute(
            // TODO #63 편집 화면 엔트리를 등록하면서 RecapEditorNavKey로 이동 연결
            onNavigateToEditor = {},
            onNavigateToHome = { navigator.clearAndNavigateTo(WorkoutMainNavKey) },
        )
    }
}
