package com.routee.android.core.domain.member

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateNicknameUseCaseTest {
    private val validateNickname = ValidateNicknameUseCase()

    @Test
    fun `한글 영문 숫자와 단어 사이 공백 한 칸은 통과`() {
        assertTrue(validateNickname("관악산 날다람쥐"))
        assertTrue(validateNickname("Routee 2026"))
    }

    @Test
    fun `12자까지 통과하고 13자부터 실패`() {
        assertTrue(validateNickname("가".repeat(12)))
        assertFalse(validateNickname("가".repeat(13)))
    }

    @Test
    fun `특수문자와 자모만 있는 값과 연속 공백과 앞뒤 공백과 빈 값은 실패`() {
        assertFalse(validateNickname("날다람쥐^&^"))
        assertFalse(validateNickname("ㄱㄴ"))
        assertFalse(validateNickname("관악산_날다람쥐"))
        assertFalse(validateNickname("관악산  날다람쥐"))
        assertFalse(validateNickname(" 날다람쥐"))
        assertFalse(validateNickname("날다람쥐 "))
        assertFalse(validateNickname(""))
    }
}
