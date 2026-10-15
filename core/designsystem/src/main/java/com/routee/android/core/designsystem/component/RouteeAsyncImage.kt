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
 * URL 이미지를 불러온다. 로딩 중이거나 실패하면 회색 배경을 보여준다
 *
 * @param fallback [url]이 null이거나 실패했을 때 보여줄 이미지. null이면 회색 배경
 */
@Composable
fun RouteeAsyncImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: Painter? = null,
) {
    val placeholderColor = RouteeTheme.colors.grey800
    val placeholder = remember(placeholderColor) { ColorPainter(placeholderColor) }

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = placeholder,
        error = fallback ?: placeholder,
        fallback = fallback ?: placeholder,
        contentScale = contentScale,
    )
}
