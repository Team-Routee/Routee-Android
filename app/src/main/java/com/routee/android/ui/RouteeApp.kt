package com.routee.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.rememberResultEventBusNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.RouteeMessageHost
import com.routee.android.core.designsystem.component.message.rememberRouteeMessageState
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.core.navigation.rememberRouteeNavigator
import com.routee.android.core.ui.loading.LocalRouteeLoadingState
import com.routee.android.core.ui.loading.RouteeLoadingHost
import com.routee.android.core.ui.loading.RouteeLoadingState
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
    // TODO 시작 화면 결정: 토큰 없음 -> LoginNavKey / 진행 중인 운동 있음 -> 운동 중(정지) 화면 / 그 외 -> WorkoutMainNavKey
    val backStack = rememberNavBackStack(WorkoutMainNavKey)
    val navigator = rememberRouteeNavigator(backStack)

    val messageState = rememberRouteeMessageState()
    val loadingState = remember { RouteeLoadingState() }

    CompositionLocalProvider(
        LocalRouteeMessageState provides messageState,
        LocalRouteeLoadingState provides loadingState,
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { RouteeMessageHost(state = messageState) },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                RouteeNavDisplay(
                    backStack = backStack,
                    navigator = navigator,
                )
                RouteeLoadingHost(state = loadingState)
            }
        }
    }
}

@Composable
private fun RouteeNavDisplay(
    backStack: NavBackStack<NavKey>,
    navigator: RouteeNavigator,
    modifier: Modifier = Modifier,
) {
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
