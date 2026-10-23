package com.routee.android.feature.timeline.main

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import coil3.Image
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.map.RouteeNaverMap
import com.routee.android.core.map.model.MapMarker
import com.routee.android.core.map.model.MapPoint
import com.routee.android.core.map.rememberRouteeMapState
import com.routee.android.feature.timeline.api.TimelineMapPhoto
import com.routee.android.feature.timeline.api.TimelineMapPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

private val MarkerSize = 42.dp
private val MarkerBorder = 3.dp

@Composable
internal fun TimelineRouteMap(
    routePoints: List<TimelineMapPoint>,
    photos: List<TimelineMapPhoto>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val density = LocalDensity.current
    val markerColor = RouteeTheme.colors.mint300.toArgb()
    val radiusDp = RouteeTheme.radius.Radius12
    val cameraPadding = MarkerSize + RouteeTheme.spacing.Spacing12
    val route = remember(routePoints) { routePoints.map { MapPoint(it.latitude, it.longitude) } }
    val cameraPoints = remember(route, photos) {
        route + photos.map { MapPoint(it.point.latitude, it.point.longitude) }
    }
    val state = rememberRouteeMapState()
    var mapSize by remember { mutableStateOf(IntSize.Zero) }
    val markers by produceState(emptyList<MapMarker>(), photos, density, markerColor, radiusDp, isPreview) {
        value = emptyList()
        if (isPreview || photos.isEmpty()) return@produceState
        val size = with(density) { MarkerSize.roundToPx() }
        val border = with(density) { MarkerBorder.toPx() }
        val radius = with(density) { radiusDp.toPx() }
        val loader = SingletonImageLoader.get(context)
        val semaphore = Semaphore(3)
        value = coroutineScope {
            photos.mapIndexed { index, photo ->
                async {
                    semaphore.withPermit {
                        val result = loader.execute(
                            ImageRequest.Builder(context)
                                .data(photo.imageUrl)
                                .size(size)
                                .allowHardware(false)
                                .build(),
                        )
                        val image = (result as? SuccessResult)?.image ?: return@withPermit null
                        MapMarker(
                            id = index.toString(),
                            point = MapPoint(photo.point.latitude, photo.point.longitude),
                            icon = photoMarkerBitmap(image, size, radius, border, markerColor).asImageBitmap(),
                            size = MarkerSize,
                        )
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    LaunchedEffect(state, cameraPoints, mapSize, cameraPadding, isPreview) {
        if (!isPreview && mapSize.width > 0 && mapSize.height > 0) {
            state.fitBounds(cameraPoints, padding = cameraPadding)
        }
    }
    RouteeNaverMap(
        state = state,
        route = route,
        markers = markers,
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
            .clip(RoundedCornerShape(radiusDp))
            .onSizeChanged { mapSize = it },
    )
}

private suspend fun photoMarkerBitmap(
    image: Image,
    size: Int,
    radius: Float,
    border: Float,
    color: Int,
): Bitmap = withContext(Dispatchers.Default) {
    val source = image.toBitmap(image.width.coerceAtLeast(1), image.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val bounds = RectF(0f, 0f, size.toFloat(), size.toFloat())
    val clip = Path().apply { addRoundRect(bounds, radius, radius, Path.Direction.CW) }
    val crop = minOf(source.width, source.height)
    val sourceRect = Rect(
        (source.width - crop) / 2,
        (source.height - crop) / 2,
        (source.width + crop) / 2,
        (source.height + crop) / 2,
    )
    canvas.clipPath(clip)
    canvas.drawBitmap(source, sourceRect, bounds, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
    val inset = border / 2f
    val borderBounds = RectF(inset, inset, size - inset, size - inset)
    canvas.drawRoundRect(
        borderBounds,
        radius - inset,
        radius - inset,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = border
            this.color = color
        },
    )
    bitmap
}
