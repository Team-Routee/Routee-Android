package com.routee.android.core.domain.member

import javax.inject.Inject

private const val NICKNAME_MAX_LENGTH = 12
private val NicknamePattern = Regex("^[가-힣a-zA-Z0-9]+( [가-힣a-zA-Z0-9]+)*$")
private val HangulJamoPattern = Regex("[ㄱ-ㅣ]")

/**
 * 닉네임 검사. 한글 · 영문 · 숫자, 단어 사이 공백 한 칸, 최대 12자. 끝 공백은 무시
 *
 * 입력 중이면 [composing]에 조합 중인 범위를 넘긴다. 조합 중인 글자가 자모 상태면 그 글자를 빼고 검사
 *
 * ```kotlin
 * validateNickname("관악산 날다람쥐") // NicknameValidation.VALID
 * validateNickname("관악산ㄴ", composing = 3..3) // NicknameValidation.VALID
 * ```
 */
class ValidateNicknameUseCase @Inject constructor() {
    operator fun invoke(
        nickname: CharSequence,
        composing: IntRange? = null,
    ): NicknameValidation {
        if (composing == null || !HangulJamoPattern.containsMatchIn(nickname.subSequence(composing))) {
            return validate(nickname)
        }
        return validate(nickname.removeRange(composing))
    }

    private fun validate(nickname: CharSequence): NicknameValidation {
        val trimmed = nickname.trimEnd()
        return when {
            trimmed.isEmpty() -> NicknameValidation.EMPTY
            trimmed.length <= NICKNAME_MAX_LENGTH && NicknamePattern.matches(trimmed) -> NicknameValidation.VALID
            else -> NicknameValidation.INVALID
        }
    }
}
