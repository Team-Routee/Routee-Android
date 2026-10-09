package com.routee.android.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.theme.OvalRadialGradient
import com.routee.android.core.designsystem.theme.RouteeTheme

// bg_ellipse
private val EllipseBrush = OvalRadialGradient(listOf(Color(0x14B0F5FA), Color(0x00B0F5FA)))
private const val BASE_SCREEN_WIDTH = 360f
private const val ELLIPSE_WIDTH = 433f
private const val ELLIPSE_HEIGHT = 568f
private const val ELLIPSE_OFFSET_Y = -103f

@Composable
fun RouteeBgEllipse(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(RouteeTheme.colors.bgPrimary),
    ) {
        val scale = maxWidth.value / BASE_SCREEN_WIDTH
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (ELLIPSE_OFFSET_Y * scale).dp)
                .requiredSize(width = (ELLIPSE_WIDTH * scale).dp, height = (ELLIPSE_HEIGHT * scale).dp)
                .background(EllipseBrush),
        )
        content()
    }
}

@Preview(name = "RouteeBgEllipse", widthDp = 360, heightDp = 800)
@Composable
private fun RouteeBgEllipsePreview() {
    RouteeTheme {
        RouteeBgEllipse {
            Text(
                text = "서비스 이용 약관에 동의해 주세요.",
                style = RouteeTheme.typography.titleSb20,
                color = RouteeTheme.colors.white,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 197.dp),
            )
        }
    }
}
