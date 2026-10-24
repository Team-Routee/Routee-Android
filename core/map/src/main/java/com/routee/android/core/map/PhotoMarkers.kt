package com.routee.android.core.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import coil3.Image
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.map.model.MapMarker
import com.routee.android.core.map.model.MapPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

// 카메라 여백 계산에도 씀
internal val PhotoMarkerSize = 42.dp
private val PhotoMarkerBorder = 3.dp
private const val MAX_CONCURRENT_LOADS = 3

/** 사진 썸네일 마커 목록, 이미지를 불러오지 못한 사진은 빠짐 */
@Composable
fun rememberPhotoMarkers(photos: List<MapPhoto>): List<MapMarker> {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val density = LocalDensity.current
    val borderColor = RouteeTheme.colors.mint300.toArgb()
    val radiusDp = RouteeTheme.radius.Radius12
    val markers by produceState(emptyList<MapMarker>(), photos, density, borderColor, radiusDp, isPreview) {
        if (isPreview || photos.isEmpty()) {
            value = emptyList()
            return@produceState
        }
        val size = with(density) { PhotoMarkerSize.roundToPx() }
        val border = with(density) { PhotoMarkerBorder.toPx() }
        val radius = with(density) { radiusDp.toPx() }
        val loader = SingletonImageLoader.get(context)
        val semaphore = Semaphore(MAX_CONCURRENT_LOADS)
        value = coroutineScope {
            photos.map { photo ->
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
                            id = photo.id,
                            point = photo.point,
                            icon = photoMarkerBitmap(image, size, radius, border, borderColor).asImageBitmap(),
                            size = PhotoMarkerSize,
                        )
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }
    return markers
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
