package com.routee.android.feature.recordedit.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.routee.android.core.navigation.RouteeNavigator
import com.routee.android.feature.recordedit.api.RecordEditMainNavKey
import com.routee.android.feature.recordedit.main.RecordEditMainRoute

fun EntryProviderScope<NavKey>.recordEditEntries(navigator: RouteeNavigator) {
    entry<RecordEditMainNavKey> {
        RecordEditMainRoute()
    }
}
