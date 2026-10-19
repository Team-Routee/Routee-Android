package com.routee.android.feature.setting.component.contact

import androidx.compose.ui.platform.UriHandler
import com.routee.android.core.ui.link.RouteeLink
import com.routee.android.core.ui.link.tryOpenUri

/**
 * 카카오톡 채널 열기. 카카오톡 앱 먼저, 없으면 웹. 둘 다 못 열면 false
 */
internal fun UriHandler.tryOpenKakaoChannel(): Boolean =
    tryOpenUri(RouteeLink.KAKAO_CHANNEL_APP.url) || tryOpenUri(RouteeLink.KAKAO_CHANNEL.url)

/**
 * 기본 메일 앱으로 문의 메일 작성. 메일 앱이 없으면 false
 */
internal fun UriHandler.tryOpenContactEmail(): Boolean = tryOpenUri(RouteeLink.CONTACT_EMAIL.url)
