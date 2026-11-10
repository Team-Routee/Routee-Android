package com.routee.android.core.data.appnotice.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNoticeConfigTest {
    @Test
    fun `빠진 자리는 0으로 읽고 형식이 틀리면 null`() {
        assertEquals(AppVersion(1, 0, 0), AppVersion.parse("1.0"))
        assertEquals(AppVersion(1, 2, 3), AppVersion.parse(" 1.2.3 "))
        assertNull(AppVersion.parse("1.0.0-qa"))
        assertNull(AppVersion.parse(""))
        assertNull(AppVersion.parse("1.2.x"))
        assertNull(AppVersion.parse("1.2.3.4"))
        assertNull(AppVersion.parse("-1.0.0"))
    }

    @Test
    fun `자리마다 숫자로 비교`() {
        assertTrue(AppVersion(1, 9, 0) < AppVersion(1, 10, 0))
        assertTrue(AppVersion(2, 0, 0) > AppVersion(1, 99, 99))
    }

    @Test
    fun `점검이 버전보다 먼저`() {
        val config = AppNoticeConfig(
            maintenance = AppNotice.Maintenance(),
            minimumVersion = AppVersion(9, 0, 0),
        )
        assertEquals(AppNotice.Maintenance(), config.noticeFor("1.0.0"))
    }

    @Test
    fun `최소 버전보다 낮으면 강제, 최신 버전보다 낮으면 권장`() {
        val config = AppNoticeConfig(minimumVersion = AppVersion(1, 1, 0), latestVersion = AppVersion(1, 2, 0))
        assertEquals(AppNotice.ForceUpdate(), config.noticeFor("1.0.9"))
        assertEquals(AppNotice.OptionalUpdate(), config.noticeFor("1.1.0"))
        assertNull(config.noticeFor("1.2.0"))
    }

    @Test
    fun `설정이 비었거나 앱 버전을 못 읽으면 안내 없음`() {
        assertNull(AppNoticeConfig().noticeFor("1.0"))
        assertNull(AppNoticeConfig(minimumVersion = AppVersion(9, 0, 0)).noticeFor("dev"))
    }
}
