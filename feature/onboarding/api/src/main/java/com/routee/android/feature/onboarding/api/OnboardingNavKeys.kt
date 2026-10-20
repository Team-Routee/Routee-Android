package com.routee.android.feature.onboarding.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 로그인 화면 키
 *
 * @param isSessionExpired 토큰 재발급까지 실패해 다시 로그인해야 할 때 true. 진입 시 안내 다이얼로그 노출
 */
@Serializable
data class LoginNavKey(
    val isSessionExpired: Boolean = false,
) : NavKey

/**
 * 이용약관 동의 화면 키
 *
 * @param idToken 구글 로그인으로 받은 idToken. 회원가입 요청까지 넘긴다
 */
@Serializable
data class TermsNavKey(
    val idToken: String,
) : NavKey

/**
 * 닉네임 작성 화면 키. 필수 약관은 모두 동의한 상태로 진입한다
 *
 * @param marketingConsent 선택 약관(마케팅 활용 및 광고성 정보 수신) 동의 여부
 */
@Serializable
data class NicknameNavKey(
    val idToken: String,
    val marketingConsent: Boolean,
) : NavKey
