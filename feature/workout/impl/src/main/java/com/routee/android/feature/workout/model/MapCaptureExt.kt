package com.routee.android.feature.workout.model

import android.graphics.Bitmap
import androidx.compose.ui.unit.dp
import com.routee.android.core.data.upload.UPLOAD_JPEG_QUALITY
import com.routee.android.core.map.RouteeMapState
import com.routee.android.core.map.model.MapPoint
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

private const val MAP_CAPTURE_TIMEOUT_MILLIS = 10_000L
private val MAP_CAPTURE_PADDING = 48.dp

/** 경로 전체를 담은 지도 캡처 JPEG, 실패하면 null */
internal suspend fun RouteeMapState.captureRouteJpeg(route: List<MapPoint>): ByteArray? = try {
    withTimeoutOrNull(MAP_CAPTURE_TIMEOUT_MILLIS) {
        val bitmap = captureRoute(route, padding = MAP_CAPTURE_PADDING)
        try {
            withContext(Dispatchers.Default) {
                ByteArrayOutputStream().use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, UPLOAD_JPEG_QUALITY, output))
                    output.toByteArray()
                }
            }
        } finally {
            bitmap.recycle()
        }
    }
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Timber.e(e, "지도 캡처 실패")
    null
}
