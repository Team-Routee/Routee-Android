package com.routee.android.core.designsystem.component.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

// toast

/**
 * 토스트
 *
 * 길면 여러 줄로 보여준다. 띄우기는 [RouteeMessageState]로 한다
 */
@Composable
fun RouteeToast(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = RouteeTheme.typography.labelM14,
        color = RouteeTheme.colors.white,
        textAlign = TextAlign.Center,
        modifier = modifier
            .background(
                color = RouteeTheme.colors.grey800,
                shape = RoundedCornerShape(RouteeTheme.radius.Radius12),
            )
            .padding(horizontal = RouteeTheme.spacing.Spacing16, vertical = RouteeTheme.spacing.Spacing10),
    )
}

@PreviewFontSizes
@Composable
private fun RouteeToastPreview() {
    RouteePreviewTheme(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RouteeToast(text = "기록이 삭제되었습니다.")
            RouteeToast(text = "한 번 더 누르면 앱이 닫히며,\n운동 기록은 계속됩니다.")
        }
    }
}
