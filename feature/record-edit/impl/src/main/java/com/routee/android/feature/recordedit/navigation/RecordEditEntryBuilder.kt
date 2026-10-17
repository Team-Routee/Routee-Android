package com.routee.android.feature.recordedit.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.feature.recordedit.api.RecapEditorNavKey
import com.routee.android.feature.recordedit.api.RecordEditMainNavKey
import com.routee.android.feature.recordedit.main.RecordEditMainRoute
import com.routee.android.feature.workout.api.WorkoutMainNavKey

fun EntryProviderScope<NavKey>.recordEditEntries(navigator: RouteeNavigator) {
    entry<RecordEditMainNavKey> {
        RecordEditMainRoute(
            onNavigateToEditor = { activityId -> navigator.navigateTo(RecapEditorNavKey(activityId)) },
            onNavigateToHome = { navigator.clearAndNavigateTo(WorkoutMainNavKey) },
        )
    }
}
