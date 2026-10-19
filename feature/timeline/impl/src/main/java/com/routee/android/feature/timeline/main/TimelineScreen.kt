package com.routee.android.feature.timeline.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeCtaPrimaryButton
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.RouteeWorkoutMetricCard
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.timeline.R
import com.routee.android.feature.timeline.api.TimelineNavKey
import com.routee.android.feature.timeline.api.TimelinePhoto
import com.routee.android.feature.timeline.main.model.TimelineUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RecordDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val EditIconTouchSize = 44.dp
private val SectionTopSpacing = 52.dp
private val SectionSpacing = 48.dp
private val CtaTopSpacing = 40.dp
private val BottomSpacing = 28.dp

@Composable
internal fun TimelineRoute(
    key: TimelineNavKey,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onEditTitleClick: () -> Unit,
    onRecordEditClick: () -> Unit,
    onPhotoSaveClick: (TimelinePhoto) -> Unit,
    routeMap: (@Composable () -> Unit)? = null,
    myRoute: (@Composable () -> Unit)? = null,
) {
    val recordDate = remember(key.endedAtEpochMillis) {
        Instant.ofEpochMilli(key.endedAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .format(RecordDateFormatter)
    }
    val defaultTitle = stringResource(R.string.timeline_default_record_title, recordDate)
    val uiState = remember(key, defaultTitle) {
        TimelineUiState(
            title = defaultTitle,
            distance = String.format(Locale.US, "%.2f", key.distanceMeters / 1000.0),
            duration = String.format(Locale.US, "%02d:%02d", key.durationSeconds / 3600, key.durationSeconds / 60 % 60),
            maxElevation = key.maxElevationMeters.toString(),
        )
    }
    TimelineScreen(
        uiState = uiState,
        photos = key.photos,
        onBackClick = onBackClick,
        onCompleteClick = onCompleteClick,
        onEditTitleClick = onEditTitleClick,
        onRecordEditClick = onRecordEditClick,
        onPhotoSaveClick = onPhotoSaveClick,
        routeMap = routeMap,
        myRoute = myRoute,
    )
}

@Composable
private fun TimelineScreen(
    uiState: TimelineUiState,
    photos: List<TimelinePhoto>,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onEditTitleClick: () -> Unit,
    onRecordEditClick: () -> Unit,
    onPhotoSaveClick: (TimelinePhoto) -> Unit,
    modifier: Modifier = Modifier,
    routeMap: (@Composable () -> Unit)? = null,
    myRoute: (@Composable () -> Unit)? = null,
) {
    RouteeBgEllipse(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            RouteePageNavBar(
                onBackClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RouteeTheme.colors.bgPrimary),
                actions = {
                    Text(
                        text = stringResource(R.string.timeline_complete),
                        style = RouteeTheme.typography.labelSb18,
                        color = RouteeTheme.colors.white,
                        modifier = Modifier
                            .noRippleClickable(onClick = onCompleteClick)
                            .semantics { role = Role.Button }
                            .padding(
                                horizontal = RouteeTheme.spacing.Spacing16,
                                vertical = RouteeTheme.spacing.Spacing10,
                            ),
                    )
                },
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RouteeTheme.spacing.Spacing16),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = uiState.title,
                            style = RouteeTheme.typography.titleSb20,
                            color = RouteeTheme.colors.white,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Image(
                            imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_edit_sm_line_white),
                            contentDescription = stringResource(R.string.timeline_edit_title),
                            modifier = Modifier
                                .size(EditIconTouchSize)
                                .noRippleClickable(onClick = onEditTitleClick)
                                .semantics { role = Role.Button }
                                .padding(RouteeTheme.spacing.Spacing10),
                        )
                    }
                    Spacer(Modifier.height(RouteeTheme.spacing.Spacing8))
                    RouteeWorkoutMetricCard(
                        distance = uiState.distance,
                        duration = uiState.duration,
                        maxElevation = uiState.maxElevation,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (routeMap != null) {
                        Spacer(Modifier.height(RouteeTheme.spacing.Spacing8))
                        routeMap()
                    }
                }
                if (photos.isNotEmpty() || myRoute != null) {
                    Spacer(Modifier.height(SectionTopSpacing))
                }
                if (photos.isNotEmpty()) {
                    TimelinePhotoSection(photos = photos, onSaveClick = onPhotoSaveClick)
                }
                if (photos.isNotEmpty() && myRoute != null) {
                    Spacer(Modifier.height(SectionSpacing))
                }
                if (myRoute != null) {
                    Column(modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16)) {
                        myRoute()
                    }
                }
                Spacer(Modifier.height(CtaTopSpacing))
                RouteeCtaPrimaryButton(
                    text = stringResource(R.string.timeline_edit_record),
                    onClick = onRecordEditClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RouteeTheme.spacing.Spacing16),
                )
                Spacer(Modifier.height(BottomSpacing))
            }
        }
    }
}

@PreviewDeviceSizes
@Composable
private fun TimelineScreenPreview() {
    RouteeTheme {
        TimelineScreen(
            uiState = TimelineUiState(
                title = "2026.07.18 기록",
                distance = "15.53",
                duration = "03:20",
                maxElevation = "2132",
            ),
            photos = emptyList(),
            onBackClick = {},
            onCompleteClick = {},
            onEditTitleClick = {},
            onRecordEditClick = {},
            onPhotoSaveClick = {},
        )
    }
}
