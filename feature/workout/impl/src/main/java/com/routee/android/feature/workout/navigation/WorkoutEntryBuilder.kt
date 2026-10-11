package com.routee.android.feature.workout.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.feature.workout.api.WorkoutMainNavKey
import com.routee.android.feature.workout.main.WorkoutMainRoute

fun EntryProviderScope<NavKey>.workoutEntries(navigator: RouteeNavigator) {
    entry<WorkoutMainNavKey> {
        WorkoutMainRoute()
    }
}
