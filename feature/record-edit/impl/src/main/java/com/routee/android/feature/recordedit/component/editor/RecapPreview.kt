package com.routee.android.feature.recordedit.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.routee.android.core.designsystem.component.RouteeAsyncImage
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.editor.model.RecapBrightness
import com.routee.android.feature.recordedit.editor.model.RecapRecordUiModel
import com.routee.android.feature.recordedit.editor.model.RecapSticker
import com.routee.android.feature.recordedit.editor.model.RecapTrackUiModel
import com.routee.android.feature.recordedit.editor.model.StickerFrame
import com.routee.android.feature.recordedit.model.RecapColor
import com.routee.android.feature.recordedit.model.StickerType
import com.routee.android.feature.recordedit.model.color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

// recap_image_default
private const val RECORD_STICKER_LEFT = 32f
private const val RECORD_STICKER_TOP = 80f

// sticker_bounding_box 테두리와 내용 사이 여백
private const val STICKER_INSET = 12f

/**
 * 기록 편집 미리보기
 *
 * 배경 위에 밝기 레이어를 깔고, 스티커를 [stickers] 순서대로 쌓는다
 *
 * @param backgroundUrl         배경 이미지. 처음에는 서버 지도 이미지
 * @param overlayOpacity        배경 위 검은 레이어 투명도 (0~1)
 * @param recapColor            스티커에 적용할 색
 * @param routeNames            루트 스티커 경유지 이름
 * @param stickers              캔버스에 놓인 스티커. 뒤에 있을수록 위
 * @param selectedSticker       테두리를 표시할 스티커. null 이면 선택 없음
 * @param onBackgroundClick     스티커가 아닌 곳 탭 시 호출
 * @param onStickerClick        스티커 탭 시 호출
 * @param onStickerFrameChange  스티커를 옮기거나 크기를 바꿀 때 호출
 * @param onStickerDeleteClick  스티커 삭제 버튼 탭 시 호출
 */
@Composable
internal fun RecapPreview(
    backgroundUrl: String?,
    overlayOpacity: Float,
    recapColor: RecapColor,
    record: RecapRecordUiModel,
    track: RecapTrackUiModel,
    routeNames: ImmutableList<String>,
    stickers: ImmutableList<RecapSticker>,
    selectedSticker: StickerType?,
    onBackgroundClick: () -> Unit,
    onStickerClick: (StickerType) -> Unit,
    onStickerFrameChange: (StickerType, StickerFrame) -> Unit,
    onStickerDeleteClick: (StickerType) -> Unit,
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
        val thumbnailUrls = remember(track) { track.markers.map { it.thumbnailUrl } }

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
        stickers.forEachIndexed { index, sticker ->
            // TODO 그릴 경로가 없을 때 사진 타임라인 스티커 처리(기획확인필요)
            if (sticker.type == StickerType.PHOTO_TIMELINE && timelineLayout == null) return@forEachIndexed

            key(sticker.type) {
                StickerBox(
                    frame = sticker.frame,
                    initialPosition = if (sticker.isCentered) null else sticker.type.initialPosition(timelineLayout),
                    isSelected = sticker.type == selectedSticker,
                    onClick = { onStickerClick(sticker.type) },
                    onFrameChange = { onStickerFrameChange(sticker.type, it) },
                    onDeleteClick = { onStickerDeleteClick(sticker.type) },
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(index.toFloat()),
                ) {
                    when (sticker.type) {
                        StickerType.WORKOUT_RECORD -> WorkoutRecordSticker(
                            record = record,
                            color = color,
                            modifier = Modifier.padding(STICKER_INSET.dp),
                        )

                        StickerType.PHOTO_TIMELINE -> timelineLayout?.let {
                            PhotoTimelineSticker(
                                layout = it,
                                thumbnailUrls = thumbnailUrls,
                                color = color,
                            )
                        }

                        StickerType.ROUTE -> RouteSticker(
                            routeNames = routeNames,
                            recapColor = recapColor,
                            modifier = Modifier.padding(STICKER_INSET.dp),
                        )
                    }
                }
            }
        }
    }
}

// 루트는 처음부터 가운데 (RT-EDIT-007)
private fun StickerType.initialPosition(timelineLayout: PhotoTimelineStickerLayout?): Offset? = when (this) {
    StickerType.WORKOUT_RECORD -> Offset(RECORD_STICKER_LEFT - STICKER_INSET, RECORD_STICKER_TOP - STICKER_INSET)
    StickerType.PHOTO_TIMELINE -> timelineLayout?.position
    StickerType.ROUTE -> null
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
            routeNames = persistentListOf("창의문", "청운대", "말바위 전망대에서 한 컷"),
            stickers = persistentListOf(
                RecapSticker(StickerType.WORKOUT_RECORD),
                RecapSticker(StickerType.ROUTE, isCentered = true),
            ),
            selectedSticker = StickerType.ROUTE,
            onBackgroundClick = {},
            onStickerClick = {},
            onStickerFrameChange = { _, _ -> },
            onStickerDeleteClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f),
        )
    }
}
