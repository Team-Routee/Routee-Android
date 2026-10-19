package com.routee.android.feature.recordedit.component.editor

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.R
import com.routee.android.feature.recordedit.editor.model.RecapRecordUiModel
import com.routee.android.feature.recordedit.model.RecapColor
import com.routee.android.feature.recordedit.model.color

/**
 * 기록 스티커 (workout_record_sticker)
 *
 * @param record  거리, 시간, 최고 고도
 * @param color   색상 팔레트에서 선택한 색
 */
@Composable
internal fun WorkoutRecordSticker(
    record: RecapRecordUiModel,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing10),
    ) {
        RecordItem(labelRes = R.string.record_edit_editor_record_distance, value = record.distance, color = color)
        RecordItem(labelRes = R.string.record_edit_editor_record_duration, value = record.duration, color = color)
        RecordItem(
            labelRes = R.string.record_edit_editor_record_max_elevation,
            value = record.maxElevation,
            color = color,
        )
    }
}

@Composable
private fun RecordItem(
    @StringRes labelRes: Int,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(labelRes),
            style = RouteeTheme.typography.labelSb12,
            color = color,
        )
        Text(
            text = value,
            style = RouteeTheme.typography.displaySb22,
            color = color,
        )
    }
}

private class WorkoutRecordStickerPreviewParameterProvider : PreviewParameterProvider<RecapColor> {
    override val values = sequenceOf(RecapColor.DEFAULT, RecapColor.ORANGE)
}

@PreviewFontSizes
@Composable
private fun WorkoutRecordStickerPreview(
    @PreviewParameter(WorkoutRecordStickerPreviewParameterProvider::class) recapColor: RecapColor,
) {
    RouteePreviewTheme {
        WorkoutRecordSticker(
            record = RecapRecordUiModel.from(
                distanceMeters = 15_500,
                durationSeconds = 12_000,
                maxElevationMeters = 2132,
            ),
            color = recapColor.color,
        )
    }
}
