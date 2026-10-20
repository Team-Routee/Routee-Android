package com.routee.android.feature.timeline.navigation

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.map.model.MapPoint
import com.routee.android.core.map.rememberRouteeMapState
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.core.navigation.util.viewModelEntry
import com.routee.android.feature.recordedit.api.RecapEditorNavKey
import com.routee.android.feature.timeline.api.TimelineNavKey
import com.routee.android.feature.timeline.main.TimelineRoute
import com.routee.android.feature.timeline.main.TimelineRouteMap
import com.routee.android.feature.timeline.main.TimelineSaveViewModel
import com.routee.android.feature.timeline.main.TimelineViewModel
import com.routee.android.feature.timeline.main.model.TimelineSaveDestination
import com.routee.android.feature.timeline.main.model.TimelineSaveResult
import kotlinx.coroutines.withTimeoutOrNull

fun EntryProviderScope<NavKey>.timelineEntries(navigator: RouteeNavigator) {
    viewModelEntry<TimelineNavKey, TimelineViewModel, TimelineViewModel.Factory> { key, viewModel ->
        val mapState = rememberRouteeMapState()
        val route = remember(key.routePoints) { key.routePoints.map { MapPoint(it.latitude, it.longitude) } }
        val saveViewModel: TimelineSaveViewModel = hiltViewModel()
        TimelineRoute(
            viewModel = viewModel,
            onExit = navigator::navigateToHome,
            onSaveSuccess = { destination ->
                navigator.navigateToHome()
                if (destination == TimelineSaveDestination.RecordEdit) {
                    navigator.navigateTo(RecapEditorNavKey(key.activityId))
                }
            },
            onSaveRecord = { draft ->
                val bitmap = withTimeoutOrNull(10_000) { mapState.captureRoute(route) }
                if (bitmap == null) {
                    TimelineSaveResult.Failed
                } else {
                    try {
                        saveViewModel.save(key, draft, bitmap)
                    } finally {
                        bitmap.recycle()
                    }
                }
            },
            routePointCount = key.routePoints.size,
            routeMap = {
                TimelineRouteMap(
                    routePoints = key.routePoints,
                    photos = key.photoMarkers,
                    state = mapState,
                )
            },
        )
    }
}
