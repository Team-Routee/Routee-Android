package com.routee.android.feature.workout.main

import com.routee.android.feature.workout.FakeWorkoutTrackingRepository
import com.routee.android.feature.workout.MainDispatcherRule
import com.routee.android.feature.workout.main.model.WorkoutMainSideEffect
import com.routee.android.feature.workout.record
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
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

        val viewModel = WorkoutMainViewModel(tracking)

        assertTrue(tracking.isRestored)
        assertEquals(WorkoutMainSideEffect.NavigateToRecording, viewModel.sideEffect.first())
    }

    /**
     * GIVEN: 진행 중인 기록이 없을 때
     * WHEN: 운동 메인에 들어오면
     * THEN: 홈에 머문다
     */
    @Test
    fun `진행 중인 기록이 없으면 홈에 머문다`() = runTest {
        val tracking = FakeWorkoutTrackingRepository()

        val viewModel = WorkoutMainViewModel(tracking)

        assertTrue(tracking.isRestored)
        assertNull(withTimeoutOrNull(100) { viewModel.sideEffect.first() })
    }
}
