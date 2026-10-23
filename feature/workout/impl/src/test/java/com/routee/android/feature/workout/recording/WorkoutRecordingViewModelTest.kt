package com.routee.android.feature.workout.recording

import androidx.lifecycle.viewModelScope
import com.routee.android.core.data.workout.model.WorkoutStatus
import com.routee.android.core.data.workout.model.WorkoutTrackingState
import com.routee.android.core.map.model.MapPoint
import com.routee.android.feature.workout.FakeLocationRepository
import com.routee.android.feature.workout.FakeWorkoutRemoteRepository
import com.routee.android.feature.workout.FakeWorkoutTrackingRepository
import com.routee.android.feature.workout.MainDispatcherRule
import com.routee.android.feature.workout.START
import com.routee.android.feature.workout.TestClock
import com.routee.android.feature.workout.record
import com.routee.android.feature.workout.recording.model.WorkoutMode
import com.routee.android.feature.workout.recording.model.WorkoutRecordingSideEffect
import com.routee.android.feature.workout.trackPoint
import java.io.IOException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
     * WHEN: 카운트다운이 끝나면
     * THEN: 기기 기록을 시작하지 않고 시작 실패를 알린다
     */
    @Test
    fun `시작 API가 실패하면 기록을 시작하지 않는다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository()
        val viewModel = viewModel(tracking, FakeWorkoutRemoteRepository(createError = IOException()))

        viewModel.onCountdownFinish()

        assertNull(tracking.startedRecord)
        assertEquals(WorkoutMode.COUNTDOWN, viewModel.uiState.value.mode)
        assertEquals(WorkoutRecordingSideEffect.StartFailed, viewModel.sideEffect.first())
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

    /** 1초마다 시간을 갱신하는 코루틴을 멈춤, 안 멈추면 runTest가 가상 시간을 끝없이 돌림 */
    private fun WorkoutRecordingViewModel.close() = viewModelScope.cancel()

    private fun viewModel(
        tracking: FakeWorkoutTrackingRepository,
        remote: FakeWorkoutRemoteRepository = FakeWorkoutRemoteRepository(),
    ) = WorkoutRecordingViewModel(tracking, remote, FakeLocationRepository(), clock)
}
