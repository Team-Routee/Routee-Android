package com.routee.android.feature.onboarding.terms.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TermsUiStateTest {

    /**
     * GIVEN: 필수 약관 4개만 체크했을 때
     * WHEN: 상태를 보면
     * THEN: 다음 버튼은 활성, 모두 동의는 해제 상태다
     */
    @Test
    fun `필수 4개만 동의하면 다음이 활성화된다`() {
        val state = TermsUiState(checkedItems = TermsItem.entries.filter { it.isRequired }.toSet())

        assertTrue(state.isNextEnabled)
        assertFalse(state.isAllChecked)
    }

    /**
     * GIVEN: 필수 약관 하나를 빼고 선택 약관을 체크했을 때
     * WHEN: 상태를 보면
     * THEN: 다음 버튼은 비활성이다
     */
    @Test
    fun `필수가 하나라도 빠지면 다음이 비활성이다`() {
        val state =
            TermsUiState(
                checkedItems = setOf(TermsItem.SERVICE, TermsItem.PRIVACY, TermsItem.LOCATION, TermsItem.MARKETING),
            )

        assertFalse(state.isNextEnabled)
    }

    /**
     * GIVEN: 5개를 모두 체크했을 때
     * WHEN: 상태를 보면
     * THEN: 모두 동의 상태다
     */
    @Test
    fun `5개 모두 체크하면 모두 동의 상태다`() {
        val state = TermsUiState(checkedItems = TermsItem.entries.toSet())

        assertTrue(state.isAllChecked)
        assertTrue(state.isNextEnabled)
    }
}
