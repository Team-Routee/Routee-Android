package com.routee.android.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.routee.android.core.designsystem.theme.RouteeTheme

/**
 * URL 이미지
 *
 * @param defaultImage 기본 이미지. [url]이 null일 때. null 이면 회색 (TODO 임시)
 * @param loadingImage 로딩 중. 넘기지 않으면 [defaultImage]
 * @param errorImage 불러오기 실패. 넘기지 않으면 [defaultImage]
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
) {
    // TODO 기본 이미지 디자인 확정 시 교체 (지금은 임시 회색)
    val placeholderColor = RouteeTheme.colors.grey800
    val grey = remember(placeholderColor) { ColorPainter(placeholderColor) }

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = loadingImage ?: grey,
        error = errorImage ?: grey,
        fallback = defaultImage ?: grey,
        contentScale = contentScale,
    )
}
