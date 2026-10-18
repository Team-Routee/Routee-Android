package com.routee.android.feature.recordedit.editor.model

import androidx.compose.runtime.Immutable
import com.routee.android.core.ui.UiState
import com.routee.android.feature.recordedit.model.RecapColor
import com.routee.android.feature.recordedit.model.RecapEditTab
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 기록 편집 화면 상태
 *
 * @param content                   편집할 기록. GET /recap, /track 응답
 * @param selectedTab               하단 편집 탭. 처음에는 모두 미선택
 * @param brightness                밝기 슬라이더 값 (0~1)
 * @param baseOpacity               밝기 기본값일 때 배경 위 검은 레이어 투명도 (지도 50%, 사진 40%)
 * @param recapColor                스티커 색
 * @param hasChanges                한 번이라도 편집했는지. 뒤로가기 시 편집 중단 모달 여부
 * @param isEditStopDialogVisible   편집 중단 모달 노출 여부
 */
@Immutable
internal data class RecapEditorUiState(
    val content: UiState<RecapEditorContent> = UiState.Loading,
    val selectedTab: RecapEditTab? = null,
    val brightness: Float = RecapBrightness.DEFAULT_SLIDER_VALUE,
    val baseOpacity: Float = RecapBrightness.MAP_BASE_OPACITY,
    val recapColor: RecapColor = RecapColor.DEFAULT,
    val hasChanges: Boolean = false,
    val isEditStopDialogVisible: Boolean = false,
) {
    val overlayOpacity: Float get() = RecapBrightness.overlayOpacity(brightness, baseOpacity)
}

/**
 * @param mapImageUrl  기본 배경 지도 이미지
 * @param routeNames   루트 스티커 경유지 이름. sequence 순서
 */
@Immutable
internal data class RecapEditorContent(
    val mapImageUrl: String?,
    val record: RecapRecordUiModel,
    val track: RecapTrackUiModel,
    val routeNames: ImmutableList<String>,
) {
    companion object {
        val EMPTY = RecapEditorContent(
            mapImageUrl = null,
            record = RecapRecordUiModel.EMPTY,
            track = RecapTrackUiModel.EMPTY,
            routeNames = persistentListOf(),
        )
    }
}
