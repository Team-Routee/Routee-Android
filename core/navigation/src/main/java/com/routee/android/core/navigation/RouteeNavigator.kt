package com.routee.android.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * feature의 화면 이동 담당 Navigator
 */
class RouteeNavigator internal constructor(
    private val backStack: NavBackStack<NavKey>,
    private val homeKey: NavKey,
) {
    fun navigateTo(key: NavKey) {
        if (backStack.lastOrNull() == key) return
        backStack.add(key)
    }

    fun navigateBack() {
        if (backStack.size > 1) backStack.removeLastOrNull()
    }

    fun clearAndNavigateTo(key: NavKey) {
        backStack.clear()
        backStack.add(key)
    }

    fun navigateToHome() = clearAndNavigateTo(homeKey)
}

@Composable
fun rememberRouteeNavigator(
    backStack: NavBackStack<NavKey>,
    homeKey: NavKey,
): RouteeNavigator = remember(backStack, homeKey) { RouteeNavigator(backStack, homeKey) }
