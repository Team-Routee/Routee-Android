package com.routee.android.feature.archive.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.feature.archive.ArchiveRoute
import com.routee.android.feature.archive.api.ArchiveDetailNavKey
import com.routee.android.feature.archive.api.ArchiveNavKey
import com.routee.android.feature.archive.detail.ArchiveDetailRoute

fun EntryProviderScope<NavKey>.archiveEntries(navigator: RouteeNavigator) {
    entry<ArchiveNavKey> {
        ArchiveRoute(
            onRecordClick = { activityId -> navigator.navigateTo(ArchiveDetailNavKey(activityId)) },
            onHomeClick = navigator::navigateToHome,
        )
    }
    entry<ArchiveDetailNavKey> { key ->
        ArchiveDetailRoute(activityId = key.activityId, onBackClick = navigator::navigateBack)
    }
}
