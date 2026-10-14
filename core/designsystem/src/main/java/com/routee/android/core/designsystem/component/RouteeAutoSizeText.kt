package com.routee.android.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

/**
 * 넘치면 글자를 줄여 한 줄에 맞추는 텍스트
 *
 * 잘리면 안 되는 숫자나 짧은 라벨에 쓴다. 잘려도 되는 문구는 `Text(maxLines = 1, overflow = TextOverflow.Ellipsis)`을 쓴다
 *
 * @param minFontSize 해당 크기까지 줄인다. 기본은 [style] 크기의 70%
 */
@Composable
fun RouteeAutoSizeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    minFontSize: TextUnit = style.fontSize * 0.7f,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(minFontSize = minFontSize, maxFontSize = style.fontSize),
    )
}

@PreviewFontSizes
@Composable
private fun RouteeAutoSizeTextPreview() {
    RouteePreviewTheme {
        Column {
            RouteeAutoSizeText(
                text = "03h 03m",
                style = RouteeTheme.typography.display52,
                color = RouteeTheme.colors.white,
            )
            RouteeAutoSizeText(
                text = "03h 03m",
                style = RouteeTheme.typography.display52,
                color = RouteeTheme.colors.white,
                modifier = Modifier.width(120.dp),
            )
        }
    }
}
