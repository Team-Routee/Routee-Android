package com.routee.android.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

private val LargeBrokenImageMinSize = 160.dp

/**
 * URL 이미지
 *
 * 불러오기에 실패하면 실패 이미지를 보여준다. 짧은 변이 160dp 이상이면 큰 그래픽(graphic_image_broken_xl),
 * 작으면 작은 아이콘(ic_image_broken_sm)을 가운데에 그린다
 *
 * @param defaultImage           기본 이미지. [url]이 null일 때. null 이면 회색 (TODO 임시)
 * @param loadingImage           로딩 중. 넘기지 않으면 [defaultImage]
 * @param errorImage             불러오기 실패. 넘기지 않으면 [defaultImage], 그것도 없으면 실패 이미지
 * @param errorBackgroundColor   실패 이미지 배경. 넘기지 않으면 큰 이미지는 grey800, 작은 이미지는 grey600
 */
@Composable
fun RouteeAsyncImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    defaultImage: Painter? = null,
    loadingImage: Painter? = defaultImage,
    errorImage: Painter? = defaultImage,
    errorBackgroundColor: Color = Color.Unspecified,
) {
    // TODO 기본 이미지 디자인 확정 시 교체 (지금은 임시 회색)
    val placeholderColor = RouteeTheme.colors.grey800
    val grey = remember(placeholderColor) { ColorPainter(placeholderColor) }
    val brokenImage = rememberBrokenImagePainter(backgroundColor = errorBackgroundColor)

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = loadingImage ?: grey,
        error = errorImage ?: brokenImage,
        fallback = defaultImage ?: grey,
        contentScale = contentScale,
    )
}

@Composable
private fun rememberBrokenImagePainter(backgroundColor: Color): Painter {
    val smallIcon = painterResource(R.drawable.ic_image_broken_sm_grey)
    val largeIcon = painterResource(R.drawable.graphic_image_broken_xl)
    val smallBackground = RouteeTheme.colors.grey600
    val largeBackground = RouteeTheme.colors.grey800
    val largeMinSizePx = with(LocalDensity.current) { LargeBrokenImageMinSize.toPx() }
    return remember(smallIcon, largeIcon, smallBackground, largeBackground, backgroundColor, largeMinSizePx) {
        BrokenImagePainter(
            smallIcon = smallIcon,
            largeIcon = largeIcon,
            smallBackground = if (backgroundColor.isSpecified) backgroundColor else smallBackground,
            largeBackground = if (backgroundColor.isSpecified) backgroundColor else largeBackground,
            largeMinSizePx = largeMinSizePx,
        )
    }
}

private class BrokenImagePainter(
    private val smallIcon: Painter,
    private val largeIcon: Painter,
    private val smallBackground: Color,
    private val largeBackground: Color,
    private val largeMinSizePx: Float,
) : Painter() {
    override val intrinsicSize: Size = Size.Unspecified

    override fun DrawScope.onDraw() {
        val isLarge = size.minDimension >= largeMinSizePx
        drawRect(if (isLarge) largeBackground else smallBackground)
        val icon = if (isLarge) largeIcon else smallIcon
        val iconSize = icon.intrinsicSize
        translate(left = (size.width - iconSize.width) / 2, top = (size.height - iconSize.height) / 2) {
            with(icon) { draw(iconSize) }
        }
    }
}

@PreviewFontSizes
@Composable
private fun BrokenImagePreview() {
    RouteePreviewTheme {
        Row {
            Image(
                painter = rememberBrokenImagePainter(backgroundColor = Color.Unspecified),
                contentDescription = null,
                modifier = Modifier.size(200.dp),
            )
            Image(
                painter = rememberBrokenImagePainter(backgroundColor = Color.Unspecified),
                contentDescription = null,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(RouteeTheme.radius.Radius12)),
            )
        }
    }
}
