package com.routee.android.feature.workout.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.feature.workout.api.WorkoutMainNavKey
import com.routee.android.feature.workout.api.WorkoutRecordingNavKey
import com.routee.android.feature.workout.main.WorkoutMainRoute
import com.routee.android.feature.workout.recording.WorkoutRecordingRoute

fun EntryProviderScope<NavKey>.workoutEntries(navigator: RouteeNavigator) {
    entry<WorkoutMainNavKey> {
        WorkoutMainRoute(onRecordClick = { navigator.navigateTo(WorkoutRecordingNavKey) })
    }
    entry<WorkoutRecordingNavKey> {
        // TODO 운동 타임라인으로 이동
        WorkoutRecordingRoute(onStartFail = navigator::navigateBack, onFinish = navigator::navigateToHome)
    }
}
