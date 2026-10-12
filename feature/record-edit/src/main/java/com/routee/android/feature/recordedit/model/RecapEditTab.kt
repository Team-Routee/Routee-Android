package com.routee.android.feature.recordedit.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.feature.recordedit.R

/**
 * 기록 편집 하단 편집 탭
 */
internal enum class RecapEditTab(
    @StringRes val labelRes: Int,
    @DrawableRes val selectedIconRes: Int,
    @DrawableRes val unselectedIconRes: Int,
) {
    BACKGROUND(
        labelRes = R.string.record_edit_editor_tab_background,
        selectedIconRes = DesignSystemR.drawable.ic_change_bg_sm_white,
        unselectedIconRes = DesignSystemR.drawable.ic_change_bg_sm_grey,
    ),
    BRIGHTNESS(
        labelRes = R.string.record_edit_editor_tab_brightness,
        selectedIconRes = DesignSystemR.drawable.ic_brightness_sm_white,
        unselectedIconRes = DesignSystemR.drawable.ic_brightness_sm_grey,
    ),
    COLOR(
        labelRes = R.string.record_edit_editor_tab_color,
        selectedIconRes = DesignSystemR.drawable.ic_change_color_sm_white,
        unselectedIconRes = DesignSystemR.drawable.ic_change_color_sm_grey,
    ),
    STICKER(
        labelRes = R.string.record_edit_editor_tab_sticker,
        selectedIconRes = DesignSystemR.drawable.ic_sticker_sm_white,
        unselectedIconRes = DesignSystemR.drawable.ic_sticker_sm_grey,
    ),
}
