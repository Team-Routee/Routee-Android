package com.routee.android.feature.workout.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.core.navigation.util.viewModelEntry
import com.routee.android.feature.workout.api.WorkoutMainNavKey
import com.routee.android.feature.workout.api.WorkoutRecordingNavKey
import com.routee.android.feature.workout.main.WorkoutMainRoute
import com.routee.android.feature.workout.recording.WorkoutRecordingRoute
import com.routee.android.feature.workout.recording.WorkoutRecordingViewModel

fun EntryProviderScope<NavKey>.workoutEntries(navigator: RouteeNavigator) {
    entry<WorkoutMainNavKey> {
        WorkoutMainRoute(onNavigateToRecording = { start -> navigator.navigateTo(WorkoutRecordingNavKey(start)) })
    }
    viewModelEntry<WorkoutRecordingNavKey, WorkoutRecordingViewModel, WorkoutRecordingViewModel.Factory> {
            _,
            viewModel,
        ->
        // TODO 운동 타임라인으로 이동
        WorkoutRecordingRoute(
            viewModel = viewModel,
            onNavigateBack = navigator::navigateBack,
            onFinish = navigator::navigateToHome,
        )
    }
}
