package com.routee.android.feature.workout.recording

import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.domain.workout.SaveFinishedWorkoutUseCase
import com.routee.android.core.map.model.MapPoint
import com.routee.android.feature.workout.FakeLocationRepository
import com.routee.android.feature.workout.FakeMapImageRepository
import com.routee.android.feature.workout.FakeWorkoutRecordRepository
import com.routee.android.feature.workout.FakeWorkoutRemoteRepository
import com.routee.android.feature.workout.FakeWorkoutTrackingRepository
import com.routee.android.feature.workout.MainDispatcherRule
import com.routee.android.feature.workout.START
import com.routee.android.feature.workout.TestClock
import com.routee.android.feature.workout.record
import com.routee.android.feature.workout.recording.model.WorkoutMode
import com.routee.android.feature.workout.recording.model.WorkoutRecordingSideEffect
import com.routee.android.feature.workout.recording.model.WorkoutSummary
import com.routee.android.feature.workout.trackPoint
import java.io.IOException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WorkoutRecordingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = TestClock()

    /**
     * GIVEN: 시작 API가 성공할 때
     * WHEN: 카운트다운이 끝나면
     * THEN: 서버가 준 id로 기기 기록을 시작하고 기록 중으로 바뀌며 위치 기록 서비스를 켠다
     */
    @Test
    fun `카운트다운이 끝나면 기록을 시작하고 서비스를 켠다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository()
        val viewModel = viewModel(tracking)

        viewModel.onCountdownFinish()

        assertEquals(1L, tracking.startedRecord?.activityId)
        assertEquals(WorkoutMode.RECORDING, viewModel.uiState.value.mode)
        assertEquals(WorkoutRecordingSideEffect.StartTrackingService, viewModel.sideEffect.first())
        viewModel.close()
    }

    /**
     * GIVEN: 시작 API가 실패할 때
     * WHEN: 카운트다운 중이면
     * THEN: 카운트다운 끝을 기다리지 않고 바로 시작 실패를 알리고, 카운트다운이 끝나도 기록을 시작하지 않는다
     */
    @Test
    fun `시작 API가 실패하면 카운트다운 중에 바로 알린다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository()
        val viewModel = viewModel(tracking, FakeWorkoutRemoteRepository(createError = IOException()))

        assertEquals(WorkoutRecordingSideEffect.StartFailed, viewModel.sideEffect.first())

        viewModel.onCountdownFinish()

        assertNull(tracking.startedRecord)
        assertEquals(WorkoutMode.COUNTDOWN, viewModel.uiState.value.mode)
        assertNull(withTimeoutOrNull(100) { viewModel.sideEffect.first() })
        viewModel.close()
    }

    /**
     * GIVEN: 기기 기록 복원이 실패할 때
     * WHEN: 화면에 들어와 카운트다운이 끝나면
     * THEN: 복원 실패를 알리고 새 기록을 시작한다
     */
    @Test
    fun `기기 기록 복원이 실패하면 알리고 새로 시작한다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository(restoreError = IOException())
        val viewModel = viewModel(tracking)

        assertEquals(WorkoutRecordingSideEffect.ShowRestoreFailed, viewModel.sideEffect.first())

        viewModel.onCountdownFinish()

        assertEquals(1L, tracking.startedRecord?.activityId)
        assertEquals(WorkoutMode.RECORDING, viewModel.uiState.value.mode)
        viewModel.close()
    }

    /**
     * GIVEN: 진행 중인 기록이 일시정지 상태로 남아 있을 때
     * WHEN: 화면에 들어오면
     * THEN: 카운트다운 없이 정지 화면으로 열고 서비스를 켠다
     */
    @Test
    fun `진행 중인 기록이 있으면 카운트다운 없이 이어간다`() = runTest {
        val viewModel = viewModel(FakeWorkoutTrackingRepository(record(WorkoutStatus.ACTIVITY_PAUSED)))

        assertEquals(WorkoutMode.PAUSED, viewModel.uiState.value.mode)
        assertEquals(WorkoutRecordingSideEffect.StartTrackingService, viewModel.sideEffect.first())
        viewModel.close()
    }

    /**
     * GIVEN: 앱이 죽은 뒤 기록 화면이 바로 복원돼서 기기 기록이 아직 메모리에 없을 때
     * WHEN: 화면에 들어오고 카운트다운 끝 신호가 와도
     * THEN: 기기 기록을 먼저 불러와 이어가고 새 기록은 시작하지 않는다
     */
    @Test
    fun `기기에 저장된 기록이 있으면 먼저 불러와 이어간다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository(savedRecord = record())
        val viewModel = viewModel(tracking)

        viewModel.onCountdownFinish()

        assertTrue(tracking.isRestored)
        assertNull(tracking.startedRecord)
        assertEquals(WorkoutMode.RECORDING, viewModel.uiState.value.mode)
        assertEquals(WorkoutRecordingSideEffect.StartTrackingService, viewModel.sideEffect.first())
        viewModel.close()
    }

    /**
     * GIVEN: 기록 중일 때
     * WHEN: 저장소에 경로 좌표가 쌓이고 5분이 지나면
     * THEN: 거리, 최고 고도는 반올림하고 시간은 초 단위로, 경로는 좌표 목록으로 보여준다
     */
    @Test
    fun `저장소 상태를 거리, 시간, 고도, 경로로 보여준다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository(record())
        val viewModel = viewModel(tracking)
        clock.now = START.plusSeconds(300)

        tracking.setState(WorkoutTrackingState(record = record(), trackPoints = listOf(trackPoint(1), trackPoint(2))))

        val state = viewModel.uiState.value
        assertEquals(1_234, state.distanceMeters)
        assertEquals(300, state.durationSec)
        assertEquals(303, state.maxElevationMeters)
        assertEquals(listOf(MapPoint(38.0, 127.0), MapPoint(39.0, 127.0)), state.route)
        viewModel.close()
    }

    /**
     * GIVEN: 기록 중일 때
     * WHEN: 정지를 누르면
     * THEN: 저장소 상태를 따라 정지 화면으로 바뀐다
     */
    @Test
    fun `정지를 누르면 저장소 상태를 따라 정지 화면이 된다`() = runTest {
        val viewModel = viewModel(FakeWorkoutTrackingRepository(record()))

        viewModel.onStopClick()

        assertEquals(WorkoutMode.PAUSED, viewModel.uiState.value.mode)
        viewModel.close()
    }

    /**
     * GIVEN: 경로 좌표가 2개인 일시정지 기록일 때
     * WHEN: 종료를 길게 누르면
     * THEN: 마무리 화면으로 바뀌고 기록을 업로드 대기로 바꾼 뒤 경로로 지도 캡처를 요청한다
     */
    @Test
    fun `종료하면 업로드 대기로 바꾸고 지도 캡처를 요청한다`() = runTest {
        val tracking = pausedWithPoints(2)
        val viewModel = viewModel(tracking)
        viewModel.sideEffect.first()

        viewModel.onEndLongPress()

        assertEquals(WorkoutMode.FINISHING, viewModel.uiState.value.mode)
        assertEquals(WorkoutStatus.PENDING_UPLOAD, tracking.state.value.record?.status)
        assertEquals(
            WorkoutRecordingSideEffect.CaptureMap(listOf(MapPoint(38.0, 127.0), MapPoint(39.0, 127.0))),
            viewModel.sideEffect.first(),
        )
        viewModel.close()
    }

    /**
     * GIVEN: 종료 후 지도 캡처가 왔을 때
     * WHEN: 마무리 애니메이션이 끝나면
     * THEN: 저장을 마치고 기록 요약을 들고 타임라인으로 간다
     */
    @Test
    fun `저장되면 마무리 애니메이션 뒤 타임라인으로 간다`() = runTest {
        val tracking = pausedWithPoints(2)
        val viewModel = viewModel(tracking)
        viewModel.onEndLongPress()

        viewModel.onMapCaptured(byteArrayOf(1))
        viewModel.onFinishAnimationEnd()

        val summary = WorkoutSummary(
            activityId = 1,
            endedAtEpochMillis = START.toEpochMilli(),
            distanceMeters = 1_234,
            durationSeconds = 60,
            maxElevationMeters = 303,
        )
        assertEquals(
            WorkoutRecordingSideEffect.NavigateToTimeline(summary),
            withTimeout(EFFECT_TIMEOUT_MILLIS) {
                viewModel.sideEffect.dropWhile { it !is WorkoutRecordingSideEffect.NavigateToTimeline }.first()
            },
        )
        assertTrue(tracking.isCleared)
        viewModel.close()
    }

    /**
     * GIVEN: 경로 좌표가 1개뿐인 기록일 때
     * WHEN: 종료 후 마무리 애니메이션이 끝나면
     * THEN: 기록 등록 실패 모달을 띄우고, 확인을 누르면 홈으로 간다
     */
    @Test
    fun `경로 좌표가 부족하면 기록 등록 실패 모달을 띄운다`() = runTest {
        val viewModel = viewModel(pausedWithPoints(1))
        viewModel.onEndLongPress()
        viewModel.onMapCaptured(byteArrayOf(1))

        viewModel.onFinishAnimationEnd()
        assertTrue(viewModel.uiState.value.isInvalidRouteDialogVisible)

        viewModel.onInvalidRouteDialogConfirm()
        assertEquals(
            WorkoutRecordingSideEffect.NavigateToHome,
            withTimeout(EFFECT_TIMEOUT_MILLIS) {
                viewModel.sideEffect.dropWhile {
                    it !=
                        WorkoutRecordingSideEffect.NavigateToHome
                }.first()
            },
        )
        viewModel.close()
    }

    /**
     * GIVEN: 지도 캡처에 실패했을 때
     * WHEN: 마무리 애니메이션이 끝나면
     * THEN: 저장 실패를 알리고 기기 기록은 남긴다
     */
    @Test
    fun `캡처에 실패하면 저장 실패를 알린다`() = runTest {
        val tracking = pausedWithPoints(2)
        val viewModel = viewModel(tracking)
        viewModel.onEndLongPress()
        viewModel.onMapCaptured(null)

        viewModel.onFinishAnimationEnd()

        assertEquals(
            WorkoutRecordingSideEffect.SaveFailed,
            withTimeout(EFFECT_TIMEOUT_MILLIS) {
                viewModel.sideEffect.dropWhile {
                    it !=
                        WorkoutRecordingSideEffect.SaveFailed
                }.first()
            },
        )
        assertFalse(tracking.isCleared)
        viewModel.close()
    }

    /** 1초마다 시간을 갱신하는 코루틴을 멈춤, 안 멈추면 runTest가 가상 시간을 끝없이 돌림 */
    private fun WorkoutRecordingViewModel.close() = viewModelScope.cancel()

    private fun viewModel(
        tracking: FakeWorkoutTrackingRepository,
        remote: FakeWorkoutRemoteRepository = FakeWorkoutRemoteRepository(),
    ) = WorkoutRecordingViewModel(
        trackingRepository = tracking,
        remoteRepository = remote,
        saveFinishedWorkout = SaveFinishedWorkoutUseCase(
            trackingRepository = tracking,
            recordRepository = FakeWorkoutRecordRepository(),
            mapImageRepository = FakeMapImageRepository(),
            remoteRepository = remote,
        ),
        locationRepository = FakeLocationRepository(),
        clock = clock,
    )

    private fun pausedWithPoints(count: Int) = FakeWorkoutTrackingRepository(
        record(WorkoutStatus.ACTIVITY_PAUSED),
    ).apply {
        setState(
            WorkoutTrackingState(
                record = record(WorkoutStatus.ACTIVITY_PAUSED).copy(durationMillis = 60_000),
                trackPoints = (1..count).map(::trackPoint),
            ),
        )
    }

    private companion object {
        const val EFFECT_TIMEOUT_MILLIS = 5_000L
    }
}
