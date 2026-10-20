package com.routee.android.feature.timeline.main

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.routee.android.core.designsystem.component.RouteePhotoTimelineCard
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.timeline.R
import com.routee.android.feature.timeline.api.TimelinePhoto
import com.routee.android.feature.timeline.component.PaginationDots
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val PhotoDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@Composable
internal fun TimelinePhotoSection(
    photos: List<TimelinePhoto>,
    onSaveClick: (TimelinePhoto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstPhoto = photos.firstOrNull() ?: return
    val date = remember(firstPhoto.capturedAtEpochMillis) {
        Instant.ofEpochMilli(firstPhoto.capturedAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .format(PhotoDateFormatter)
    }
    val pagerState = rememberPagerState(pageCount = { photos.size })

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RouteeTheme.spacing.Spacing16),
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing4),
        ) {
            Text(
                text = stringResource(R.string.timeline_photo_title),
                style = RouteeTheme.typography.titleSb18,
                color = RouteeTheme.colors.white,
            )
            Text(
                text = date,
                style = RouteeTheme.typography.labelM12,
                color = RouteeTheme.colors.grey200,
            )
        }
        Spacer(Modifier.height(RouteeTheme.spacing.Spacing16))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) { page ->
            val photo = photos[page]
            RouteePhotoTimelineCard(
                imageUrl = photo.imageUrl,
                location = photo.location,
                onSaveClick = { onSaveClick(photo) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (photos.size > 1) {
            Spacer(Modifier.height(RouteeTheme.spacing.Spacing12))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                PaginationDots(pageCount = photos.size, currentPage = pagerState.currentPage)
            }
        }
    }
}

@PreviewFontSizes
@Composable
private fun TimelinePhotoSectionPreview() {
    RouteeTheme {
        TimelinePhotoSection(
            photos = listOf(TimelinePhoto("", 1_782_825_200_000, "창의문")),
            onSaveClick = {},
        )
    }
}

@PreviewFontSizes
@Composable
private fun TimelinePhotoSectionMultiplePreview() {
    RouteeTheme {
        val photo = TimelinePhoto("", 1_782_825_200_000, "창의문")
        TimelinePhotoSection(
            photos = List(6) { index -> if (index == 0) photo else photo.copy(location = null) },
            onSaveClick = {},
        )
    }
}
