package com.routee.android.feature.setting.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.feature.onboarding.api.LoginNavKey
import com.routee.android.feature.setting.api.ContactNavKey
import com.routee.android.feature.setting.api.SettingNavKey
import com.routee.android.feature.setting.api.WithdrawNavKey
import com.routee.android.feature.setting.contact.ContactRoute
import com.routee.android.feature.setting.main.SettingRoute
import com.routee.android.feature.setting.withdraw.WithdrawRoute

fun EntryProviderScope<NavKey>.settingEntries(navigator: RouteeNavigator) {
    entry<SettingNavKey> {
        SettingRoute(
            onProfileClick = {}, // TODO 프로필 변경 화면 연결
            onContactClick = { navigator.navigateTo(ContactNavKey) },
            onWithdrawClick = { navigator.navigateTo(WithdrawNavKey) },
            onLogout = { navigator.clearAndNavigateTo(LoginNavKey()) },
        )
    }
    entry<ContactNavKey> {
        ContactRoute(onBackClick = navigator::navigateBack)
    }
    entry<WithdrawNavKey> {
        WithdrawRoute(
            onBackClick = navigator::navigateBack,
            onWithdrawn = { navigator.clearAndNavigateTo(LoginNavKey()) },
        )
    }
}
