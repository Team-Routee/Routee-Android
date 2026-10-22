package com.routee.android.feature.archive.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeAsyncImage
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.archive.R

@Composable
internal fun UserProfile(
    nickname: String,
    daysTogether: Int,
    workoutCount: Int,
    modifier: Modifier = Modifier,
    profileImageUrl: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing14),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val profileImageModifier = Modifier
                .size(74.dp)
                .clip(CircleShape)

            if (profileImageUrl == null) {
                Image(
                    painter = painterResource(DesignSystemR.drawable.img_profile_default),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = profileImageModifier,
                )
            } else {
                RouteeAsyncImage(
                    url = profileImageUrl,
                    contentDescription = null,
                    modifier = profileImageModifier,
                    errorImage = painterResource(DesignSystemR.drawable.img_profile_default),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8),
            ) {
                Text(
                    text = nickname,
                    color = RouteeTheme.colors.white,
                    style = RouteeTheme.typography.labelSb18,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_fire_sm_gradient),
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(R.string.user_profile_days_together, daysTogether),
                        color = RouteeTheme.colors.mint300,
                        style = RouteeTheme.typography.labelM14,
                        maxLines = 1,
                    )
                }
            }
        }

        TotalWorkoutCountChip(
            workoutCount = workoutCount,
            modifier = Modifier.padding(vertical = RouteeTheme.spacing.Spacing8),
        )
    }
}

@Preview
@Composable
private fun UserProfileEmptyPreview() {
    RouteeTheme {
        UserProfile(
            nickname = "관악산 날다람쥐",
            daysTogether = 8,
            workoutCount = 25,
        )
    }
}
