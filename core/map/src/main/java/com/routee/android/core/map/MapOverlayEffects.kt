package com.routee.android.core.map

import android.location.Location
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.naver.maps.map.LocationSource
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.routee.android.core.map.model.MapCamera
import com.routee.android.core.map.model.MapMarker
import com.routee.android.core.map.model.MapPoint
import kotlinx.coroutines.flow.Flow

@Composable
internal fun MyLocationEffect(
    map: NaverMap,
    source: MapLocationSource,
    myLocation: Flow<Location>?,
    @DrawableRes icon: Int?,
) {
    // 내 위치 점 아이콘
    LaunchedEffect(map, icon) {
        icon?.let { map.locationOverlay.icon = OverlayImage.fromResource(it) }
    }
    // 내 위치를 SDK 에 전달. 화면이 보일 때만 수집
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(map, myLocation, lifecycle) {
        val locations = myLocation ?: run {
            map.locationTrackingMode = LocationTrackingMode.None
            return@LaunchedEffect
        }
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            locations.collect { location ->
                source.update(location)
                if (map.locationTrackingMode == LocationTrackingMode.None) {
                    map.locationTrackingMode = LocationTrackingMode.NoFollow
                }
            }
        }
    }
}

@Composable
internal fun RouteEffect(
    map: NaverMap,
    route: List<MapPoint>,
    color: Color,
    widthPx: Int,
) {
    val path = remember { PathOverlay().apply { outlineWidth = 0 } }
    // 경로선 그리기, 좌표가 2개 미만이면 숨김
    DisposableEffect(map, route, color, widthPx) {
        path.color = color.toArgb()
        path.width = widthPx
        if (route.size >= 2) {
            path.coords = route.map(MapPoint::toLatLng)
            path.map = map
        } else {
            path.map = null
        }
        onDispose { path.map = null }
    }
}

@Composable
internal fun MarkersEffect(
    map: NaverMap,
    markers: List<MapMarker>,
    density: Density,
    onClick: (String) -> Unit,
) {
    val currentOnClick by rememberUpdatedState(onClick)
    // 마커 다시 붙이기, 빠질 때 모두 제거
    DisposableEffect(map, markers, density) {
        val overlays = markers.map { item ->
            Marker().apply {
                position = item.point.toLatLng()
                item.icon?.let { icon = OverlayImage.fromBitmap(it.asAndroidBitmap()) }
                item.size?.let {
                    width = with(density) { it.roundToPx() }
                    height = width
                }
                setOnClickListener {
                    currentOnClick(item.id)
                    true
                }
                this.map = map
            }
        }
        onDispose { overlays.forEach { it.map = null } }
    }
}

internal val RouteWidth = 4.dp

internal class MapLocationSource : LocationSource {
    private var listener: LocationSource.OnLocationChangedListener? = null
    private var last: Location? = null

    fun update(location: Location) {
        last = location
        listener?.onLocationChanged(location)
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        this.listener = listener
        last?.let(listener::onLocationChanged)
    }

    override fun deactivate() {
        listener = null
    }
}
