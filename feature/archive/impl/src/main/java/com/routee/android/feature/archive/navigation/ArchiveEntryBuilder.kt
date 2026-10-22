package com.routee.android.feature.archive.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.core.navigation.util.viewModelEntry
import com.routee.android.feature.archive.ArchiveRoute
import com.routee.android.feature.archive.api.ArchiveDetailNavKey
import com.routee.android.feature.archive.api.ArchiveNavKey
import com.routee.android.feature.archive.detail.ArchiveDetailRoute
import com.routee.android.feature.archive.detail.ArchiveDetailViewModel

fun EntryProviderScope<NavKey>.archiveEntries(navigator: RouteeNavigator) {
    entry<ArchiveNavKey> {
        ArchiveRoute(
            onRecordClick = { activityId -> navigator.navigateTo(ArchiveDetailNavKey(activityId)) },
            onHomeClick = navigator::navigateToHome,
        )
    }
    viewModelEntry<ArchiveDetailNavKey, ArchiveDetailViewModel, ArchiveDetailViewModel.Factory> { _, viewModel ->
        ArchiveDetailRoute(
            viewModel = viewModel,
            onBackClick = navigator::navigateBack,
            onHomeClick = navigator::navigateToHome,
        )
    }
}
