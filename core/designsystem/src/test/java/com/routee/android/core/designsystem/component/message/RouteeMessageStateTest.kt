package com.routee.android.core.designsystem.component.message

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouteeMessageStateTest {

    private val scope = TestScope(StandardTestDispatcher())
    private val state = RouteeMessageState(scope = scope, accessibilityManager = null)

    /**
     * GIVEN: 메시지가 없을 때
     * WHEN: 토스트를 띄우면
     * THEN: 코루틴이 돌기 전에 바로 현재 메시지가 된다
     */
    @Test
    fun `토스트를 띄우면 바로 현재 메시지가 된다`() {
        state.showToast(text = FIRST_TEXT)

        assertEquals(RouteeMessage.Toast(text = FIRST_TEXT), state.currentMessage)
    }

    /**
     * GIVEN: 토스트를 띄운 직후
     * WHEN: 바로 닫으면
     * THEN: 현재 메시지가 비워진다
     */
    @Test
    fun `토스트를 띄운 직후 닫으면 바로 닫힌다`() {
        state.showToast(text = FIRST_TEXT)
        state.dismiss()
        scope.runCurrent()

        assertNull(state.currentMessage)
    }

    /**
     * GIVEN: 토스트가 떠 있을 때
     * WHEN: 새 토스트를 띄우면
     * THEN: 새 토스트로 바뀐다
     */
    @Test
    fun `새 메시지가 오면 이전 메시지를 교체한다`() {
        state.showToast(text = FIRST_TEXT)
        state.showToast(text = SECOND_TEXT)
        scope.runCurrent()

        assertEquals(RouteeMessage.Toast(text = SECOND_TEXT), state.currentMessage)
    }

    /**
     * GIVEN: 첫 토스트가 1초 떠 있다가 새 토스트로 바뀌었을 때
     * WHEN: 첫 토스트가 원래 닫힐 시간이 지나면
     * THEN: 새 토스트는 그대로 떠 있다
     */
    @Test
    fun `이전 메시지가 닫혀도 새 메시지는 유지된다`() {
        state.showToast(text = FIRST_TEXT, duration = RouteeMessageDuration.SHORT)
        scope.advanceTimeBy(1_000)
        state.showToast(text = SECOND_TEXT, duration = RouteeMessageDuration.SHORT)
        scope.advanceTimeBy(1_500)

        assertEquals(RouteeMessage.Toast(text = SECOND_TEXT), state.currentMessage)
    }

    /**
     * GIVEN: 토스트를 띄웠을 때
     * WHEN: 노출 시간이 지나면
     * THEN: 시간 직전까지는 떠 있고, 지나면 닫힌다
     */
    @Test
    fun `노출 시간이 지나면 자동으로 닫힌다`() {
        state.showToast(text = FIRST_TEXT, duration = RouteeMessageDuration.SHORT)

        scope.advanceTimeBy(RouteeMessageDuration.SHORT.millis - 1)
        assertEquals(RouteeMessage.Toast(text = FIRST_TEXT), state.currentMessage)

        scope.advanceUntilIdle()
        assertNull(state.currentMessage)
    }

    /**
     * GIVEN: 버튼이 있는 스낵바가 떠 있을 때
     * WHEN: 버튼을 누르면
     * THEN: ACTION_PERFORMED를 돌려주고 닫힌다
     */
    @Test
    fun `스낵바 버튼을 누르면 ACTION_PERFORMED를 돌려준다`() {
        var result: RouteeMessageResult? = null
        scope.launch { result = state.showSnackbar(text = FIRST_TEXT, actionLabel = ACTION_LABEL) }
        scope.runCurrent()

        state.performAction()
        scope.runCurrent()

        assertEquals(RouteeMessageResult.ACTION_PERFORMED, result)
        assertNull(state.currentMessage)
    }

    /**
     * GIVEN: 스낵바가 떠 있을 때
     * WHEN: 버튼을 누르지 않고 노출 시간이 지나면
     * THEN: DISMISSED를 돌려준다
     */
    @Test
    fun `스낵바 시간이 지나면 DISMISSED를 돌려준다`() {
        var result: RouteeMessageResult? = null
        scope.launch { result = state.showSnackbar(text = FIRST_TEXT, actionLabel = ACTION_LABEL) }

        scope.advanceUntilIdle()

        assertEquals(RouteeMessageResult.DISMISSED, result)
        assertNull(state.currentMessage)
    }

    /**
     * GIVEN: 스낵바 결과를 기다리는 중일 때
     * WHEN: 새 토스트가 오면
     * THEN: 기다리던 스낵바는 DISMISSED로 끝나고 토스트가 뜬다
     */
    @Test
    fun `스낵바 중 새 메시지가 오면 이전 스낵바는 DISMISSED로 끝난다`() {
        var result: RouteeMessageResult? = null
        scope.launch { result = state.showSnackbar(text = FIRST_TEXT, actionLabel = ACTION_LABEL) }
        scope.runCurrent()

        state.showToast(text = SECOND_TEXT)
        scope.runCurrent()

        assertEquals(RouteeMessageResult.DISMISSED, result)
        assertEquals(RouteeMessage.Toast(text = SECOND_TEXT), state.currentMessage)
    }

    /**
     * GIVEN: 스낵바 결과를 기다리는 중일 때
     * WHEN: 기다리던 코루틴이 취소되면
     * THEN: 스낵바가 사라진다
     */
    @Test
    fun `스낵바를 기다리던 코루틴이 취소되면 메시지가 사라진다`() {
        val job = scope.launch { state.showSnackbar(text = FIRST_TEXT, actionLabel = ACTION_LABEL) }
        scope.runCurrent()
        assertTrue(state.currentMessage is RouteeMessage.Snackbar)

        job.cancel()
        scope.runCurrent()

        assertNull(state.currentMessage)
    }

    private companion object {
        const val FIRST_TEXT = "저장했어요"
        const val SECOND_TEXT = "삭제했어요"
        const val ACTION_LABEL = "취소"
    }
}
