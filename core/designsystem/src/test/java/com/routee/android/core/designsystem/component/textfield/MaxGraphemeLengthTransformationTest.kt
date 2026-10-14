package com.routee.android.core.designsystem.component.textfield

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import org.junit.Assert.assertEquals
import org.junit.Test

class MaxGraphemeLengthTransformationTest {

    @Test
    fun `max length 이하 입력은 허용된다`() {
        assertEquals("12345", type(initial = "1234", input = "5", maxLength = 5))
    }

    @Test
    fun `max length 초과 입력은 차단된다`() {
        assertEquals("12345", type(initial = "12345", input = "6", maxLength = 5))
    }

    @Test
    fun `max length를 초과하는 기존 값에서 글자를 지우는 것은 허용된다`() {
        val state = TextFieldState("123456")
        state.edit {
            delete(5, 6)
            with(MaxGraphemeLengthTransformation(5)) { transformInput() }
        }
        assertEquals("12345", state.text.toString())
    }

    private fun type(
        initial: String,
        input: String,
        maxLength: Int,
    ): String {
        val state = TextFieldState(initial)
        state.edit {
            append(input)
            with(MaxGraphemeLengthTransformation(maxLength)) { transformInput() }
        }
        return state.text.toString()
    }
}
