package com.routee.android.core.ui.link

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.net.toUri

/**
 * 웹 링크를 받는 앱(브라우저 제외)으로 열기. 받는 앱이 없거나 Android 10 이하면 false
 *
 * false 이면 [tryOpenUri]로 브라우저에서 연다
 */
fun Context.tryOpenUriInNativeApp(uri: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
    val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
        .addFlags(Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER)
    return try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
