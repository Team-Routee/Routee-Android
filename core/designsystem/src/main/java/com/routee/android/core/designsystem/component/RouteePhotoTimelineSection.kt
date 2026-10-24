package com.routee.android.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.component.skeleton.skeleton
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme

/** 사진 타임라인 카드에 표시할 이미지와 위치 */
@Immutable
data class RouteeTimelinePhoto(val imageUrl: String?, val location: String?)

/**
 * 날짜와 사진 페이저를 표시하는 타임라인 섹션
 *
 * @param date 날짜 표시 문자열
 * @param onSaveClick 현재 사진 인덱스 전달. null이면 저장 버튼을 숨김
 * @param savedPhotoIndex 저장 완료 표시할 사진 인덱스
 * @param isLoading true 이면 사진 대신 스켈레톤 카드 표시
 */
@Composable
fun RouteePhotoTimelineSection(
    photos: List<RouteeTimelinePhoto>,
    date: String,
    modifier: Modifier = Modifier,
    onSaveClick: ((Int) -> Unit)? = null,
    savedPhotoIndex: Int? = null,
    isLoading: Boolean = false,
) {
    if (photos.isEmpty() && !isLoading) return
    val pagerState = rememberPagerState(pageCount = { photos.size })

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16),
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing4),
        ) {
            Text(
                text = stringResource(R.string.designsystem_photo_timeline_title),
                style = RouteeTheme.typography.titleSb18,
                color = RouteeTheme.colors.white,
            )
            Text(text = date, style = RouteeTheme.typography.labelM12, color = RouteeTheme.colors.grey200)
        }
        Spacer(Modifier.height(RouteeTheme.spacing.Spacing16))
        if (isLoading) {
            Box(Modifier.fillMaxWidth().aspectRatio(1f).skeleton(RectangleShape))
            return@Column
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        ) { page ->
            val photo = photos[page]
            RouteePhotoTimelineCard(
                imageUrl = photo.imageUrl,
                location = photo.location,
                isSaved = savedPhotoIndex == page,
                onSaveClick = onSaveClick?.let { save -> { save(page) } },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (photos.size > 1) {
            Spacer(Modifier.height(RouteeTheme.spacing.Spacing12))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                RouteePaginationDots(pageCount = photos.size, currentPage = pagerState.currentPage)
            }
        }
    }
}

@PreviewFontSizes
@Composable
private fun RouteePhotoTimelineSectionPreview() {
    RouteeTheme {
        RouteePhotoTimelineSection(
            photos = List(6) { RouteeTimelinePhoto(null, if (it == 0) "창의문" else null) },
            date = "2026.03.12",
            onSaveClick = {},
        )
    }
}

@PreviewFontSizes
@Composable
private fun RouteePhotoTimelineSectionLoadingPreview() {
    RouteeTheme {
        RouteePhotoTimelineSection(
            photos = emptyList(),
            date = "2026.03.12",
            isLoading = true,
        )
    }
}
