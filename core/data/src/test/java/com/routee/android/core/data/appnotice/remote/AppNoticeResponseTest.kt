package com.routee.android.core.data.appnotice.remote

import com.routee.android.core.data.appnotice.model.AppNotice
import com.routee.android.core.data.appnotice.model.AppNoticeConfig
import com.routee.android.core.data.appnotice.model.AppVersion
import com.routee.android.core.data.appnotice.remote.dto.response.AppNoticeResponse
import com.routee.android.core.data.appnotice.remote.dto.response.toAppNoticeConfig
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class AppNoticeResponseTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun parse(value: String): AppNoticeConfig =
        json.decodeFromString<AppNoticeResponse>(value).toAppNoticeConfig()

    @Test
    fun `필요한 필드만 넣으면 나머지는 기본값`() {
        val config = parse("""{"optionalUpdate":{"latestVersion":"1.2.0"}}""")
        assertEquals(AppNoticeConfig(latestVersion = AppVersion(1, 2, 0)), config)
    }

    @Test
    fun `점검이 꺼져 있으면 문구가 있어도 점검 아님, 빈 문구는 앱 기본 문구`() {
        val config = parse(
            """
            {
              "maintenance": { "enabled": false, "message": "점검" },
              "forceUpdate": { "minimumVersion": "1.x", "title": " ", "message": "안내\n문구" },
              "unknown": 1
            }
            """,
        )
        assertEquals(
            AppNoticeConfig(forceUpdate = AppNotice.ForceUpdate(title = null, message = "안내\n문구")),
            config,
        )
    }
}
