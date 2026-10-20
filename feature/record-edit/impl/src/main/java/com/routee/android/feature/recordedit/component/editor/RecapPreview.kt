package com.routee.android.feature.recordedit.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.component.RouteeAsyncImage
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.editor.model.RecapBrightness
import com.routee.android.feature.recordedit.editor.model.RecapRecordUiModel
import com.routee.android.feature.recordedit.editor.model.RecapTrackUiModel
import com.routee.android.feature.recordedit.model.RecapColor
import com.routee.android.feature.recordedit.model.color

// recap_image_default
private val RecordStickerLeft = 32.dp
private val RecordStickerTop = 80.dp

/**
 * 기록 편집 미리보기
 *
 * 배경 위에 밝기 레이어, 기록 스티커, 사진 타임라인 스티커를 차례로 쌓는다
 *
 * @param backgroundUrl     배경 이미지. 처음에는 서버 지도 이미지
 * @param overlayOpacity    배경 위 검은 레이어 투명도 (0~1)
 * @param recapColor        스티커에 적용할 색
 * @param onBackgroundClick 스티커가 아닌 곳 탭 시 호출
 */
@Composable
internal fun RecapPreview(
    backgroundUrl: String?,
    overlayOpacity: Float,
    recapColor: RecapColor,
    record: RecapRecordUiModel,
    track: RecapTrackUiModel,
    onBackgroundClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = recapColor.color

    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
            .pointerInput(onBackgroundClick) { detectTapGestures { onBackgroundClick() } },
    ) {
        val canvasSize = Size(maxWidth.value, maxHeight.value)
        val timelineLayout = remember(track, canvasSize) { PhotoTimelineStickerLayout.create(track, canvasSize) }

        RouteeAsyncImage(
            url = backgroundUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RouteeTheme.colors.black.copy(alpha = overlayOpacity)),
        )
        WorkoutRecordSticker(
            record = record,
            color = color,
            modifier = Modifier.offset(RecordStickerLeft, RecordStickerTop),
        )
        if (timelineLayout != null) {
            PhotoTimelineSticker(
                layout = timelineLayout,
                thumbnailUrls = track.markers.map { it.thumbnailUrl },
                color = color,
                modifier = Modifier.offset(timelineLayout.position.x.dp, timelineLayout.position.y.dp),
            )
        }
    }
}

@PreviewFontSizes
@Composable
private fun RecapPreviewPreview() {
    RouteePreviewTheme {
        RecapPreview(
            backgroundUrl = null,
            overlayOpacity = RecapBrightness.MAP_BASE_OPACITY,
            recapColor = RecapColor.DEFAULT,
            record = RecapRecordUiModel.from(
                distanceMeters = 15_500,
                durationSeconds = 12_000,
                maxElevationMeters = 2132,
            ),
            track = RecapTrackUiModel.EMPTY,
            onBackgroundClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f),
        )
    }
}
