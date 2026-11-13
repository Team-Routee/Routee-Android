package com.routee.android.core.designsystem.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Precision
import coil3.toBitmap
import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val QUARTER_TURN_DEGREES = 90f
private const val MAX_DECODE_SIZE = 4096
private const val OUTPUT_JPEG_QUALITY = 90
private const val CROPPED_FILE_PREFIX = "cropped"
private const val CROPPED_FILE_SUFFIX = ".jpg"
private const val GUIDELINE_COUNT = 3
private const val RECENTER_DELAY_MILLIS = 800L
private const val RECENTER_DURATION_MILLIS = 500
private val CropAreaHorizontalPadding = 16.dp
private val CropAreaVerticalPadding = 52.dp
private val BorderLineThickness = 1.dp
private val CornerThickness = 5.dp
private val CornerLength = 15.dp
private val CornerOutset = 1.5.dp
private val GuidelineThickness = 1.dp
private val HandleTouchRadius = 24.dp
private val MinBoxSize = 42.dp

/**
 * [RouteeImageCropper] 기본값
 */
object RouteeImageCropperDefaults {
    /** 자른 이미지 긴 변 최대 px */
    const val MAX_OUTPUT_SIZE = 2048
}

/**
 * 자르기 영역 비율
 */
enum class RouteeCropAspectRatio(
    val x: Int,
    val y: Int,
) {
    Story(x = 9, y = 16),
    Square(x = 1, y = 1),
}

/**
 * [RouteeImageCropper]의 회전, 초기화, 자르기를 호출하는 상태
 */
@Stable
class RouteeImageCropperState internal constructor(private val cacheDirectory: File) {
    internal var bitmap: Bitmap? by mutableStateOf(null)
    internal var geometry: CropGeometry? by mutableStateOf(null)
    private val mutex = Mutex()

    fun rotateLeft() {
        geometry = geometry?.rotated(clockwise = false)
    }

    fun rotateRight() {
        geometry = geometry?.rotated(clockwise = true)
    }

    fun reset() {
        geometry = geometry?.reset()
    }

    /**
     * 긴 변 최대 [maxOutputSize]px로 잘라 캐시에 저장하고, 성공하면 이전에 자른 파일은 지운다
     */
    suspend fun crop(maxOutputSize: Int = RouteeImageCropperDefaults.MAX_OUTPUT_SIZE): Result<Uri> = mutex.withLock {
        val source = bitmap ?: return Result.failure(IllegalStateException("Image is not loaded"))
        val current = geometry ?: return Result.failure(IllegalStateException("Image is not loaded"))
        suspendRunCatching {
            withContext(Dispatchers.IO) {
                val file = File(cacheDirectory, "$CROPPED_FILE_PREFIX${System.currentTimeMillis()}$CROPPED_FILE_SUFFIX")
                val cropped = source.cropped(current, maxOutputSize)
                try {
                    file.outputStream().use { output ->
                        check(cropped.compress(Bitmap.CompressFormat.JPEG, OUTPUT_JPEG_QUALITY, output)) {
                            "Image crop failed"
                        }
                    }
                } finally {
                    cropped.recycle()
                }
                cacheDirectory
                    .listFiles { other -> other.name.startsWith(CROPPED_FILE_PREFIX) && other.name != file.name }
                    ?.forEach(File::delete)
                Uri.fromFile(file)
            }
        }
    }
}

@Composable
fun rememberRouteeImageCropperState(): RouteeImageCropperState {
    val context = LocalContext.current
    return remember(context) { RouteeImageCropperState(context.cacheDir) }
}

/**
 * 이미지 자르기 영역 (crop_box)
 *
 * 두 손가락으로 사진을 확대·축소하고 한 손가락으로 옮기며, 흰 테두리와 모서리 핸들을 드래그해 영역을 조절한다. 만지는 동안 3x3 격자가 표시된다. 회전, 초기화, 자르기는 [state]로 호출
 */
@Composable
fun RouteeImageCropper(
    imageUri: Uri?,
    aspectRatio: RouteeCropAspectRatio,
    modifier: Modifier = Modifier,
    state: RouteeImageCropperState = rememberRouteeImageCropperState(),
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val isPreview = LocalInspectionMode.current
    val lineColor = RouteeTheme.colors.white
    val overlayColor = RouteeTheme.colors.black80
    val paint = remember { Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG) }
    var isTouching by remember { mutableStateOf(false) }

    val bitmap by produceState(
        initialValue = if (isPreview) {
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.img_bg_login,
            )
        } else {
            null
        },
        imageUri,
    ) {
        if (!isPreview) {
            value = null
            value = imageUri?.let { context.loadCropBitmap(it) }
        }
    }

    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val contentBounds = with(density) {
            Rect(
                left = CropAreaHorizontalPadding.toPx(),
                top = CropAreaVerticalPadding.toPx(),
                right = maxWidth.toPx() - CropAreaHorizontalPadding.toPx(),
                bottom = maxHeight.toPx() - CropAreaVerticalPadding.toPx(),
            )
        }
        remember(state, bitmap, contentBounds, aspectRatio) {
            state.bitmap = bitmap
            state.geometry = bitmap?.let { image ->
                CropGeometry.initial(
                    contentBounds = contentBounds,
                    aspectRatio = aspectRatio.x.toFloat() / aspectRatio.y,
                    imageSize = Size(image.width.toFloat(), image.height.toFloat()),
                    minBoxSize = with(density) { MinBoxSize.toPx() },
                )
            }
        }

        val needsRecenter = !isTouching && state.geometry?.isSettled == false
        LaunchedEffect(needsRecenter) {
            if (!needsRecenter) return@LaunchedEffect
            delay(RECENTER_DELAY_MILLIS)
            val start = state.geometry ?: return@LaunchedEffect
            val target = start.recentered()
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(RECENTER_DURATION_MILLIS, easing = LinearOutSlowInEasing),
            ) { fraction, _ ->
                state.geometry = start.interpolatedTo(target, fraction)
            }
            state.geometry = target
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state) {
                    detectCropGestures(state = state, onTouchChange = { isTouching = it })
                },
        ) {
            val image = state.bitmap ?: return@Canvas
            val geometry = state.geometry ?: return@Canvas
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawBitmap(image, geometry.imageMatrix(image), paint)
            }
            drawOverlay(geometry = geometry, color = overlayColor)
            if (isTouching) drawGuidelines(box = geometry.box, color = lineColor)
            drawBorder(box = geometry.box, color = lineColor)
            drawCorners(box = geometry.box, color = lineColor)
        }
    }
}

private suspend fun Context.loadCropBitmap(uri: Uri): Bitmap? {
    val request = ImageRequest.Builder(this)
        .data(uri)
        .size(MAX_DECODE_SIZE)
        .precision(Precision.INEXACT)
        .allowHardware(false)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .build()
    return imageLoader.execute(request).image?.toBitmap()
}

private suspend fun PointerInputScope.detectCropGestures(
    state: RouteeImageCropperState,
    onTouchChange: (Boolean) -> Unit,
) = awaitEachGesture {
    val down = awaitFirstDown(requireUnconsumed = false)
    onTouchChange(true)
    val start = state.geometry
    val handle = start?.handleAt(down.position, HandleTouchRadius.toPx())
    val grabOffset = handle?.let { start.handlePosition(it) - down.position } ?: Offset.Zero
    var isResizing = handle != null
    do {
        val event = awaitPointerEvent()
        if (event.changes.count { it.pressed } > 1) isResizing = false
        val pointer = event.changes.firstOrNull { it.id == down.id && it.pressed }
        val centroid = event.calculateCentroid()
        state.geometry = when {
            isResizing && handle != null && pointer != null ->
                start?.resized(handle, pointer.position + grabOffset)

            !isResizing && centroid.isSpecified ->
                state.geometry?.transformed(centroid, event.calculatePan(), event.calculateZoom())

            else -> state.geometry
        }
        event.changes.forEach { it.consume() }
    } while (event.changes.any { it.pressed })
    onTouchChange(false)
}

private fun CropGeometry.imageMatrix(image: Bitmap): Matrix = Matrix().apply {
    setTranslate(-image.width / 2f, -image.height / 2f)
    postRotate(quarterTurns * QUARTER_TURN_DEGREES)
    postScale(scale, scale)
    postTranslate(center.x, center.y)
}

private fun Bitmap.cropped(
    geometry: CropGeometry,
    maxOutputSize: Int,
): Bitmap {
    val box = geometry.box
    val sourceWidth = box.width / geometry.scale
    val sourceHeight = box.height / geometry.scale
    val outputScale = min(1f, maxOutputSize / max(sourceWidth, sourceHeight))
    val outputWidth = (sourceWidth * outputScale).roundToInt().coerceAtLeast(1)
    val outputHeight = (sourceHeight * outputScale).roundToInt().coerceAtLeast(1)
    val output = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
    val matrix = geometry.imageMatrix(this).apply {
        postTranslate(-box.left, -box.top)
        postScale(outputWidth / box.width, outputHeight / box.height)
    }
    Canvas(output).drawBitmap(this, matrix, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
    return output
}

private fun DrawScope.drawOverlay(
    geometry: CropGeometry,
    color: Color,
) {
    val photo = geometry.photoRect
    val box = geometry.box
    listOf(
        Rect(photo.left, photo.top, photo.right, box.top),
        Rect(photo.left, box.bottom, photo.right, photo.bottom),
        Rect(photo.left, box.top, box.left, box.bottom),
        Rect(box.right, box.top, photo.right, box.bottom),
    ).forEach { rect ->
        drawRect(color = color, topLeft = rect.topLeft, size = rect.size)
    }
}

private fun DrawScope.drawGuidelines(
    box: Rect,
    color: Color,
) {
    val thickness = GuidelineThickness.toPx()
    for (index in 1 until GUIDELINE_COUNT) {
        val x = box.left + box.width * index / GUIDELINE_COUNT
        val y = box.top + box.height * index / GUIDELINE_COUNT
        drawLine(color, Offset(x, box.top), Offset(x, box.bottom), thickness)
        drawLine(color, Offset(box.left, y), Offset(box.right, y), thickness)
    }
}

private fun DrawScope.drawBorder(
    box: Rect,
    color: Color,
) {
    val thickness = BorderLineThickness.toPx()
    drawRect(
        color = color,
        topLeft = box.topLeft + Offset(thickness / 2, thickness / 2),
        size = Size(box.width - thickness, box.height - thickness),
        style = Stroke(width = thickness),
    )
}

private fun DrawScope.drawCorners(
    box: Rect,
    color: Color,
) {
    val thickness = CornerThickness.toPx()
    val length = CornerLength.toPx()
    val outset = CornerOutset.toPx()
    val horizontalSides = listOf(box.left - outset to 1f, box.right + outset to -1f)
    val verticalSides = listOf(box.top - outset to 1f, box.bottom + outset to -1f)
    horizontalSides.forEach { (x, xInward) ->
        verticalSides.forEach { (y, yInward) ->
            val corner = Offset(x, y)
            drawLine(
                color = color,
                start = corner - Offset(xInward * thickness / 2, 0f),
                end = corner + Offset(xInward * length, 0f),
                strokeWidth = thickness,
            )
            drawLine(
                color = color,
                start = corner - Offset(0f, yInward * thickness / 2),
                end = corner + Offset(0f, yInward * length),
                strokeWidth = thickness,
            )
        }
    }
}

private class RouteeImageCropperPreviewParameterProvider : PreviewParameterProvider<RouteeCropAspectRatio> {
    override val values = RouteeCropAspectRatio.entries.asSequence()
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun RouteeImageCropperPreview(
    @PreviewParameter(RouteeImageCropperPreviewParameterProvider::class) aspectRatio: RouteeCropAspectRatio,
) {
    RouteePreviewTheme {
        RouteeImageCropper(
            imageUri = null,
            aspectRatio = aspectRatio,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
