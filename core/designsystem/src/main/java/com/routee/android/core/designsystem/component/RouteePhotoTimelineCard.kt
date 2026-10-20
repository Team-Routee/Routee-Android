package com.routee.android.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme

/**
 * 위치와 저장 버튼을 겹쳐 표시하는 정사각형 사진 카드
 *
 * @param imageUrl 표시할 사진 주소. null이면 기본 배경을 표시한다.
 * @param location 사진 위치. 없으면 위치 라벨을 숨긴다.
 * @param isSaved 사진 저장 완료 여부
 * @param onSaveClick 저장 버튼 클릭 콜백. null이면 버튼을 숨긴다.
 */
@Composable
fun RouteePhotoTimelineCard(
    imageUrl: String?,
    location: String?,
    modifier: Modifier = Modifier,
    isSaved: Boolean = false,
    onSaveClick: (() -> Unit)? = null,
) {
    Box(modifier = modifier.aspectRatio(1f)) {
        RouteeAsyncImage(
            url = imageUrl,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(
                    start = RouteeTheme.spacing.Spacing12,
                    top = RouteeTheme.spacing.Spacing12,
                    end = if (onSaveClick != null) RouteeTheme.spacing.Spacing16 else RouteeTheme.spacing.Spacing12,
                ),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            if (!location.isNullOrBlank()) {
                RouteeLocationInfo(
                    location = location,
                    modifier = Modifier.weight(1f, fill = false),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (onSaveClick != null) {
                RouteePhotoSaveButton(
                    isSaved = isSaved,
                    onClick = onSaveClick,
                    modifier = Modifier.padding(start = RouteeTheme.spacing.Spacing8),
                )
            }
        }
    }
}

@PreviewFontSizes
@Composable
private fun RouteePhotoTimelineCardPreview() {
    RouteeTheme {
        Column {
            RouteePhotoTimelineCard(
                imageUrl = null,
                location = "창의문",
                modifier = Modifier.size(360.dp),
            )
            RouteePhotoTimelineCard(
                imageUrl = null,
                location = "창의문",
                modifier = Modifier.size(360.dp),
                onSaveClick = {},
            )
            RouteePhotoTimelineCard(
                imageUrl = null,
                location = null,
                modifier = Modifier.size(360.dp),
                onSaveClick = {},
            )
        }
    }
}
