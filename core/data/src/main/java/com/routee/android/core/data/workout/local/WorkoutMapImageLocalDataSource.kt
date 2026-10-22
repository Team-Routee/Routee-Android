package com.routee.android.core.data.workout.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 운동 기록 지도 캡처 파일, 기록마다 한 장 */
internal class WorkoutMapImageLocalDataSource(
    private val directory: () -> File,
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : this({ File(context.filesDir, DIRECTORY) })

    /** 캡처 저장, 같은 기록이면 덮어씀. 파일 경로 반환 */
    suspend fun save(
        activityId: Long,
        jpeg: ByteArray,
    ): String = withContext(Dispatchers.IO) {
        val dir = directory().apply { mkdirs() }
        val target = File(dir, "$activityId$IMAGE_SUFFIX")
        val temp = File.createTempFile("$activityId$TEMP_SEPARATOR", TEMP_SUFFIX, dir)
        try {
            temp.writeBytes(jpeg)
            Files.move(
                temp.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } finally {
            temp.delete()
        }
        target.path
    }

    /** 기록의 캡처와 남은 임시 파일 삭제 */
    suspend fun delete(activityId: Long) {
        withContext(Dispatchers.IO) {
            directory()
                .listFiles { file ->
                    file.name == "$activityId$IMAGE_SUFFIX" || file.name.startsWith("$activityId$TEMP_SEPARATOR")
                }?.forEach(File::delete)
        }
    }

    /** 모든 캡처 삭제 */
    suspend fun clear() {
        withContext(Dispatchers.IO) { directory().deleteRecursively() }
    }

    private companion object {
        const val DIRECTORY = "workout_map"
        const val IMAGE_SUFFIX = ".jpg"
        const val TEMP_SEPARATOR = "-temp-"
        const val TEMP_SUFFIX = ".tmp"
    }
}
