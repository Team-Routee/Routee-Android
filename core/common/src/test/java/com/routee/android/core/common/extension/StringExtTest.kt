package com.routee.android.core.common.extension

import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtTest {

    /**
     * GIVEN: 한글, 영문, 숫자, 공백으로 된 문자열일 때
     * WHEN: graphemeLength를 호출하면
     * THEN: 글자 수를 그대로 센다
     */
    @Test
    fun `한글 영문 숫자 공백은 한 글자씩 센다`() {
        assertEquals(10, "관악산 Gwan 1".graphemeLength())
    }

    /**
     * GIVEN: 두 개의 코드로 이루어진 이모지가 있을 때
     * WHEN: graphemeLength를 호출하면
     * THEN: 이모지를 1자로 센다
     */
    @Test
    fun `이모지는 1자로 센다`() {
        assertEquals(1, "⛰️".graphemeLength())
        assertEquals(3, "산📍길".graphemeLength())
    }

    /**
     * GIVEN: 빈 문자열일 때
     * WHEN: graphemeLength를 호출하면
     * THEN: 0을 반환한다
     */
    @Test
    fun `빈 문자열은 0이다`() {
        assertEquals(0, "".graphemeLength())
    }
}
