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
}
