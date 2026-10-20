package com.routee.android.core.ui.link

import androidx.compose.ui.platform.UriHandler

/**
 * 링크 열기. 열 수 있는 앱이 없으면 false
 */
fun UriHandler.tryOpenUri(uri: String): Boolean = runCatching { openUri(uri) }.isSuccess
