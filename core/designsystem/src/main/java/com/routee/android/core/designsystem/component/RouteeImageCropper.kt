package com.routee.android.core.designsystem.component

import android.graphics.BitmapFactory
import android.net.Uri
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
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val ROTATION_DEGREES = 90
private val BorderLineThickness = 1.dp
private val CornerThickness = 5.dp
private val CornerLength = 15.dp
private val CornerOffset = (-2).dp
private val GuidelineThickness = 1.dp

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

    suspend fun crop(): Result<Uri> = mutex.withLock {
        val view = cropImageView ?: return Result.failure(IllegalStateException("CropImageView is not attached"))
        if (!isImageLoaded || view.imageUri == null) {
            return Result.failure(IllegalStateException("Image is not loaded"))
        }
        suspendCancellableCoroutine { continuation ->
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
            view.croppedImageAsync()
        }
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
            CropImageView(context).also { view ->
                state.cropImageView = view
                view.setOnSetImageUriCompleteListener { _, _, error ->
                    state.isImageLoaded = error == null
                }
            }
        },
        update = { view ->
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
