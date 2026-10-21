package com.routee.android.core.domain.member

import javax.inject.Inject

private const val NICKNAME_MAX_LENGTH = 12
private val NicknamePattern = Regex("^[가-힣a-zA-Z0-9]+( [가-힣a-zA-Z0-9]+)*$")

/**
 * 닉네임 검사. 한글 · 영문 · 숫자, 단어 사이 공백 한 칸, 최대 12자. 끝 공백은 무시
 *
 * ```kotlin
 * validateNickname("관악산 날다람쥐") // NicknameValidation.VALID
 * ```
 */
class ValidateNicknameUseCase @Inject constructor() {
    operator fun invoke(nickname: CharSequence): NicknameValidation {
        val trimmed = nickname.trimEnd()
        return when {
            trimmed.isEmpty() -> NicknameValidation.EMPTY
            trimmed.length <= NICKNAME_MAX_LENGTH && NicknamePattern.matches(trimmed) -> NicknameValidation.VALID
            else -> NicknameValidation.INVALID
        }
    }
}
