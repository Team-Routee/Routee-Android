package com.routee.android.feature.workout.component.finish

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.RouteePhotoTimelineSection
import com.routee.android.core.designsystem.component.RouteeWorkoutMetricCard
import com.routee.android.core.designsystem.component.skeleton.skeleton
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TitleSkeletonHeight = 28.dp
private val MapSkeletonHeight = 480.dp
private val SectionTopSpacing = 52.dp

// TODO: 저장을 화면 밖 업로더로 옮기면 지움 (타임라인이 기기 데이터로 바로 그림)
@Composable
internal fun WorkoutSavingSkeleton(
    distanceMeters: Int,
    durationSec: Int,
    maxElevationMeters: Int,
    hasPhotos: Boolean,
    modifier: Modifier = Modifier,
) {
    val date = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) }
    RouteeBgEllipse(modifier = modifier.fillMaxSize().pointerInput(Unit) {}) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            RouteePageNavBar(onBackClick = {}, modifier = Modifier.fillMaxWidth())
            Column(modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16)) {
                Box(
                    modifier = Modifier
                        .padding(vertical = RouteeTheme.spacing.Spacing8)
                        .fillMaxWidth(0.8f)
                        .height(TitleSkeletonHeight)
                        .skeleton(),
                )
                Spacer(Modifier.height(RouteeTheme.spacing.Spacing8))
                RouteeWorkoutMetricCard(
                    distance = String.format(Locale.US, "%.2f", distanceMeters / 1000.0),
                    duration = String.format(Locale.US, "%02d:%02d", durationSec / 3600, durationSec / 60 % 60),
                    maxElevation = maxElevationMeters.toString(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(RouteeTheme.spacing.Spacing8))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MapSkeletonHeight)
                        .skeleton(RoundedCornerShape(RouteeTheme.radius.Radius12)),
                )
            }
            if (hasPhotos) {
                Spacer(Modifier.height(SectionTopSpacing))
                RouteePhotoTimelineSection(photos = emptyList(), date = date, isLoading = true)
            }
        }
    }
}

@PreviewDeviceSizes
@Composable
private fun WorkoutSavingSkeletonPreview() {
    RouteeTheme {
        WorkoutSavingSkeleton(
            distanceMeters = 15_530,
            durationSec = 12_000,
            maxElevationMeters = 2132,
            hasPhotos = true,
        )
    }
}
