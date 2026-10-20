package com.routee.android.feature.recordedit.editor.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RecapRecordUiModelTest {

    /**
     * GIVEN: 거리(m)가 있을 때
     * WHEN: 표시용 문자열로 바꾸면
     * THEN: km 단위 소수점 둘째 자리까지 보여준다
     */
    @Test
    fun `거리는 km 소수점 둘째 자리까지 보여준다`() {
        assertEquals("15.50km", RecapRecordUiModel.formatDistance(15_500))
        assertEquals("1.23km", RecapRecordUiModel.formatDistance(1_234))
        assertEquals("0.01km", RecapRecordUiModel.formatDistance(5))
    }

    /**
     * GIVEN: 운동 시간(초)이 있을 때
     * WHEN: 표시용 문자열로 바꾸면
     * THEN: 시간과 분으로 보여주고 초는 버린다
     */
    @Test
    fun `시간은 h m 형식으로 보여준다`() {
        assertEquals("3h 20m", RecapRecordUiModel.formatDuration(12_000))
        assertEquals("0h 1m", RecapRecordUiModel.formatDuration(119))
        assertEquals("10h 0m", RecapRecordUiModel.formatDuration(36_000))
    }

    /**
     * GIVEN: 최고 고도(m)가 있을 때
     * WHEN: 표시용 문자열로 바꾸면
     * THEN: 정수 m로 보여준다
     */
    @Test
    fun `최고 고도는 정수 m로 보여준다`() {
        assertEquals("2132m", RecapRecordUiModel.formatElevation(2132))
    }

    /**
     * GIVEN: 값이 0일 때
     * WHEN: 표시용 문자열로 바꾸면
     * THEN: 빈 문자열이다
     */
    @Test
    fun `값이 0이면 빈 문자열이다`() {
        assertEquals(RecapRecordUiModel.EMPTY, RecapRecordUiModel.from(0, 0, 0))
    }
}
