package com.routee.android.core.designsystem.component.textfield

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteeTextFieldStatusTest {

    /**
     * GIVEN: 포커스가 없고 검증 결과가 없을 때
     * WHEN: 상태를 정하면
     * THEN: Initial이다
     */
    @Test
    fun `포커스와 검증 결과가 없으면 Initial이다`() {
        assertEquals(RouteeTextFieldStatus.Initial, status(isFocused = false, RouteeTextFieldValidation.None))
    }

    /**
     * GIVEN: 포커스는 있고 검증 결과가 없을 때
     * WHEN: 상태를 정하면
     * THEN: Focus다
     */
    @Test
    fun `포커스만 있으면 Focus다`() {
        assertEquals(RouteeTextFieldStatus.Focus, status(isFocused = true, RouteeTextFieldValidation.None))
    }

    /**
     * GIVEN: 포커스가 있고 입력값이 유효할 때
     * WHEN: 상태를 정하면
     * THEN: Typing이다
     */
    @Test
    fun `포커스 중 유효하면 Typing이다`() {
        assertEquals(RouteeTextFieldStatus.Typing, status(isFocused = true, RouteeTextFieldValidation.Valid))
    }

    /**
     * GIVEN: 포커스가 없고 입력값이 유효할 때
     * WHEN: 상태를 정하면
     * THEN: Success다
     */
    @Test
    fun `포커스 없이 유효하면 Success다`() {
        assertEquals(RouteeTextFieldStatus.Success, status(isFocused = false, RouteeTextFieldValidation.Valid))
    }

    /**
     * GIVEN: 입력값이 유효하지 않을 때
     * WHEN: 포커스 여부와 관계없이 상태를 정하면
     * THEN: Error다
     */
    @Test
    fun `유효하지 않으면 포커스와 관계없이 Error다`() {
        assertEquals(RouteeTextFieldStatus.Error, status(isFocused = true, RouteeTextFieldValidation.Invalid))
        assertEquals(RouteeTextFieldStatus.Error, status(isFocused = false, RouteeTextFieldValidation.Invalid))
    }

    private fun status(
        isFocused: Boolean,
        validation: RouteeTextFieldValidation,
    ) = RouteeTextFieldStatus.from(isFocused = isFocused, validation = validation)
}
