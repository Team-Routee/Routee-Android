package com.routee.android.feature.recordedit.component.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.R

private val TickTopPadding = 13.dp
private val TickHeight = 12.dp
private val TickSpacing = 8.dp
private val TickWidth = 2.dp
private val IndicatorHeight = 26.dp
private val IndicatorWidth = 2.dp
private val SideFadeWidth = 40.dp
private const val ACCENT_TICK_INTERVAL = 10
private const val MIN_VALUE = 0f
private const val MAX_VALUE = 1f

/**
 * 기록 편집 배경 밝기 슬라이더 (brightness_slider)
 *
 * 가운데 표시선은 고정되고, 좌우로 드래그하면 눈금이 움직이며 값이 바뀐다
 * 양 끝(0, 1)에서는 더 넘어가지 않는다
 *
 * @param value 밝기 값 (0 = 가장 어둡게, 1 = 어둡게 처리 없음)
 * @param onValueChange 드래그로 바뀐 값. 0..1 범위로 제한된다
 */
@Composable
internal fun BrightnessSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentValue by rememberUpdatedState(value.coerceIn(MIN_VALUE, MAX_VALUE))
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val sliderDescription = stringResource(R.string.record_edit_editor_brightness_slider_a11y)
    val colors = RouteeTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .background(colors.black80)
            .sideFade(color = colors.black, width = SideFadeWidth)
            .brightnessDrag(value = { currentValue }, onValueChange = { currentOnValueChange(it) })
            .brightnessSemantics(
                value = currentValue,
                description = sliderDescription,
                onValueChange = currentOnValueChange,
            ),
    ) {
        BrightnessTicks(
            value = currentValue,
            accentTickColor = colors.white,
            tickColor = colors.grey400,
        )
        CenterIndicator(
            color = colors.white,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun BrightnessTicks(
    value: Float,
    accentTickColor: Color,
    tickColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = TickTopPadding)
            .height(TickHeight),
    ) {
        val spacingPx = TickSpacing.toPx()
        val strokeWidthPx = TickWidth.toPx()
        val rangeWidth = tickRangeWidth(size.width, spacingPx, strokeWidthPx)
        val tickCount = (rangeWidth / spacingPx).toInt()
        val firstTickX = (size.width - rangeWidth) / 2 + (0.5f - value) * rangeWidth
        val halfStroke = strokeWidthPx / 2
        for (index in 0..tickCount) {
            val x = firstTickX + index * spacingPx
            drawLine(
                color = if (index % ACCENT_TICK_INTERVAL == 0) accentTickColor else tickColor,
                start = Offset(x, halfStroke),
                end = Offset(x, size.height - halfStroke),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun CenterIndicator(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(bottom = RouteeTheme.spacing.Spacing16)
            .width(IndicatorWidth)
            .height(IndicatorHeight)
            .clip(RoundedCornerShape(IndicatorWidth / 2))
            .background(color),
    )
}

/**
 * 좌우 끝을 [color]에서 투명으로 흐리게 덮는다
 */
private fun Modifier.sideFade(
    color: Color,
    width: Dp,
): Modifier = drawWithCache {
    val fadeWidthPx = width.toPx()
    val fadeSize = Size(fadeWidthPx, size.height)
    val startFadeBrush = Brush.horizontalGradient(
        colors = listOf(color, Color.Transparent),
        startX = 0f,
        endX = fadeWidthPx,
    )
    val endFadeBrush = Brush.horizontalGradient(
        colors = listOf(Color.Transparent, color),
        startX = size.width - fadeWidthPx,
        endX = size.width,
    )
    onDrawWithContent {
        drawContent()
        drawRect(brush = startFadeBrush, size = fadeSize)
        drawRect(brush = endFadeBrush, topLeft = Offset(size.width - fadeWidthPx, 0f), size = fadeSize)
    }
}

/**
 * 가로 드래그 이동량을 눈금 폭 비율로 환산해 값을 바꾼다
 *
 * @param value 드래그 시작 시점의 값을 읽는 함수
 */
private fun Modifier.brightnessDrag(
    value: () -> Float,
    onValueChange: (Float) -> Unit,
): Modifier = pointerInput(Unit) {
    var draggingValue = value()
    detectHorizontalDragGestures(
        onDragStart = { draggingValue = value() },
    ) { change, dragAmount ->
        val rangeWidth = tickRangeWidth(size.width.toFloat(), TickSpacing.toPx(), TickWidth.toPx())
        if (rangeWidth <= 0f) return@detectHorizontalDragGestures
        change.consume()
        val newValue = (draggingValue + dragAmount / rangeWidth).coerceIn(MIN_VALUE, MAX_VALUE)
        if (newValue != draggingValue) {
            draggingValue = newValue
            onValueChange(newValue)
        }
    }
}

private fun Modifier.brightnessSemantics(
    value: Float,
    description: String,
    onValueChange: (Float) -> Unit,
): Modifier = semantics {
    contentDescription = description
    progressBarRangeInfo = ProgressBarRangeInfo(value, MIN_VALUE..MAX_VALUE)
    setProgress { target ->
        onValueChange(target.coerceIn(MIN_VALUE, MAX_VALUE))
        true
    }
}

/**
 * 눈금이 그려지는 전체 폭. 값 0..1이 이 폭만큼의 드래그에 대응한다
 */
private fun tickRangeWidth(
    width: Float,
    spacingPx: Float,
    strokeWidthPx: Float,
): Float {
    if (spacingPx <= 0f) return 0f
    val tickCount = ((width - strokeWidthPx) / spacingPx).toInt().coerceAtLeast(0)
    return tickCount * spacingPx
}

private class BrightnessSliderPreviewParameterProvider : PreviewParameterProvider<Float> {
    override val values = sequenceOf(0f, 0.5f, 1f)
}

@PreviewFontSizes
@Composable
private fun BrightnessSliderPreview(
    @PreviewParameter(BrightnessSliderPreviewParameterProvider::class) value: Float,
) {
    RouteePreviewTheme {
        BrightnessSlider(
            value = value,
            onValueChange = {},
        )
    }
}
