package com.routee.android.feature.onboarding.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.feature.onboarding.api.LoginNavKey
import com.routee.android.feature.onboarding.api.NicknameNavKey
import com.routee.android.feature.onboarding.api.TermsNavKey
import com.routee.android.feature.onboarding.login.LoginRoute
import com.routee.android.feature.onboarding.nickname.NicknameRoute
import com.routee.android.feature.onboarding.terms.TermsRoute
import com.routee.android.feature.workout.api.WorkoutMainNavKey

fun EntryProviderScope<NavKey>.onboardingEntries(navigator: RouteeNavigator) {
    entry<LoginNavKey> {
        LoginRoute(onLoginClick = { navigator.navigateTo(TermsNavKey) })
    }
    entry<TermsNavKey> {
        TermsRoute(
            onAgreeClick = { navigator.navigateTo(NicknameNavKey) },
            onBackClick = navigator::navigateBack,
        )
    }
    entry<NicknameNavKey> {
        NicknameRoute(
            onStartClick = { navigator.clearAndNavigateTo(WorkoutMainNavKey) },
            onBackClick = navigator::navigateBack,
        )
    }
}
