package com.routee.android.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme

/** 현재 사진 위치를 최대 다섯 개의 점으로 표시하는 인디케이터 */
@Composable
fun RouteePaginationDots(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    if (pageCount <= 1) return
    val selectedPage = currentPage.coerceIn(0, pageCount - 1)
    val dotCount = pageCount.coerceAtMost(5)
    val selectedDot = selectedPage.coerceAtMost(2).coerceAtLeast(selectedPage - (pageCount - dotCount))
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing6),
    ) {
        repeat(dotCount) { index ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (index == selectedDot) RouteeTheme.colors.white else RouteeTheme.colors.grey400,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@PreviewFontSizes
@Composable
private fun RouteePaginationDotsPreview() {
    RouteeTheme {
        Column(verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12)) {
            RouteePaginationDots(pageCount = 5, currentPage = 0)
            RouteePaginationDots(pageCount = 5, currentPage = 2)
            RouteePaginationDots(pageCount = 5, currentPage = 4)
            RouteePaginationDots(pageCount = 8, currentPage = 3)
        }
    }
}
