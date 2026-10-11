package com.routee.android.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.rememberResultEventBusNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.routee.android.core.navigation.rememberRouteeNavigator
import com.routee.android.feature.archive.navigation.archiveEntries
import com.routee.android.feature.onboarding.navigation.onboardingEntries
import com.routee.android.feature.recordedit.navigation.recordEditEntries
import com.routee.android.feature.setting.navigation.settingEntries
import com.routee.android.feature.workout.api.WorkoutMainNavKey
import com.routee.android.feature.workout.navigation.workoutEntries
import com.routee.android.navigation.rememberRouteeTabSceneDecorator

@Composable
internal fun RouteeApp(
    modifier: Modifier = Modifier,
) {
    // TODO 로그인 연결 필요. 토큰 여부로 시작 화면 결정 (없으면 LoginNavKey)
    val backStack = rememberNavBackStack(WorkoutMainNavKey)
    val navigator = rememberRouteeNavigator(backStack)

    NavDisplay(
        backStack = backStack,
        modifier = modifier.fillMaxSize(),
        onBack = navigator::navigateBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
            rememberResultEventBusNavEntryDecorator(),
        ),
        sceneDecoratorStrategies = listOf(
            rememberRouteeTabSceneDecorator(backStack, navigator),
        ),
        entryProvider = entryProvider {
            onboardingEntries(navigator)
            workoutEntries(navigator)
            recordEditEntries(navigator)
            archiveEntries(navigator)
            settingEntries(navigator)
        },
    )
}
