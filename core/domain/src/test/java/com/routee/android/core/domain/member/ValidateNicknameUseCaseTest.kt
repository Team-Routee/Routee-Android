package com.routee.android.core.domain.member

import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateNicknameUseCaseTest {
    private val validateNickname = ValidateNicknameUseCase()

    @Test
    fun `빈 값은 EMPTY`() {
        assertEquals(NicknameValidation.EMPTY, validateNickname(""))
        assertEquals(NicknameValidation.EMPTY, validateNickname("   "))
    }

    @Test
    fun `한글 영문 숫자와 단어 사이 공백 한 칸은 VALID`() {
        assertEquals(NicknameValidation.VALID, validateNickname("관악산 날다람쥐"))
        assertEquals(NicknameValidation.VALID, validateNickname("Routee 2026"))
    }

    @Test
    fun `끝 공백은 무시하고 검사`() {
        assertEquals(NicknameValidation.VALID, validateNickname("관악산 "))
        assertEquals(NicknameValidation.VALID, validateNickname("가".repeat(12) + " "))
    }

    @Test
    fun `12자까지 VALID 13자부터 INVALID`() {
        assertEquals(NicknameValidation.VALID, validateNickname("가".repeat(12)))
        assertEquals(NicknameValidation.INVALID, validateNickname("가".repeat(13)))
    }

    @Test
    fun `특수문자와 자모만 있는 값과 연속 공백과 앞 공백은 INVALID`() {
        assertEquals(NicknameValidation.INVALID, validateNickname("날다람쥐^&^"))
        assertEquals(NicknameValidation.INVALID, validateNickname("ㄱㄴ"))
        assertEquals(NicknameValidation.INVALID, validateNickname("관악산_날다람쥐"))
        assertEquals(NicknameValidation.INVALID, validateNickname("관악산  날다람쥐"))
        assertEquals(NicknameValidation.INVALID, validateNickname(" 날다람쥐"))
    }

    @Test
    fun `조합 중인 완성 글자는 포함해서 검사`() {
        assertEquals(NicknameValidation.VALID, validateNickname("관악", composing = 1..1))
        assertEquals(NicknameValidation.INVALID, validateNickname("가".repeat(13), composing = 12..12))
    }

    @Test
    fun `조합 중인 마지막 자모는 빼고 검사`() {
        assertEquals(NicknameValidation.VALID, validateNickname("관악산ㄴ", composing = 3..3))
        assertEquals(NicknameValidation.VALID, validateNickname("관악산 ㄴ", composing = 4..4))
        assertEquals(NicknameValidation.EMPTY, validateNickname("ㄱ", composing = 0..0))
    }

    @Test
    fun `단어 전체를 조합 중이면 끝에 이어진 자모를 모두 빼고 검사`() {
        assertEquals(NicknameValidation.VALID, validateNickname("관악산ㄴ", composing = 0..3))
        assertEquals(NicknameValidation.VALID, validateNickname("관악산ㄴㄷ", composing = 0..4))
        assertEquals(NicknameValidation.EMPTY, validateNickname("ㄱㄴㄷ", composing = 0..2))
    }

    @Test
    fun `조합 범위 중간의 자모와 허용 외 문자는 INVALID`() {
        assertEquals(NicknameValidation.INVALID, validateNickname("관악ㄴ산ㄷ", composing = 0..4))
        assertEquals(NicknameValidation.INVALID, validateNickname("관악산^ㄴ", composing = 4..4))
    }

    @Test
    fun `빈 조합 범위는 그대로 검사`() {
        assertEquals(NicknameValidation.INVALID, validateNickname("관악산ㄴ", composing = 4 until 4))
    }

    @Test
    fun `조합이 끝난 자모는 INVALID`() {
        assertEquals(NicknameValidation.INVALID, validateNickname("관악산ㄴ"))
    }
}
