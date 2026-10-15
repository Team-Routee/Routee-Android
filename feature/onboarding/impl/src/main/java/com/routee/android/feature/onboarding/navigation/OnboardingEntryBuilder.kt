package com.routee.android.feature.onboarding.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.core.navigation.util.viewModelEntry
import com.routee.android.feature.onboarding.api.LoginNavKey
import com.routee.android.feature.onboarding.api.NicknameNavKey
import com.routee.android.feature.onboarding.api.TermsNavKey
import com.routee.android.feature.onboarding.login.LoginRoute
import com.routee.android.feature.onboarding.nickname.NicknameRoute
import com.routee.android.feature.onboarding.nickname.NicknameViewModel
import com.routee.android.feature.onboarding.terms.TermsRoute
import com.routee.android.feature.onboarding.terms.TermsViewModel
import com.routee.android.feature.workout.api.WorkoutMainNavKey

fun EntryProviderScope<NavKey>.onboardingEntries(navigator: RouteeNavigator) {
    entry<LoginNavKey> {
        LoginRoute(
            onNavigateToHome = { navigator.clearAndNavigateTo(WorkoutMainNavKey) },
            onNavigateToTerms = { idToken -> navigator.navigateTo(TermsNavKey(idToken)) },
        )
    }
    viewModelEntry<TermsNavKey, TermsViewModel, TermsViewModel.Factory> { _, viewModel ->
        TermsRoute(
            viewModel = viewModel,
            onNavigateBack = navigator::navigateBack,
            onNavigateToNickname = { idToken, marketingConsent ->
                navigator.navigateTo(NicknameNavKey(idToken = idToken, marketingConsent = marketingConsent))
            },
        )
    }
    viewModelEntry<NicknameNavKey, NicknameViewModel, NicknameViewModel.Factory> { _, viewModel ->
        NicknameRoute(
            viewModel = viewModel,
            onBackClick = navigator::navigateBack,
            onNavigateToHome = { navigator.clearAndNavigateTo(WorkoutMainNavKey) },
        )
    }
}
