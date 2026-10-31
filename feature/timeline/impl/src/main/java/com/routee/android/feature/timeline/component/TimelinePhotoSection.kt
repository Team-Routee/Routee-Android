package com.routee.android.feature.timeline.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.routee.android.core.data.archive.model.ArchivePhoto
import com.routee.android.core.designsystem.component.RouteePhotoTimelineSection
import com.routee.android.core.designsystem.component.RouteeTimelinePhoto
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.UiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val PhotoDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@Composable
internal fun TimelinePhotoSection(
    photos: UiState<List<ArchivePhoto>>,
    recordDate: String,
    onSaveClick: (Int, ArchivePhoto) -> Unit,
    modifier: Modifier = Modifier,
    savedPhotoIndex: Int = -1,
    saveMessage: String? = null,
) {
    val loaded = (photos as? UiState.Success)?.data.orEmpty()
    val date = remember(loaded, recordDate) {
        loaded.firstOrNull()?.createdAt?.atZone(ZoneId.systemDefault())?.format(PhotoDateFormatter) ?: recordDate
    }
    val items = remember(loaded) { loaded.map { RouteeTimelinePhoto(it.imageUrl, it.location) } }
    RouteePhotoTimelineSection(
        photos = items,
        date = date,
        onSaveClick = { index -> onSaveClick(index, loaded[index]) },
        savedPhotoIndex = savedPhotoIndex,
        saveMessage = saveMessage,
        isLoading = photos is UiState.Loading,
        modifier = modifier,
    )
}

@PreviewFontSizes
@Composable
private fun TimelinePhotoSectionPreview() {
    RouteeTheme {
        TimelinePhotoSection(
            photos = UiState.Success(listOf(ArchivePhoto(0, "창의문", "", Instant.ofEpochMilli(1_782_825_200_000)))),
            recordDate = "2026.07.18",
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
            photos = UiState.Success(List(6) { index -> if (index == 0) photo else photo.copy(location = null) }),
            recordDate = "2026.07.18",
            onSaveClick = { _, _ -> },
        )
    }
}
