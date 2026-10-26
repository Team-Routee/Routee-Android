package com.routee.android.core.domain.member

import javax.inject.Inject

private const val NICKNAME_MAX_LENGTH = 12
private val NicknamePattern = Regex("^[가-힣a-zA-Z0-9]+( [가-힣a-zA-Z0-9]+)*$")
private val HangulJamo = 'ㄱ'..'ㅣ'

/**
 * 닉네임 검사. 한글 · 영문 · 숫자, 단어 사이 공백 한 칸, 최대 12자. 끝 공백은 무시
 *
 * 입력 중이면 [composing]에 조합 중인 범위를 넘긴다. 조합 범위 끝에 이어진 자모(미완성 음절)는 빼고 검사
 *
 * ```kotlin
 * validateNickname("관악산 날다람쥐") // NicknameValidation.VALID
 * validateNickname("관악산ㄴ", composing = 0..3) // NicknameValidation.VALID
 * validateNickname("ㄱㄴㄷ", composing = 0..2) // NicknameValidation.EMPTY
 * ```
 */
class ValidateNicknameUseCase @Inject constructor() {
    operator fun invoke(
        nickname: CharSequence,
        composing: IntRange? = null,
    ): NicknameValidation {
        if (composing == null || composing.isEmpty()) return validate(nickname)
        var completedEnd = composing.last + 1
        while (completedEnd > composing.first && nickname[completedEnd - 1] in HangulJamo) completedEnd--
        return validate(nickname.removeRange(completedEnd, composing.last + 1))
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
