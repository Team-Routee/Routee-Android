package com.routee.android.core.designsystem.component

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import android.os.SystemClock
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import java.io.File
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val ROTATION_DEGREES = 90
private const val MAX_OUTPUT_SIZE = 2048
private const val CROPPED_FILE_PREFIX = "cropped"
private val BorderLineThickness = 1.dp
private val CornerThickness = 5.dp
private val CornerLength = 15.dp
private val CornerOffset = (-2).dp
private val GuidelineThickness = 1.dp
private const val MIN_PINCH_SCALE = 0.25f
private const val MAX_PINCH_SCALE = 4f

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
class RouteeImageCropperState internal constructor() {
    internal var cropImageView: CropImageView? = null
    internal var isImageLoaded: Boolean = false
    private val mutex = Mutex()

    fun rotateLeft() {
        cropImageView?.rotateImage(-ROTATION_DEGREES)
    }

    fun rotateRight() {
        cropImageView?.rotateImage(ROTATION_DEGREES)
    }

    fun reset() {
        cropImageView?.resetCropRect()
    }

    /**
     * 긴 변 최대 [MAX_OUTPUT_SIZE]px로 잘라 캐시에 저장하고, 성공하면 이전에 자른 파일은 지운다
     */
    suspend fun crop(): Result<Uri> = mutex.withLock {
        val view = cropImageView ?: return Result.failure(IllegalStateException("CropImageView is not attached"))
        if (!isImageLoaded || view.imageUri == null) {
            return Result.failure(IllegalStateException("Image is not loaded"))
        }
        val cropResult = suspendCancellableCoroutine { continuation ->
            view.setOnCropImageCompleteListener { _, result ->
                view.setOnCropImageCompleteListener(null)
                val uri = result.uriContent
                continuation.resume(
                    if (result.isSuccessful && uri != null) {
                        Result.success(uri)
                    } else {
                        Result.failure(result.error ?: IllegalStateException("Image crop failed"))
                    },
                )
            }
            continuation.invokeOnCancellation { view.setOnCropImageCompleteListener(null) }
            view.croppedImageAsync(reqWidth = MAX_OUTPUT_SIZE, reqHeight = MAX_OUTPUT_SIZE)
        }
        cropResult.onSuccess { uri ->
            deletePreviousCroppedFiles(directory = view.context.cacheDir, keepFileName = uri.lastPathSegment)
        }
    }

    private suspend fun deletePreviousCroppedFiles(
        directory: File,
        keepFileName: String?,
    ) = withContext(Dispatchers.IO) {
        directory
            .listFiles { file -> file.name.startsWith(CROPPED_FILE_PREFIX) && file.name != keepFileName }
            ?.forEach(File::delete)
    }
}

@Composable
fun rememberRouteeImageCropperState(): RouteeImageCropperState = remember { RouteeImageCropperState() }

/**
 * 이미지 자르기 영역 (crop_box)
 *
 * 흰 테두리와 모서리 핸들을 드래그해 영역을 조절하고, 드래그하는 동안 3x3 격자가 표시된다. 회전, 초기화, 자르기는 [state]로 호출
 */
@Composable
fun RouteeImageCropper(
    imageUri: Uri?,
    aspectRatio: RouteeCropAspectRatio,
    modifier: Modifier = Modifier,
    state: RouteeImageCropperState = rememberRouteeImageCropperState(),
) {
    val density = LocalDensity.current
    val isPreview = LocalInspectionMode.current
    val lineColor = RouteeTheme.colors.white.toArgb()
    val overlayColor = RouteeTheme.colors.black80.toArgb()
    val options = remember(aspectRatio, density, lineColor, overlayColor) {
        with(density) {
            CropImageOptions(
                guidelines = CropImageView.Guidelines.ON_TOUCH,
                borderLineThickness = BorderLineThickness.toPx(),
                borderLineColor = lineColor,
                borderCornerThickness = CornerThickness.toPx(),
                borderCornerLength = CornerLength.toPx(),
                borderCornerOffset = CornerOffset.toPx(),
                borderCornerColor = lineColor,
                guidelinesThickness = GuidelineThickness.toPx(),
                guidelinesColor = lineColor,
                backgroundColor = overlayColor,
                fixAspectRatio = true,
                aspectRatioX = aspectRatio.x,
                aspectRatioY = aspectRatio.y,
                showProgressBar = false,
            )
        }
    }

    AndroidView(
        factory = { context ->
            PinchCropImageLayout(context).also { layout ->
                state.cropImageView = layout.cropImageView
                layout.cropImageView.setOnSetImageUriCompleteListener { _, _, error ->
                    state.isImageLoaded = error == null
                }
            }
        },
        update = { layout ->
            val view = layout.cropImageView
            state.cropImageView = view
            if (view.tag != options) {
                view.setImageCropOptions(options)
                view.tag = options
            }
            if (isPreview) {
                view.setImageBitmap(BitmapFactory.decodeResource(view.resources, R.drawable.img_bg_login))
                state.isImageLoaded = true
            } else if (imageUri == null) {
                view.clearImage()
                state.isImageLoaded = false
            } else if (view.imageUri != imageUri) {
                state.isImageLoaded = false
                view.setImageUriAsync(imageUri)
            }
        },
        onRelease = {
            state.cropImageView = null
            state.isImageLoaded = false
        },
        modifier = modifier,
    )
}

/**
 * 두 손가락 핀치를 자르기 영역 왼쪽 위 모서리 드래그로 바꿔 [cropImageView]에 전달한다.
 * 벌리면 영역이 커지고 오므리면 작아지며, 손을 떼면 라이브러리 자동 확대·축소가 사진 배율을 맞춘다
 */
private class PinchCropImageLayout(context: Context) : FrameLayout(context) {
    val cropImageView = CropImageView(context)

    private var isMultiTouch = false
    private var pinchStartRect: RectF? = null
    private var pinchScale = 1f
    private var pinchDownTime = 0L
    private var lastCorner = PointF()

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                val rect = cropImageView.cropWindowRect?.let(::RectF) ?: return false
                pinchStartRect = rect
                pinchScale = 1f
                pinchDownTime = SystemClock.uptimeMillis()
                lastCorner = PointF(rect.left, rect.top)
                dispatchCornerDrag(MotionEvent.ACTION_DOWN, lastCorner)
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val rect = pinchStartRect ?: return false
                pinchScale = (pinchScale * detector.scaleFactor).coerceIn(MIN_PINCH_SCALE, MAX_PINCH_SCALE)
                lastCorner = PointF(rect.right - rect.width() * pinchScale, rect.bottom - rect.height() * pinchScale)
                dispatchCornerDrag(MotionEvent.ACTION_MOVE, lastCorner)
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                endPinch()
            }
        },
    ).apply { isQuickScaleEnabled = false }

    init {
        addView(cropImageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    // 자르기 영역이 부모 가로채기를 막아서 onInterceptTouchEvent 대신 여기서 나눈다
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) isMultiTouch = false
        scaleDetector.onTouchEvent(event)
        if (!isMultiTouch && event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            isMultiTouch = true
            val cancel = MotionEvent.obtain(event).apply { action = MotionEvent.ACTION_CANCEL }
            super.dispatchTouchEvent(cancel)
            cancel.recycle()
        }
        if (!isMultiTouch) return super.dispatchTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            endPinch()
            isMultiTouch = false
        }
        return true
    }

    private fun endPinch() {
        if (pinchStartRect == null) return
        dispatchCornerDrag(MotionEvent.ACTION_UP, lastCorner)
        pinchStartRect = null
    }

    private fun dispatchCornerDrag(
        action: Int,
        corner: PointF,
    ) {
        val event = MotionEvent.obtain(pinchDownTime, SystemClock.uptimeMillis(), action, corner.x, corner.y, 0)
        cropImageView.dispatchTouchEvent(event)
        event.recycle()
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
