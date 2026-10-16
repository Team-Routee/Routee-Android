package com.routee.android.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme

private val MarkerWidth = 20.dp
private val MarkerHeight = 24.dp

/**
 * 번호가 들어간 루트 지점 마커
 *
 * [number]를 마커 가운데 표시한다.
 */
@Composable
fun RouteeRouteMarker(
    number: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(width = MarkerWidth, height = MarkerHeight),
        contentAlignment = Alignment.TopCenter,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_route_marker),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
        )
        Box(modifier = Modifier.size(MarkerWidth), contentAlignment = Alignment.Center) {
            Text(
                text = number.toString(),
                color = RouteeTheme.colors.grey800,
                style = RouteeTheme.typography.labelM12,
            )
        }
    }
}

@PreviewFontSizes
@Composable
private fun RouteeRouteMarkerPreview() {
    RouteeTheme {
        RouteeRouteMarker(number = 1)
    }
}
