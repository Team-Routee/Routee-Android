package com.routee.android.util

import android.app.Activity
import android.content.pm.ActivityInfo

/**
 * 폰만 세로 고정, 큰 화면은 회전 허용. Android 16 이상 sw600dp 이상은 시스템이 고정을 무시
 */
internal fun Activity.lockPortraitOnPhone() {
    val isPhone = resources.configuration.smallestScreenWidthDp < LARGE_SCREEN_MIN_WIDTH_DP
    requestedOrientation = if (isPhone) {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    } else {
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}

private const val LARGE_SCREEN_MIN_WIDTH_DP = 600
