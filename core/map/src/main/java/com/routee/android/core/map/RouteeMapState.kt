package com.routee.android.core.map

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.util.CameraUtils
import com.routee.android.core.map.model.MapCamera
import com.routee.android.core.map.model.MapMarker
import com.routee.android.core.map.model.MapPoint
import kotlin.coroutines.resume
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * [RouteeNaverMap] 의 카메라 상태
 *
 * @property camera 지금 카메라 위치. 지도 준비 전이면 초기 위치
 */
@Stable
class RouteeMapState internal constructor(
    internal val initialCamera: MapCamera?,
) {
    var camera: MapCamera? by mutableStateOf(initialCamera)
        private set

    private var map: NaverMap? = null
    private var mapReady = CompletableDeferred<NaverMap>()
    private var density = Density(1f)

    // 지도에서 오는 카메라 이동 알림
    private val cameraChangeListener = NaverMap.OnCameraChangeListener { _, _ ->
        camera = map?.cameraPosition?.toMapCamera()
    }

    /**
     * 카메라를 옮긴다. 지도가 준비될 때까지 기다린다
     *
     * @param zoom    null 이면 지금 줌 유지
     * @param animate true 이면 부드럽게 이동
     */
    suspend fun moveTo(point: MapPoint, zoom: Double? = null, animate: Boolean = false) {
        val map = mapReady.await()
        val update = if (zoom == null) {
            CameraUpdate.scrollTo(point.toLatLng())
        } else {
            CameraUpdate.scrollAndZoomTo(point.toLatLng(), zoom)
        }
        map.moveCamera(if (animate) update.animate(CameraAnimation.Easing) else update)
    }

    /**
     * 좌표들이 모두 보이게 카메라를 맞춘다. 좌표가 하나면 그 좌표로. 지도가 준비될 때까지 기다린다
     *
     * @param padding 가장자리 여백
     * @param maxZoom 줌 상한
     */
    suspend fun fitBounds(coordinates: List<MapPoint>, padding: Dp = 0.dp, maxZoom: Double = DEFAULT_MAX_FIT_ZOOM) {
        if (coordinates.isEmpty()) return
        val map = mapReady.await()
        map.moveCamera(fitCameraUpdate(map, coordinates, padding, maxZoom))
    }

    /** 내 위치 따라가기 시작. 손으로 지도를 움직이면 풀린다 */
    suspend fun followMyLocation() {
        mapReady.await().locationTrackingMode = LocationTrackingMode.Follow
    }

    /**
     * 좌표들이 모두 보이게 카메라를 맞추고, 이동이 끝나면 지도를 캡처한다
     *
     * @param padding 가장자리 여백
     * @param maxZoom 줌 상한
     */
    suspend fun captureRoute(
        coordinates: List<MapPoint>,
        padding: Dp = 0.dp,
        maxZoom: Double = DEFAULT_MAX_FIT_ZOOM,
    ): Bitmap {
        val map = mapReady.await()
        if (coordinates.isNotEmpty()) map.moveAndAwait(fitCameraUpdate(map, coordinates, padding, maxZoom))
        withTimeoutOrNull(RENDER_TIMEOUT_MILLIS) { map.awaitFullyRendered() }
        return suspendCancellableCoroutine { cont -> map.takeSnapshot(false) { cont.resume(it) } }
    }

    // 카메라 이동이 끝날 때까지 대기
    private suspend fun NaverMap.moveAndAwait(update: CameraUpdate) = suspendCancellableCoroutine { cont ->
        moveCamera(update.finishCallback { cont.resume(Unit) }.cancelCallback { cont.resume(Unit) })
    }

    // 타일까지 다 그려질 때까지 대기
    private suspend fun NaverMap.awaitFullyRendered() = suspendCancellableCoroutine { cont ->
        val listener = object : NaverMap.OnMapRenderedListener {
            override fun onMapRendered(fullyRendered: Boolean, renderingStable: Boolean) {
                if (!fullyRendered || !renderingStable) return
                removeOnMapRenderedListener(this)
                if (cont.isActive) cont.resume(Unit)
            }
        }
        addOnMapRenderedListener(listener)
        cont.invokeOnCancellation { removeOnMapRenderedListener(listener) }
    }

    // 좌표 범위에 맞춘 카메라 이동
    private fun fitCameraUpdate(
        map: NaverMap,
        coordinates: List<MapPoint>,
        padding: Dp,
        maxZoom: Double,
    ): CameraUpdate {
        val latLngs = coordinates.map(MapPoint::toLatLng)
        if (latLngs.size == 1) return CameraUpdate.scrollAndZoomTo(latLngs.first(), maxZoom)
        val bounds = LatLngBounds.from(latLngs)
        val zoom = CameraUtils.getFittableZoom(map, bounds, with(density) { padding.roundToPx() })
        return CameraUpdate.scrollAndZoomTo(bounds.center, cappedZoom(zoom, maxZoom))
    }

    internal fun attach(naverMap: NaverMap, density: Density) {
        map = naverMap
        this.density = density
        camera = naverMap.cameraPosition.toMapCamera()
        naverMap.addOnCameraChangeListener(cameraChangeListener)
        mapReady.complete(naverMap)
    }

    internal fun detach() {
        map?.removeOnCameraChangeListener(cameraChangeListener)
        map = null
        mapReady = CompletableDeferred()
    }

    internal companion object {
        const val DEFAULT_MAX_FIT_ZOOM = 18.0
        const val RENDER_TIMEOUT_MILLIS = 3_000L
    }
}

/** 회전, 프로세스 종료 뒤 카메라 복원은 MapView 상태 저장이 맡는다 */
@Composable
fun rememberRouteeMapState(initialCamera: MapCamera? = null): RouteeMapState =
    remember { RouteeMapState(initialCamera) }

internal fun cappedZoom(zoom: Double, maxZoom: Double) = minOf(zoom, maxZoom)

internal fun MapPoint.toLatLng() = LatLng(latitude, longitude)

internal fun CameraPosition.toMapCamera() = MapCamera(MapPoint(target.latitude, target.longitude), zoom)

internal fun MapCamera.toCameraPosition() = CameraPosition(target.toLatLng(), zoom)
