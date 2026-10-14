package com.routee.android.core.designsystem.component.textfield

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import org.junit.Assert.assertEquals
import org.junit.Test

class NicknameSpaceTransformationTest {

    /**
     * GIVEN: 빈 입력창일 때
     * WHEN: 공백을 입력하면
     * THEN: 입력되지 않는다
     */
    @Test
    fun `앞 공백은 입력되지 않는다`() {
        assertEquals("", type(initial = "", input = " "))
    }

    /**
     * GIVEN: 공백으로 끝나는 입력값일 때
     * WHEN: 공백을 한 번 더 입력하면
     * THEN: 입력되지 않는다
     */
    @Test
    fun `연속 공백은 입력되지 않는다`() {
        assertEquals("관악 ", type(initial = "관악 ", input = " "))
    }

    /**
     * GIVEN: 단어 뒤일 때
     * WHEN: 공백을 한 번 입력하면
     * THEN: 입력된다
     */
    @Test
    fun `단어 사이 단일 공백은 입력된다`() {
        assertEquals("관악 산", type(initial = "관악", input = " 산"))
    }

    /**
     * GIVEN: 연속 공백이 들어 있는 문자열을
     * WHEN: 붙여넣으면
     * THEN: 전체가 입력되지 않는다
     */
    @Test
    fun `연속 공백이 있는 문자열은 입력되지 않는다`() {
        assertEquals("관악", type(initial = "관악", input = "  산"))
    }

    /**
     * GIVEN: 단어 사이 글자를 지우면 공백이 붙게 될 때
     * WHEN: 그 글자를 지우면
     * THEN: 지워진다
     */
    @Test
    fun `글자를 지워 생기는 연속 공백은 막지 않는다`() {
        val state = TextFieldState("관악 산 지기")
        state.edit {
            delete(3, 4)
            with(NicknameSpaceTransformation) { transformInput() }
        }

        assertEquals("관악  지기", state.text.toString())
    }

    /**
     * GIVEN: 글자를 지워 연속 공백이 이미 있는 입력값일 때
     * WHEN: 다른 글자를 입력하면
     * THEN: 입력된다
     */
    @Test
    fun `이미 있는 연속 공백 때문에 다른 글자 입력이 막히지 않는다`() {
        assertEquals("관악  지기산", type(initial = "관악  지기", input = "산"))
    }

    private fun type(
        initial: String,
        input: String,
    ): String {
        val state = TextFieldState(initial)
        state.edit {
            append(input)
            with(NicknameSpaceTransformation) { transformInput() }
        }
        return state.text.toString()
    }
}
