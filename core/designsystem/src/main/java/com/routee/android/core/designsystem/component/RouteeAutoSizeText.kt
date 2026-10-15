package com.routee.android.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

private val DefaultFontSize = 14.sp
private val DefaultMinFontSize = 12.sp
private val FontSizeStep = 1.sp

/**
 * 넘치면 글자를 줄여 한 줄에 맞추는 텍스트
 *
 * 잘리면 안 되는 숫자나 짧은 라벨에 쓴다. 잘려도 되는 문구는 `Text(maxLines = 1, overflow = TextOverflow.Ellipsis)`을 쓴다
 *
 * @param minFontSize 이 크기까지 줄인다. 기본 12sp
 */
@Composable
fun RouteeAutoSizeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    minFontSize: TextUnit = DefaultMinFontSize,
) {
    val mergedStyle = LocalTextStyle.current.merge(style)
    val maxFontSize = mergedStyle.fontSize.takeOrElse { DefaultFontSize }

    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = mergedStyle,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(
            minFontSize = if (minFontSize > maxFontSize) maxFontSize else minFontSize, // 12sp보다 작은 스타일이면 줄이지 않음
            maxFontSize = maxFontSize,
            stepSize = FontSizeStep,
        ),
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
