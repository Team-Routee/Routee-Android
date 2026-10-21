package com.routee.android.feature.recordedit.editor

import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.recap.model.RecapResource
import com.routee.android.core.data.recap.model.RecapRoute
import com.routee.android.core.data.recap.model.TrackPoint
import com.routee.android.core.domain.recap.GetRecapEditorDataUseCase
import com.routee.android.core.ui.UiState
import com.routee.android.feature.recordedit.FakeRecapRepository
import com.routee.android.feature.recordedit.MainDispatcherRule
import com.routee.android.feature.recordedit.api.RecapEditorNavKey
import com.routee.android.feature.recordedit.complete.FakeRecapImageRepository
import com.routee.android.feature.recordedit.editor.model.RecapBrightness
import com.routee.android.feature.recordedit.editor.model.RecapEditorContent
import com.routee.android.feature.recordedit.editor.model.RecapEditorSideEffect
import com.routee.android.feature.recordedit.editor.model.RecapEditorUiState
import com.routee.android.feature.recordedit.editor.model.RecapSticker
import com.routee.android.feature.recordedit.editor.model.StickerFrame
import com.routee.android.feature.recordedit.model.RecapColor
import com.routee.android.feature.recordedit.model.RecapEditTab
import com.routee.android.feature.recordedit.model.StickerType
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RecapEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val recapRepository = FakeRecapRepository()
    private val viewModel by lazy { createViewModel() }
    private val state get() = viewModel.uiState.value

    /**
     * GIVEN: 색을 바꿨다가
     * WHEN: 처음 색으로 되돌리면
     * THEN: 변경 사항이 없다
     */
    @Test
    fun `처음 상태로 되돌리면 변경 사항이 없다`() {
        viewModel.onColorClick(RecapColor.ORANGE)
        assertTrue(state.hasChanges)

        viewModel.onColorClick(RecapColor.DEFAULT)

        assertFalse(state.hasChanges)
    }

    /**
     * GIVEN: 처음 스티커를
     * WHEN: 탭해서 선택하면
     * THEN: 맨 위로 올라오지만 변경 사항은 아니다
     */
    @Test
    fun `스티커를 선택하면 맨 위로 올라오고 변경 사항은 아니다`() {
        viewModel.onStickerClick(StickerType.WORKOUT_RECORD)

        assertEquals(StickerType.WORKOUT_RECORD, state.stickers.last().type)
        assertEquals(StickerType.WORKOUT_RECORD, state.selectedSticker)
        assertFalse(state.hasChanges)
    }

    /**
     * GIVEN: 기록 스티커를 삭제한 뒤
     * WHEN: 서브탭에서 다시 추가하면
     * THEN: 가운데에 맨 위로 놓이고 선택된다
     */
    @Test
    fun `삭제 후 다시 추가하면 가운데 맨 위에 놓인다`() {
        viewModel.onStickerDeleteClick(StickerType.WORKOUT_RECORD)
        assertTrue(state.stickers.none { it.type == StickerType.WORKOUT_RECORD })
        assertTrue(state.hasChanges)

        viewModel.onStickerItemClick(StickerType.WORKOUT_RECORD)

        assertEquals(RecapSticker(StickerType.WORKOUT_RECORD, isCentered = true), state.stickers.last())
        assertEquals(StickerType.WORKOUT_RECORD, state.selectedSticker)
    }

    /**
     * GIVEN: 스티커가 선택된 상태에서
     * WHEN: 스티커 탭을 열면
     * THEN: 스티커 선택이 풀린다
     */
    @Test
    fun `스티커 탭을 열면 스티커 선택이 풀린다`() {
        viewModel.onStickerClick(StickerType.PHOTO_TIMELINE)

        viewModel.onTabClick(RecapEditTab.STICKER)

        assertEquals(RecapEditTab.STICKER, state.selectedTab)
        assertNull(state.selectedSticker)
    }

    /**
     * GIVEN: 스티커가 선택된 상태에서
     * WHEN: 밝기 탭으로 바꾸면
     * THEN: 스티커 선택이 유지된다
     */
    @Test
    fun `스티커 탭이 아닌 탭끼리 바꾸면 스티커 선택이 유지된다`() {
        viewModel.onTabClick(RecapEditTab.COLOR)
        viewModel.onStickerClick(StickerType.PHOTO_TIMELINE)

        viewModel.onTabClick(RecapEditTab.BRIGHTNESS)

        assertEquals(StickerType.PHOTO_TIMELINE, state.selectedSticker)
    }

    /**
     * GIVEN: 스티커를 옮기고 밝기와 색을 바꾼 뒤
     * WHEN: 초기화를 확인하면
     * THEN: 처음 상태로 돌아간다
     */
    @Test
    fun `초기화하면 처음 상태로 돌아간다`() {
        viewModel.onStickerFrameChange(StickerType.WORKOUT_RECORD, StickerFrame(x = 10f, y = 10f, scale = 2f))
        viewModel.onBrightnessChange(0.2f)
        viewModel.onColorClick(RecapColor.ORANGE)
        viewModel.onResetClick()
        assertTrue(state.isResetDialogVisible)

        viewModel.onResetConfirm()

        assertEquals(RecapEditorUiState(content = state.content), state)
        assertFalse(state.hasChanges)
    }

    /**
     * GIVEN: 밝기를 바꾼 뒤
     * WHEN: 배경을 사진으로 바꾸면
     * THEN: 밝기는 초기화되고 사진 기본 투명도(40%)가 적용되며 탭이 닫힌다
     */
    @Test
    fun `배경을 바꾸면 밝기가 초기화되고 사진 기본 투명도가 적용된다`() {
        viewModel.onBrightnessChange(0.2f)

        viewModel.onBackgroundCropComplete("content://background")

        assertEquals("content://background", state.backgroundUri)
        assertEquals(RecapBrightness.PHOTO_BASE_OPACITY, state.baseOpacity)
        assertEquals(RecapBrightness.DEFAULT_SLIDER_VALUE, state.brightness)
        assertNull(state.selectedTab)
        assertTrue(state.hasChanges)
    }

    /**
     * GIVEN: 스티커를 옮긴 뒤
     * WHEN: 배경을 바꾸면
     * THEN: 스티커 위치는 그대로다
     */
    @Test
    fun `배경을 바꿔도 스티커 위치는 유지된다`() {
        val frame = StickerFrame(x = 10f, y = 10f, scale = 2f)
        viewModel.onStickerFrameChange(StickerType.WORKOUT_RECORD, frame)

        viewModel.onBackgroundCropComplete("content://background")

        assertEquals(frame, state.stickers.first { it.type == StickerType.WORKOUT_RECORD }.frame)
    }

    /**
     * GIVEN: 배경을 사진으로 바꾼 뒤
     * WHEN: 초기화하면
     * THEN: 지도 배경과 지도 기본 투명도로 돌아간다
     */
    @Test
    fun `초기화하면 지도 배경으로 돌아간다`() {
        viewModel.onBackgroundCropComplete("content://background")

        viewModel.onResetConfirm()

        assertNull(state.backgroundUri)
        assertEquals(RecapBrightness.MAP_BASE_OPACITY, state.baseOpacity)
    }

    /**
     * GIVEN: 변경 사항이 없을 때
     * WHEN: 초기화 버튼을 누르면
     * THEN: 모달을 띄우지 않는다
     */
    @Test
    fun `변경 사항이 없으면 초기화 모달을 띄우지 않는다`() {
        viewModel.onResetClick()

        assertFalse(state.isResetDialogVisible)
    }

    /**
     * GIVEN: 탭이 열려 있고 스티커가 선택된 상태에서
     * WHEN: 완료를 누르면
     * THEN: 탭과 선택을 닫고 미리보기 캡처를 요청한다
     */
    @Test
    fun `완료를 누르면 선택을 풀고 캡처를 요청한다`() = runTest {
        viewModel.onTabClick(RecapEditTab.COLOR)
        viewModel.onStickerClick(StickerType.WORKOUT_RECORD)

        viewModel.onCompleteClick()

        assertNull(state.selectedTab)
        assertNull(state.selectedSticker)
        assertEquals(RecapEditorSideEffect.CaptureRecap, viewModel.sideEffect.first())
    }

    /**
     * GIVEN: 완료를 누른 뒤
     * WHEN: 완료를 한 번 더 누르면
     * THEN: 캡처를 다시 요청하지 않는다
     */
    @Test
    fun `완료를 두 번 눌러도 캡처는 한 번만 요청한다`() = runTest {
        viewModel.onCompleteClick()
        viewModel.sideEffect.first()

        viewModel.onCompleteClick()
        viewModel.onCompleteAnimationEnd()

        assertFalse(state.isCompleteAnimationVisible)
    }

    @Test
    fun `편집 데이터를 불러오면 루트는 순번대로, 경로는 좌표 기록 순서대로 보여준다`() {
        recapRepository.resource = RecapResource(
            distanceMeters = 15_500,
            durationSeconds = 12_000,
            maxElevationMeters = 2132,
            mapImageUrl = "https://example.com/map.png",
            routes = listOf(RecapRoute(sequence = 2, name = "백운대"), RecapRoute(sequence = 1, name = "북한산성")),
        )
        recapRepository.track = ActivityTrack(
            trackPoints = listOf(trackPoint(index = 2, latitude = 37.2), trackPoint(index = 1, latitude = 37.1)),
            timelineMarkers = emptyList(),
        )

        val content = (state.content as UiState.Success<RecapEditorContent>).data

        assertEquals(listOf("북한산성", "백운대"), content.routeNames)
        assertEquals(listOf(37.1, 37.2), content.track.trackPoints.map { it.latitude })
        assertEquals("15.50km", content.record.distance)
        assertEquals("https://example.com/map.png", content.mapImageUrl)
    }

    @Test
    fun `편집 데이터를 못 불러오면 실패 상태고 다시 시도하면 불러온다`() {
        recapRepository.editorError = IOException()
        assertTrue(state.content is UiState.Failure)

        recapRepository.editorError = null
        viewModel.onRetryClick()

        assertTrue(state.content is UiState.Success)
    }

    private fun createViewModel() = RecapEditorViewModel(
        RecapEditorNavKey(activityId = 1L),
        FakeRecapImageRepository(),
        GetRecapEditorDataUseCase(recapRepository),
    )

    private fun trackPoint(
        index: Int,
        latitude: Double,
    ) = TrackPoint(latitude = latitude, longitude = 127.0, elevation = 0.0, pointIndex = index)
}
