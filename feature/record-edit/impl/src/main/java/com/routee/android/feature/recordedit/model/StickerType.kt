package com.routee.android.feature.recordedit.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.feature.recordedit.R

/**
 * 기록 편집 스티커 종류
 */
internal enum class StickerType(
    @StringRes val labelRes: Int,
    @DrawableRes val selectedIconRes: Int,
    @DrawableRes val unselectedIconRes: Int,
) {
    WORKOUT_RECORD(
        labelRes = R.string.record_edit_editor_sticker_workout_record,
        selectedIconRes = DesignSystemR.drawable.ic_workout_record_sm_white,
        unselectedIconRes = DesignSystemR.drawable.ic_workout_record_sm_grey,
    ),
    PHOTO_TIMELINE(
        labelRes = R.string.record_edit_editor_sticker_photo_timeline,
        selectedIconRes = DesignSystemR.drawable.ic_photo_timeline_sticker_sm_white,
        unselectedIconRes = DesignSystemR.drawable.ic_photo_timeline_sticker_sm_grey,
    ),
    ROUTE(
        labelRes = R.string.record_edit_editor_sticker_route,
        selectedIconRes = DesignSystemR.drawable.ic_route_sticker_sm_white,
        unselectedIconRes = DesignSystemR.drawable.ic_route_sticker_sm_grey,
    ),
}
