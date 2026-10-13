package com.routee.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import com.routee.android.core.designsystem.component.LocalRouteeNavBarPadding
import com.routee.android.core.designsystem.component.RouteeNavBar
import com.routee.android.core.designsystem.component.RouteeNavBarItem
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.navigation.RouteeNavigator

/**
 * 탭 첫 화면 아래에 하단 탭 바 표시
 */
@Composable
internal fun rememberRouteeTabSceneDecorator(
    backStack: List<NavKey>,
    navigator: RouteeNavigator,
): SceneDecoratorStrategy<NavKey> = remember(backStack, navigator) {
    RouteeTabSceneDecorator(backStack, navigator)
}

private class RouteeTabSceneDecorator(
    private val backStack: List<NavKey>,
    private val navigator: RouteeNavigator,
) : SceneDecoratorStrategy<NavKey> {
    override fun SceneDecoratorStrategyScope<NavKey>.decorateScene(scene: Scene<NavKey>): Scene<NavKey> {
        val currentTab = backStack
            .drop(scene.previousEntries.size)
            .take(scene.entries.size)
            .asReversed()
            .firstNotNullOfOrNull(RouteeTab::find)
            ?: return scene
        return RouteeTabScene(scene, currentTab, navigator)
    }
}

private class RouteeTabScene(
    private val delegate: Scene<NavKey>,
    private val currentTab: RouteeTab,
    private val navigator: RouteeNavigator,
) : Scene<NavKey> {
    override val key: Any get() = delegate.key
    override val entries get() = delegate.entries
    override val previousEntries get() = delegate.previousEntries
    override val metadata get() = delegate.metadata

    override val content: @Composable () -> Unit = {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                RouteeBottomBar(
                    currentTab = currentTab,
                    // TODO 탭별 상태 유지 여부 기획 확인 후 반영 필수
                    onTabClick = { tab -> navigator.clearAndNavigateTo(tab.navKey) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = RouteeTheme.spacing.Spacing16),
                )
            },
        ) { innerPadding ->
            CompositionLocalProvider(LocalRouteeNavBarPadding provides innerPadding.calculateBottomPadding()) {
                Box(Modifier.consumeWindowInsets(innerPadding)) {
                    delegate.content()
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean =
        other is RouteeTabScene && delegate == other.delegate && currentTab == other.currentTab

    override fun hashCode(): Int = delegate.hashCode() * 31 + currentTab.hashCode()
}

@Composable
private fun RouteeBottomBar(
    currentTab: RouteeTab,
    onTabClick: (RouteeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    RouteeNavBar(modifier = modifier) {
        RouteeTab.entries.forEach { tab ->
            RouteeNavBarItem(
                selected = tab == currentTab,
                onClick = { onTabClick(tab) },
                iconRes = tab.iconRes,
                label = stringResource(tab.labelRes),
            )
        }
    }
}
