package com.routee.android.feature.archive

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteePageHeader
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.archive.component.UserProfile

private data class ArchiveProfile(
    val nickname: String,
    val daysTogether: Int,
    val workoutCount: Int,
    val imageUrl: String? = null,
)

@Composable
internal fun ArchiveRoute() {
    val profile = remember {
        ArchiveProfile(
            nickname = "관악산 날다람쥐",
            daysTogether = 8,
            workoutCount = 25,
        )
    }

    ArchiveScreen(profile = profile)
}

@Composable
private fun ArchiveScreen(
    profile: ArchiveProfile,
    modifier: Modifier = Modifier,
) {
    RouteeBgEllipse(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RouteePageHeader(title = stringResource(R.string.archive_title))
            UserProfile(
                nickname = profile.nickname,
                daysTogether = profile.daysTogether,
                workoutCount = profile.workoutCount,
                profileImageUrl = profile.imageUrl,
                modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16),
            )
        }
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveScreenPreview() {
    RouteePreviewTheme {
        ArchiveScreen(
            profile = ArchiveProfile(
                nickname = "관악산 날다람쥐",
                daysTogether = 8,
                workoutCount = 25,
            ),
        )
    }
}
