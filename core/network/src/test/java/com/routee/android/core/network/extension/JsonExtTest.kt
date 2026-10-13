package com.routee.android.core.network.extension

import org.junit.Assert.assertEquals
import org.junit.Test

class JsonExtTest {

    /**
     * GIVEN: 한 줄로 된 JSON 객체일 때
     * WHEN: toPrettyJsonOrSelf를 호출하면
     * THEN: 들여쓰기된 JSON으로 바꾼다
     */
    @Test
    fun `JSON 객체는 들여쓰기된 형태로 바꾼다`() {
        val result = """{"code":"OK","data":{"id":1}}""".toPrettyJsonOrSelf()

        assertEquals(
            """
            {
                "code": "OK",
                "data": {
                    "id": 1
                }
            }
            """.trimIndent(),
            result,
        )
    }

    /**
     * GIVEN: JSON이 아닌 로그 문자열일 때
     * WHEN: toPrettyJsonOrSelf를 호출하면
     * THEN: 원본을 그대로 반환한다
     */
    @Test
    fun `JSON이 아니면 원본을 반환한다`() {
        val message = "--> POST http://localhost/api/v1/auth/login"

        assertEquals(message, message.toPrettyJsonOrSelf())
    }

    /**
     * GIVEN: 중괄호로 시작하지만 깨진 JSON일 때
     * WHEN: toPrettyJsonOrSelf를 호출하면
     * THEN: 예외 없이 원본을 반환한다
     */
    @Test
    fun `깨진 JSON은 원본을 반환한다`() {
        val message = """{"code":"OK","""

        assertEquals(message, message.toPrettyJsonOrSelf())
    }
}
