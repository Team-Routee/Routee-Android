package com.routee.android.feature.timeline.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.routee.android.core.map.RouteePhotoRouteMap
import com.routee.android.core.map.model.MapPhoto
import com.routee.android.core.map.model.MapPoint
import com.routee.android.feature.timeline.api.TimelineMapPhoto
import com.routee.android.feature.timeline.api.TimelineMapPoint

@Composable
internal fun TimelineRouteMap(
    routePoints: List<TimelineMapPoint>,
    photos: List<TimelineMapPhoto>,
    modifier: Modifier = Modifier,
) {
    val route = remember(routePoints) { routePoints.map { MapPoint(it.latitude, it.longitude) } }
    val markers = remember(photos) {
        photos.mapIndexed { index, photo ->
            MapPhoto(index.toString(), photo.imageUrl, MapPoint(photo.point.latitude, photo.point.longitude))
        }
    }
    RouteePhotoRouteMap(route = route, photos = markers, modifier = modifier)
}
