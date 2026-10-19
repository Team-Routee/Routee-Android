package com.routee.android.core.common.extension

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * 설치된 앱 버전 (예: "1.0.0"). 읽지 못하면 빈 문자열
 */
val Context.appVersionName: String
    get() = runCatching {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
        packageInfo.versionName
    }.getOrNull().orEmpty()
