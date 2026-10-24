package com.routee.android.feature.timeline.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.routee.android.core.data.archive.model.ArchivePhoto
import com.routee.android.core.designsystem.component.RouteePhotoTimelineSection
import com.routee.android.core.designsystem.component.RouteeTimelinePhoto
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val PhotoDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@Composable
internal fun TimelinePhotoSection(
    photos: List<ArchivePhoto>,
    onSaveClick: (Int, ArchivePhoto) -> Unit,
    modifier: Modifier = Modifier,
    savedPhotoIndex: Int = -1,
) {
    val firstPhoto = photos.firstOrNull() ?: return
    val date = remember(firstPhoto.createdAt) {
        firstPhoto.createdAt
            .atZone(ZoneId.systemDefault())
            .format(PhotoDateFormatter)
    }
    val items = remember(photos) { photos.map { RouteeTimelinePhoto(it.imageUrl, it.location) } }
    RouteePhotoTimelineSection(
        photos = items,
        date = date,
        onSaveClick = { index -> onSaveClick(index, photos[index]) },
        savedPhotoIndex = savedPhotoIndex,
        modifier = modifier,
    )
}

@PreviewFontSizes
@Composable
private fun TimelinePhotoSectionPreview() {
    RouteeTheme {
        TimelinePhotoSection(
            photos = listOf(ArchivePhoto(0, "창의문", "", Instant.ofEpochMilli(1_782_825_200_000))),
            onSaveClick = { _, _ -> },
        )
    }
}

@PreviewFontSizes
@Composable
private fun TimelinePhotoSectionMultiplePreview() {
    RouteeTheme {
        val photo = ArchivePhoto(0, "창의문", "", Instant.ofEpochMilli(1_782_825_200_000))
        TimelinePhotoSection(
            photos = List(6) { index -> if (index == 0) photo else photo.copy(location = null) },
            onSaveClick = { _, _ -> },
        )
    }
}
