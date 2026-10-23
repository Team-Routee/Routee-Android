package com.routee.android.core.map

import android.location.Location
import android.os.Bundle
import android.view.Gravity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.map.model.MapCamera
import com.routee.android.core.map.model.MapMarker
import com.routee.android.core.map.model.MapPoint
import kotlinx.coroutines.flow.Flow

/**
 * 네이버 지도. 야간 모드, 산악 레이어, 기본 버튼 없음, 로고 왼쪽 아래로 고정
 *
 * @param state          카메라 상태 ([rememberRouteeMapState])
 * @param myLocation     내 위치. null 이면 숨김
 * @param myLocationIcon 내 위치 점 아이콘. null 이면 SDK 기본
 * @param route          경로선. 2개 미만이면 숨김
 * @param markers        마커 (사진 등)
 * @param onMarkerClick  마커 탭 시 호출 ([MapMarker.id])
 * @param content        지도 위에 올릴 UI. 로고 위치는 [mapLogoAbove]
 */
@Composable
fun RouteeNaverMap(
    modifier: Modifier = Modifier,
    state: RouteeMapState = rememberRouteeMapState(),
    myLocation: Flow<Location>? = null,
    @DrawableRes myLocationIcon: Int? = null,
    route: List<MapPoint> = emptyList(),
    markers: List<MapMarker> = emptyList(),
    onMarkerClick: (String) -> Unit = {},
    content: (@Composable BoxScope.() -> Unit)? = null,
) {
    val logoAnchor = remember { MapLogoAnchor() }

    Box(modifier = modifier) {
        NaverMapView(
            state = state,
            myLocation = myLocation,
            myLocationIcon = myLocationIcon,
            route = route,
            markers = markers,
            logoAnchor = logoAnchor,
            onMarkerClick = onMarkerClick,
            modifier = Modifier.matchParentSize(),
        )
        CompositionLocalProvider(LocalMapLogoAnchor provides logoAnchor) {
            content?.invoke(this@Box)
        }
    }
}

@Composable
private fun NaverMapView(
    state: RouteeMapState,
    myLocation: Flow<Location>?,
    @DrawableRes myLocationIcon: Int?,
    route: List<MapPoint>,
    markers: List<MapMarker>,
    logoAnchor: MapLogoAnchor?,
    onMarkerClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Preview 에서는 지도 대신 빈 영역
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(RouteeTheme.colors.white))
        return
    }

    val context = LocalContext.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    var savedState by rememberSaveable { mutableStateOf<Bundle?>(null) }
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    var mapBounds by remember { mutableStateOf<Rect?>(null) }
    val locationSource = remember { MapLocationSource() }
    val currentMyLocationIcon by rememberUpdatedState(myLocationIcon)
    // 지도는 한 번만 만듦. 내 위치가 처음 보일 때부터 앱 아이콘이도록 여기서 먼저 정함
    val mapView = remember {
        MapView(context, routeeMapOptions(state.initialCamera)).apply {
            getMapAsync {
                it.locationSource = locationSource
                it.applyMyLocationIcon(currentMyLocationIcon)
                naverMap = it
            }
        }
    }

    // 생명주기 전달
    MapViewLifecycle(mapView = mapView, savedState = savedState, onSave = { savedState = it })

    // 지도 준비되면 상태 연결
    DisposableEffect(state, naverMap, density) {
        naverMap?.let { state.attach(it, density) }
        onDispose { state.detach() }
    }

    // 지도 준비 뒤 내 위치, 경로선, 마커 반영
    naverMap?.let { map ->
        MyLocationEffect(map, locationSource, myLocation, myLocationIcon)
        RouteEffect(map, route, color = RouteeTheme.colors.mint300, widthPx = with(density) { RouteWidth.roundToPx() })
        MarkersEffect(map, markers, density, onClick = onMarkerClick)
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.onGloballyPositioned { mapBounds = it.boundsInRoot() },
        // mapLogoAbove 를 단 컴포넌트 위로 로고 이동
        update = {
            val ui = naverMap?.uiSettings ?: return@AndroidView
            val logo = logoAnchor?.logoPlacement(mapBounds, density, layoutDirection) ?: return@AndroidView
            if (logo.alignRight) {
                ui.logoGravity = Gravity.RIGHT or Gravity.BOTTOM
                ui.setLogoMargin(0, 0, logo.horizontal, logo.bottom)
            } else {
                ui.logoGravity = Gravity.LEFT or Gravity.BOTTOM
                ui.setLogoMargin(logo.horizontal, 0, 0, logo.bottom)
            }
        },
    )
}

// 첫 프레임부터 적용할 고정 설정
private fun routeeMapOptions(initialCamera: MapCamera?) = NaverMapOptions()
    .nightModeEnabled(true)
    .backgroundResource(NaverMap.DEFAULT_BACKGROUND_DRWABLE_DARK)
    .enabledLayerGroups(NaverMap.LAYER_GROUP_BUILDING, NaverMap.LAYER_GROUP_MOUNTAIN)
    .locationButtonEnabled(false)
    .scaleBarEnabled(false)
    .zoomControlEnabled(false)
    .compassEnabled(false)
    .logoGravity(Gravity.START or Gravity.BOTTOM)
    .apply { initialCamera?.let { camera(it.toCameraPosition()) } }
