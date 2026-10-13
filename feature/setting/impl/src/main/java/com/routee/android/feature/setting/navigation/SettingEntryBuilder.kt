package com.routee.android.feature.setting.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.feature.onboarding.api.LoginNavKey
import com.routee.android.feature.setting.api.SettingNavKey
import com.routee.android.feature.setting.main.SettingRoute

fun EntryProviderScope<NavKey>.settingEntries(navigator: RouteeNavigator) {
    entry<SettingNavKey> {
        SettingRoute(onLogoutClick = { navigator.clearAndNavigateTo(LoginNavKey) })
    }
}
