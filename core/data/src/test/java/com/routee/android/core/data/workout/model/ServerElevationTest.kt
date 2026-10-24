package com.routee.android.core.data.workout.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ServerElevationTest {
    /**
     * GIVEN: 서버 허용 범위 안 고도일 때
     * WHEN: 서버로 보낼 고도로 바꾸면
     * THEN: 반올림만 한다
     */
    @Test
    fun `범위 안 고도는 반올림한다`() {
        assertEquals(302, 301.6.toServerElevation())
        assertEquals(-12, (-12.4).toServerElevation())
    }

    /**
     * GIVEN: GPS 고도가 서버 허용 범위(-500~9000)를 벗어났을 때
     * WHEN: 서버로 보낼 고도로 바꾸면
     * THEN: 범위 끝값으로 자른다
     */
    @Test
    fun `범위를 벗어난 고도는 끝값으로 자른다`() {
        assertEquals(-500, (-800.0).toServerElevation())
        assertEquals(9_000, 9_120.0.toServerElevation())
    }
}
