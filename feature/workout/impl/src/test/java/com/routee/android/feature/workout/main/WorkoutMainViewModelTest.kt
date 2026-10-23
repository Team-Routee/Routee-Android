package com.routee.android.feature.workout.main

import com.routee.android.feature.workout.FakeWorkoutRemoteRepository
import com.routee.android.feature.workout.FakeWorkoutTrackingRepository
import com.routee.android.feature.workout.MainDispatcherRule
import com.routee.android.feature.workout.START
import com.routee.android.feature.workout.TestClock
import com.routee.android.feature.workout.api.WorkoutStart
import com.routee.android.feature.workout.main.model.WorkoutMainSideEffect
import com.routee.android.feature.workout.record
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WorkoutMainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * GIVEN: 기기에 진행 중인 기록이 있을 때
     * WHEN: 운동 메인에 들어오면
     * THEN: 기록을 복원하고 기록 화면으로 보낸다
     */
    @Test
    fun `진행 중인 기록이 있으면 기록 화면으로 보낸다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository(savedRecord = record())

        val viewModel = viewModel(tracking)

        assertTrue(tracking.isRestored)
        assertEquals(WorkoutMainSideEffect.NavigateToRecording(), viewModel.sideEffect.first())
    }

    /**
     * GIVEN: 진행 중인 기록이 없을 때
     * WHEN: 운동 메인에 들어오면
     * THEN: 홈에 머문다
     */
    @Test
    fun `진행 중인 기록이 없으면 홈에 머문다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository()

        val viewModel = viewModel(tracking)

        assertTrue(tracking.isRestored)
        assertNull(withTimeoutOrNull(100) { viewModel.sideEffect.first() })
    }

    /**
     * GIVEN: 시작 API가 성공할 때
     * WHEN: 운동 기록을 누르면
     * THEN: 받은 id, 제목과 누른 시각을 들고 기록 화면으로 간다
     */
    @Test
    fun `시작 API가 성공하면 기록 화면으로 간다`() = runTest {
        val viewModel = viewModel(FakeWorkoutTrackingRepository())

        viewModel.onRecordStart()

        val expected =
            WorkoutStart(activityId = 1, title = "2026.10.23 기록", startedAtEpochMillis = START.toEpochMilli())
        assertEquals(WorkoutMainSideEffect.NavigateToRecording(expected), viewModel.sideEffect.first())
        assertFalse(viewModel.uiState.value.isStarting)
    }

    /**
     * GIVEN: 시작 API가 실패할 때
     * WHEN: 운동 기록을 누르면
     * THEN: 운동 메인에 머물며 실패를 알리고 버튼을 다시 켠다
     */
    @Test
    fun `시작 API가 실패하면 운동 메인에서 알린다`() = runTest {
        val viewModel =
            viewModel(FakeWorkoutTrackingRepository(), FakeWorkoutRemoteRepository(createError = IOException()))

        viewModel.onRecordStart()

        assertEquals(WorkoutMainSideEffect.StartFailed, viewModel.sideEffect.first())
        assertFalse(viewModel.uiState.value.isStarting)
    }

    private fun viewModel(
        tracking: FakeWorkoutTrackingRepository,
        remote: FakeWorkoutRemoteRepository = FakeWorkoutRemoteRepository(),
    ) = WorkoutMainViewModel(tracking, remote, TestClock())
}
