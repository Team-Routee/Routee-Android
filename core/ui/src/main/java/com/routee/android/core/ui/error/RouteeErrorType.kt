package com.routee.android.core.ui.error

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.ui.R

/**
 * 에러 화면 종류 (RT-EDGE-001)
 */
enum class RouteeErrorType(
    @param:DrawableRes val graphicRes: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
) {
    NotFound(
        graphicRes = DesignSystemR.drawable.graphic_404_error_xl,
        titleRes = R.string.ui_error_not_found_title,
        descriptionRes = R.string.ui_error_not_found_description,
    ),
    Unknown(
        graphicRes = DesignSystemR.drawable.graphic_unknown_error_xl,
        titleRes = R.string.ui_error_unknown_title,
        descriptionRes = R.string.ui_error_unknown_description,
    ),
}
