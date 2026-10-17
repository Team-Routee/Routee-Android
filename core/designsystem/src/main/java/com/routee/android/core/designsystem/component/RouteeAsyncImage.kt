package com.routee.android.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.routee.android.core.designsystem.theme.RouteeTheme

/**
 * URL 이미지
 *
 * 이미지가 보이기 전에는 상태에 따라 [defaultImage], [loadingImage], [errorImage]를 가운데에 그린다
 * 크기는 [modifier]로 지정
 *
 * @param defaultImage 기본 이미지. [url]이 null일 때
 * @param loadingImage 로딩 중 (로딩 이미지, 스켈레톤). 넘기지 않으면 [defaultImage]
 * @param errorImage 불러오기 실패. 넘기지 않으면 [defaultImage]
 */
@Composable
fun RouteeAsyncImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    defaultImage: @Composable () -> Unit = { DefaultImage() },
    loadingImage: @Composable () -> Unit = defaultImage,
    errorImage: @Composable () -> Unit = defaultImage,
) {
    var state: AsyncImagePainter.State by remember(url) { mutableStateOf(AsyncImagePainter.State.Empty) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when {
            url == null -> defaultImage()
            state is AsyncImagePainter.State.Error -> errorImage()
            state !is AsyncImagePainter.State.Success -> loadingImage()
        }
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            onState = { state = it },
            contentScale = contentScale,
        )
    }
}

// TODO 기본 이미지 디자인 확정 시 교체 (지금은 임시 회색 배경)
@Composable
private fun DefaultImage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RouteeTheme.colors.grey800),
    )
}
