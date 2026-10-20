package com.routee.android.core.domain.member

import javax.inject.Inject

private const val NICKNAME_MAX_LENGTH = 12
private val NicknamePattern = Regex("^[가-힣a-zA-Z0-9]+( [가-힣a-zA-Z0-9]+)*$")

/**
 * 닉네임 검사. 한글 · 영문 · 숫자, 단어 사이 공백 한 칸, 최대 12자
 *
 * ```kotlin
 * validateNickname("관악산 날다람쥐") // true
 * ```
 */
class ValidateNicknameUseCase @Inject constructor() {
    operator fun invoke(nickname: String): Boolean =
        nickname.length <= NICKNAME_MAX_LENGTH && NicknamePattern.matches(nickname)
}
