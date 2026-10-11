package com.routee.android.navigation

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.routee.android.R
import com.routee.android.feature.archive.api.ArchiveNavKey
import com.routee.android.feature.recordedit.api.RecordEditMainNavKey
import com.routee.android.feature.setting.api.SettingNavKey
import com.routee.android.feature.workout.api.WorkoutMainNavKey

internal enum class RouteeTab(
    val navKey: NavKey,
    @StringRes val labelRes: Int,
) {
    WORKOUT(WorkoutMainNavKey, R.string.tab_workout),
    RECORD_EDIT(RecordEditMainNavKey, R.string.tab_record_edit),
    ARCHIVE(ArchiveNavKey, R.string.tab_archive),
    SETTING(SettingNavKey, R.string.tab_setting),
    ;

    companion object {
        fun find(navKey: NavKey?): RouteeTab? = entries.find { it.navKey == navKey }
    }
}
