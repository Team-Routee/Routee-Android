package com.routee.android.feature.onboarding.nickname.model

import com.routee.android.core.designsystem.component.textfield.RouteeTextFieldValidation
import org.junit.Assert.assertEquals
import org.junit.Test

class NicknameValidatorTest {

    /**
     * GIVEN: 아무것도 입력하지 않았을 때
     * WHEN: 검증하면
     * THEN: 검증 전 상태다
     */
    @Test
    fun `빈 값은 검증 전 상태다`() {
        assertEquals(RouteeTextFieldValidation.None, NicknameValidator.validate(""))
    }

    /**
     * GIVEN: 한글, 영문, 숫자와 단어 사이 단일 공백으로 된 닉네임일 때
     * WHEN: 검증하면
     * THEN: 유효하다
     */
    @Test
    fun `한글 영문 숫자와 단일 공백은 유효하다`() {
        assertEquals(RouteeTextFieldValidation.Valid, NicknameValidator.validate("관악산 날다람쥐"))
        assertEquals(RouteeTextFieldValidation.Valid, NicknameValidator.validate("Routee 99"))
    }

    /**
     * GIVEN: 입력 중 뒷공백이 붙어 있을 때
     * WHEN: 검증하면
     * THEN: 뒷공백은 포커스 아웃 때 지우므로 유효하다
     */
    @Test
    fun `뒷공백은 검증에서 제외한다`() {
        assertEquals(RouteeTextFieldValidation.Valid, NicknameValidator.validate("관악산 "))
    }

    /**
     * GIVEN: 특수문자나 완성되지 않은 자모가 들어 있을 때
     * WHEN: 검증하면
     * THEN: 유효하지 않다
     */
    @Test
    fun `허용 외 문자는 유효하지 않다`() {
        assertEquals(RouteeTextFieldValidation.Invalid, NicknameValidator.validate("관악산^&^"))
        assertEquals(RouteeTextFieldValidation.Invalid, NicknameValidator.validate("ㄱㄴ"))
        assertEquals(RouteeTextFieldValidation.Invalid, NicknameValidator.validate("관악산_날다람쥐"))
    }

    /**
     * GIVEN: 12자를 넘는 닉네임일 때
     * WHEN: 검증하면
     * THEN: 유효하지 않다
     */
    @Test
    fun `12자를 넘으면 유효하지 않다`() {
        assertEquals(RouteeTextFieldValidation.Valid, NicknameValidator.validate("가나다라마바사아자차카타"))
        assertEquals(RouteeTextFieldValidation.Invalid, NicknameValidator.validate("가나다라마바사아자차카타파"))
    }
}
