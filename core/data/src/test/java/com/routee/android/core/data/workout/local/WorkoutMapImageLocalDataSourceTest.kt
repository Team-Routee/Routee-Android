package com.routee.android.core.data.workout.local

import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WorkoutMapImageLocalDataSourceTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var directory: File
    private lateinit var dataSource: WorkoutMapImageLocalDataSource

    @Before
    fun setUp() {
        directory = File(tempFolder.root, "workout_map")
        dataSource = WorkoutMapImageLocalDataSource { directory }
    }

    /**
     * GIVEN: 같은 기록일 때
     * WHEN: 캡처를 두 번 저장하면
     * THEN: 마지막 캡처 파일 하나만 남는다
     */
    @Test
    fun `같은 기록에 두 번 저장하면 마지막 캡처만 남는다`() = runTest {
        dataSource.save(activityId = 1, jpeg = byteArrayOf(1))
        val path = dataSource.save(activityId = 1, jpeg = byteArrayOf(2))

        assertEquals(listOf(File(path)), directory.listFiles()?.toList())
        assertTrue(File(path).readBytes().contentEquals(byteArrayOf(2)))
    }

    /**
     * GIVEN: 두 기록의 캡처와 쓰다 남은 임시 파일이 있을 때
     * WHEN: 한 기록을 지우면
     * THEN: 그 기록의 캡처와 임시 파일만 지워진다
     */
    @Test
    fun `기록을 지우면 그 기록의 캡처와 임시 파일만 지운다`() = runTest {
        dataSource.save(activityId = 1, jpeg = byteArrayOf(1))
        val other = dataSource.save(activityId = 12, jpeg = byteArrayOf(1))
        File(directory, "1-temp-123.tmp").writeBytes(byteArrayOf(1))

        dataSource.delete(activityId = 1)

        assertEquals(listOf(File(other)), directory.listFiles()?.toList())
    }
}
