package com.routee.android.feature.timeline.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.core.navigation.util.viewModelEntry
import com.routee.android.feature.recordedit.api.RecapEditorNavKey
import com.routee.android.feature.timeline.api.TimelineNavKey
import com.routee.android.feature.timeline.main.TimelineRoute
import com.routee.android.feature.timeline.main.TimelineRouteMap
import com.routee.android.feature.timeline.main.TimelineViewModel
import com.routee.android.feature.timeline.main.model.TimelineSaveDestination

fun EntryProviderScope<NavKey>.timelineEntries(navigator: RouteeNavigator) {
    viewModelEntry<TimelineNavKey, TimelineViewModel, TimelineViewModel.Factory> { key, viewModel ->
        TimelineRoute(
            viewModel = viewModel,
            onExit = navigator::navigateToHome,
            onSaveSuccess = { destination ->
                navigator.navigateToHome()
                if (destination == TimelineSaveDestination.RecordEdit) {
                    navigator.navigateTo(RecapEditorNavKey(key.activityId))
                }
            },
            onSaveRecord = viewModel::save,
            routeMap = {
                TimelineRouteMap(
                    routePoints = key.routePoints,
                    photos = key.photoMarkers,
                )
            },
        )
    }
}
