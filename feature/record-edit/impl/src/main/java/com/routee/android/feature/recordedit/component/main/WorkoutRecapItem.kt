package com.routee.android.feature.recordedit.component.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.model.RecapItemUiModel
import kotlinx.collections.immutable.toImmutableList

/**
 * 기록 편집 메인의 기록 카드 (workout_recap_item)
 *
 * 썸네일을 누르면 기록 편집 화면으로 이동하고, 제목·날짜를 눌러서는 이동하지 않는다
 *
 * @param editingTitleState 제목 편집 중일 때의 입력 상태. null이면 편집 중이 아니다
 * @param onItemClick 썸네일 클릭 시 호출
 * @param onEditTitleClick 제목 옆 연필 아이콘 클릭 시 호출
 * @param onEditTitleComplete 제목 편집 완료 시 호출
 */
@Composable
internal fun WorkoutRecapItem(
    item: RecapItemUiModel,
    editingTitleState: TextFieldState?,
    onItemClick: () -> Unit,
    onEditTitleClick: () -> Unit,
    onEditTitleComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 썸네일-제목 간격(10)과 제목-날짜 간격(2)이 달라, 중첩 Column 대신 각 항목의 top padding으로 처리한다
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RecapThumbnail(
            thumbnailUrls = item.thumbnailUrls,
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable(role = Role.Button, onClick = onItemClick),
        )
        RecapTitleTextField(
            title = item.title,
            editingState = editingTitleState,
            onEditClick = onEditTitleClick,
            onEditComplete = onEditTitleComplete,
            modifier = Modifier
                .padding(top = RouteeTheme.spacing.Spacing10)
                .fillMaxWidth(),
        )
        Text(
            text = item.workoutDate,
            style = RouteeTheme.typography.labelM12,
            color = RouteeTheme.colors.white60,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .padding(top = RouteeTheme.spacing.Spacing2)
                .fillMaxWidth(),
        )
    }
}

private data class WorkoutRecapItemPreviewParam(
    val photoCount: Int,
    val isEditingTitle: Boolean = false,
)

private class WorkoutRecapItemPreviewParameterProvider : PreviewParameterProvider<WorkoutRecapItemPreviewParam> {
    override val values = sequenceOf(
        WorkoutRecapItemPreviewParam(photoCount = 0),
        WorkoutRecapItemPreviewParam(photoCount = 1),
        WorkoutRecapItemPreviewParam(photoCount = 2),
        WorkoutRecapItemPreviewParam(photoCount = 3),
        WorkoutRecapItemPreviewParam(photoCount = 4),
        WorkoutRecapItemPreviewParam(photoCount = 4, isEditingTitle = true),
    )
}

@Preview(widthDp = 144)
@Composable
private fun WorkoutRecapItemPreview(
    @PreviewParameter(WorkoutRecapItemPreviewParameterProvider::class) param: WorkoutRecapItemPreviewParam,
) {
    val item = RecapItemUiModel(
        activityId = 1L,
        title = "2026.03.23 기록",
        workoutDate = "2026.03.23.",
        thumbnailUrls = List(param.photoCount) { index -> "https://example.com/photo_$index.jpg" }.toImmutableList(),
    )
    val editingTitleState = rememberTextFieldState(item.title)

    RouteePreviewTheme {
        WorkoutRecapItem(
            item = item,
            editingTitleState = if (param.isEditingTitle) editingTitleState else null,
            onItemClick = {},
            onEditTitleClick = {},
            onEditTitleComplete = {},
        )
    }
}
