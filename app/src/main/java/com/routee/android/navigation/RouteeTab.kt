package com.routee.android.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.routee.android.R
import com.routee.android.feature.archive.api.ArchiveNavKey
import com.routee.android.feature.recordedit.api.RecordEditMainNavKey
import com.routee.android.feature.setting.api.SettingNavKey
import com.routee.android.feature.workout.api.WorkoutMainNavKey
import com.routee.android.core.designsystem.R as DesignSystemR

internal enum class RouteeTab(
    val navKey: NavKey,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    WORKOUT(
        navKey = WorkoutMainNavKey,
        labelRes = R.string.tab_workout,
        iconRes = DesignSystemR.drawable.ic_exercise_nav_sm_white,
    ),
    RECORD_EDIT(
        navKey = RecordEditMainNavKey,
        labelRes = R.string.tab_record_edit,
        iconRes = DesignSystemR.drawable.ic_recap_nav_sm_white,
    ),
    ARCHIVE(
        navKey = ArchiveNavKey,
        labelRes = R.string.tab_archive,
        iconRes = DesignSystemR.drawable.ic_archive_nav_sm_white,
    ),
    SETTING(
        navKey = SettingNavKey,
        labelRes = R.string.tab_setting,
        iconRes = DesignSystemR.drawable.ic_setting_nav_sm_white,
    ),
    ;

    companion object {
        fun find(navKey: NavKey?): RouteeTab? = entries.find { it.navKey == navKey }
    }
}
