package com.routee.android.feature.recordedit.component.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.model.RecapThumbnailType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// workout_recap_item_android 썸네일 (큰 사진 132 x 170 기준)
private const val MAIN_PHOTO_ASPECT_RATIO = 132f / 170f
private const val SMALL_PHOTO_WIDTH_FRACTION = 52f / 132f
private const val SMALL_PHOTO_ASPECT_RATIO = 52f / 66f
private val MainPhotoBorderThickness = 4.dp
private val SmallPhotoBorderThickness = 2.dp

/**
 * 큰 사진 위에 겹쳐지는 작은 사진의 위치(큰 사진 크기 대비 중심 비율)와 기울기
 *
 * 피그마 workout_recap_item_android의 picture_frame 좌표를 큰 사진 기준 비율로 환산한 값
 */
@Immutable
private data class SmallPhotoPlacement(
    val centerXFraction: Float,
    val centerYFraction: Float,
    val rotationDegrees: Float,
)

private val SmallPhotoPlacements = listOf(
    SmallPhotoPlacement(centerXFraction = 38.6f / 132f, centerYFraction = 126.3f / 170f, rotationDegrees = -8f),
    SmallPhotoPlacement(centerXFraction = 80.27f / 132f, centerYFraction = 117.84f / 170f, rotationDegrees = 5f),
    SmallPhotoPlacement(centerXFraction = 96.44f / 132f, centerYFraction = 128.75f / 170f, rotationDegrees = 8f),
)

/**
 * 기록 카드 썸네일 (workout_thumb)
 *
 * 첫 번째 사진은 크게, 나머지는 최대 3장까지 작게 기울여 겹쳐 보여준다
 * 사진이 없으면 루티 심볼을 보여준다
 *
 * @param thumbnailUrls 촬영 순서대로 정렬된 사진 URL
 */
@Composable
internal fun RecapThumbnail(
    thumbnailUrls: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    val thumbnailType = RecapThumbnailType.from(thumbnailUrls.size)
    val smallPhotoUrls = remember(thumbnailUrls) {
        thumbnailUrls.drop(1).take(thumbnailType.smallPhotoCount).toImmutableList()
    }

    // 바깥 반투명 테두리(workout_thumb)는 감싸는 레이아웃 없이 사진 영역의 Modifier로 그린다
    val frameModifier = modifier
        .clip(RoundedCornerShape(RouteeTheme.radius.Radius20))
        .background(RouteeTheme.colors.white10)
        .padding(RouteeTheme.spacing.Spacing6)
        .aspectRatio(MAIN_PHOTO_ASPECT_RATIO)

    if (thumbnailType == RecapThumbnailType.NONE) {
        EmptyMainPhoto(modifier = frameModifier)
    } else {
        PhotoStack(
            mainPhotoUrl = thumbnailUrls.first(),
            smallPhotoUrls = smallPhotoUrls,
            modifier = frameModifier,
        )
    }
}

@Composable
private fun EmptyMainPhoto(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(RouteeTheme.radius.Radius16))
            .background(RouteeTheme.colors.grey900),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(DesignSystemR.drawable.routee_symbol_grey_sm),
            contentDescription = null,
        )
    }
}

@Composable
private fun PhotoStack(
    mainPhotoUrl: String,
    smallPhotoUrls: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    val mainShape = RoundedCornerShape(RouteeTheme.radius.Radius16)

    Box(modifier = modifier) {
        ThumbnailImage(
            url = mainPhotoUrl,
            modifier = Modifier
                .fillMaxSize()
                .clip(mainShape)
                .border(MainPhotoBorderThickness, RouteeTheme.colors.white30, mainShape),
        )
        smallPhotoUrls.forEachIndexed { index, url ->
            SmallPhoto(
                url = url,
                placement = SmallPhotoPlacements[index],
            )
        }
    }
}

@Composable
private fun SmallPhoto(
    url: String,
    placement: SmallPhotoPlacement,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(RouteeTheme.radius.Radius8)

    ThumbnailImage(
        url = url,
        modifier = modifier
            .placeInParent(placement)
            .rotate(placement.rotationDegrees)
            .clip(shape)
            .border(SmallPhotoBorderThickness, RouteeTheme.colors.white, shape),
    )
}

@Composable
private fun ThumbnailImage(
    url: String,
    modifier: Modifier = Modifier,
) {
    val placeholderColor = RouteeTheme.colors.grey800
    val placeholder = remember(placeholderColor) { ColorPainter(placeholderColor) }

    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier,
        placeholder = placeholder,
        error = placeholder,
        contentScale = ContentScale.Crop,
    )
}

/**
 * 부모(큰 사진) 크기에 비례해 작은 사진의 크기를 정하고, 지정한 중심 비율 위치에 배치한다
 */
private fun Modifier.placeInParent(placement: SmallPhotoPlacement): Modifier = layout { measurable, constraints ->
    val parentWidth = constraints.maxWidth
    val parentHeight = constraints.maxHeight
    val width = (parentWidth * SMALL_PHOTO_WIDTH_FRACTION).toInt()
    val height = (width / SMALL_PHOTO_ASPECT_RATIO).toInt()
    val placeable = measurable.measure(Constraints.fixed(width, height))

    layout(parentWidth, parentHeight) {
        placeable.place(
            x = (parentWidth * placement.centerXFraction - width / 2f).toInt(),
            y = (parentHeight * placement.centerYFraction - height / 2f).toInt(),
        )
    }
}

private class RecapThumbnailPreviewParameterProvider : PreviewParameterProvider<ImmutableList<String>> {
    override val values = (0..4).asSequence().map { count ->
        List(count) { index -> "https://example.com/photo_$index.jpg" }.toImmutableList()
    }
}

@Preview(widthDp = 144)
@Composable
private fun RecapThumbnailPreview(
    @PreviewParameter(RecapThumbnailPreviewParameterProvider::class) thumbnailUrls: ImmutableList<String>,
) {
    RouteeTheme {
        RecapThumbnail(
            thumbnailUrls = thumbnailUrls,
            modifier = Modifier.background(RouteeTheme.colors.bgPrimary),
        )
    }
}
