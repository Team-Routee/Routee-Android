package com.routee.android.feature.archive.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.RouteePhotoTimelineSection
import com.routee.android.core.designsystem.component.RouteeTimelinePhoto
import com.routee.android.core.designsystem.component.RouteeWorkoutMetricCard
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.UiState
import com.routee.android.core.ui.error.RouteeErrorScreen
import com.routee.android.core.ui.error.RouteeErrorType
import com.routee.android.core.ui.loading.LocalRouteeLoadingState
import com.routee.android.core.ui.loading.RouteeLoadingHost
import com.routee.android.core.ui.loading.RouteeLoadingOverlay
import com.routee.android.core.ui.loading.RouteeLoadingState
import com.routee.android.feature.archive.R
import com.routee.android.feature.archive.component.RoutePointItem
import com.routee.android.feature.archive.detail.component.ArchiveDetailMap
import com.routee.android.feature.archive.detail.model.ArchiveDetailRecord
import com.routee.android.feature.archive.detail.model.ArchiveDetailUiState
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ArchiveDetailRoute(
    activityId: Long,
    onBackClick: () -> Unit,
    uiState: ArchiveDetailUiState = ArchiveDetailUiState(),
) {
    var expanded by rememberSaveable(activityId) { mutableStateOf(false) }

    ArchiveDetailScreen(
        uiState = uiState,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        onBackClick = onBackClick,
    )
}

@Composable
private fun ArchiveDetailScreen(
    uiState: ArchiveDetailUiState,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val record = uiState.record) {
        UiState.Empty, is UiState.Failure -> RouteeErrorScreen(
            type = if (record == UiState.Empty) RouteeErrorType.NotFound else RouteeErrorType.Unknown,
            onHomeClick = onBackClick,
            modifier = modifier.fillMaxSize().navigationBarsPadding(),
        )

        else -> RouteeBgEllipse(modifier = modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                RouteePageNavBar(onBackClick = onBackClick)
                if (record is UiState.Success) {
                    ArchiveDetailContent(
                        record = record.data,
                        expanded = expanded,
                        onExpandedChange = onExpandedChange,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    RouteeLoadingOverlay()
                }
            }
        }
    }
}

@Composable
private fun ArchiveDetailContent(
    record: ArchiveDetailRecord,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16),
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8),
        ) {
            Box(Modifier.fillMaxWidth().heightIn(min = 44.dp), contentAlignment = Alignment.CenterStart) {
                Text(
                    text = record.title,
                    style = RouteeTheme.typography.titleSb20,
                    color = RouteeTheme.colors.white,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            RouteeWorkoutMetricCard(
                distance = record.distance,
                duration = record.duration,
                maxElevation = record.maxElevation,
                modifier = Modifier.fillMaxWidth(),
            )
            ArchiveDetailMap(track = record.track, markers = record.markers)
        }
        if (record.photos.isNotEmpty()) {
            Spacer(Modifier.height(52.dp))
            RouteePhotoTimelineSection(photos = record.photos, date = record.date)
        }
        if (record.routeNames.isNotEmpty()) {
            Spacer(Modifier.height(if (record.photos.isEmpty()) 52.dp else 48.dp))
            RoutePointItem(
                points = record.routeNames,
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16),
            )
        } else if (record.photos.isEmpty()) {
            Spacer(Modifier.height(22.dp))
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = RouteeTheme.spacing.Spacing10),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.archive_detail_route_empty),
                    style = RouteeTheme.typography.labelM14,
                    color = RouteeTheme.colors.grey200,
                )
            }
        }
        Spacer(Modifier.height(if (record.routeNames.isEmpty()) 36.dp else 100.dp))
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveDetailScreenPreview() {
    RouteePreviewTheme {
        var expanded by rememberSaveable { mutableStateOf(false) }
        ArchiveDetailScreen(
            uiState = ArchiveDetailUiState(
                UiState.Success(
                    ArchiveDetailRecord(
                        title = "숭실대 동기모임 북악산",
                        date = "2026.03.12",
                        distance = "15.53",
                        duration = "03:20",
                        maxElevation = "2132",
                        photos = persistentListOf(RouteeTimelinePhoto(null, "창의문"), RouteeTimelinePhoto(null, null)),
                        routeNames = persistentListOf("창의문", "청운대", "말바위", "백숙 먹으러 가는 중"),
                    ),
                ),
            ),
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onBackClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveDetailScreenEmptyPreview() {
    RouteePreviewTheme {
        ArchiveDetailScreen(
            uiState = ArchiveDetailUiState(
                UiState.Success(ArchiveDetailRecord("북악산 산행", "2026.03.12", "15.53", "03:20", "2132")),
            ),
            expanded = false,
            onExpandedChange = {},
            onBackClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveDetailScreenWithoutPhotosPreview() {
    RouteePreviewTheme {
        ArchiveDetailScreen(
            uiState = ArchiveDetailUiState(
                UiState.Success(
                    ArchiveDetailRecord(
                        title = "북악산 산행",
                        date = "2026.03.12",
                        distance = "15.53",
                        duration = "03:20",
                        maxElevation = "2132",
                        routeNames = persistentListOf("창의문", "청운대", "말바위"),
                    ),
                ),
            ),
            expanded = false,
            onExpandedChange = {},
            onBackClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveDetailScreenFailurePreview() {
    RouteePreviewTheme {
        ArchiveDetailScreen(
            uiState = ArchiveDetailUiState(UiState.Failure(IllegalStateException())),
            expanded = false,
            onExpandedChange = {},
            onBackClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveDetailScreenWithoutRoutesPreview() {
    RouteePreviewTheme {
        ArchiveDetailScreen(
            uiState = ArchiveDetailUiState(
                UiState.Success(
                    ArchiveDetailRecord(
                        title = "북악산 산행",
                        date = "2026.03.12",
                        distance = "15.53",
                        duration = "03:20",
                        maxElevation = "2132",
                        photos = persistentListOf(RouteeTimelinePhoto(null, "창의문")),
                    ),
                ),
            ),
            expanded = false,
            onExpandedChange = {},
            onBackClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveDetailScreenLoadingPreview() {
    val loadingState = remember { RouteeLoadingState() }
    CompositionLocalProvider(LocalRouteeLoadingState provides loadingState) {
        RouteePreviewTheme {
            Box(Modifier.fillMaxSize()) {
                ArchiveDetailScreen(
                    uiState = ArchiveDetailUiState(),
                    expanded = false,
                    onExpandedChange = {},
                    onBackClick = {},
                )
                RouteeLoadingHost(state = loadingState, showDelayMillis = 0)
            }
        }
    }
}
