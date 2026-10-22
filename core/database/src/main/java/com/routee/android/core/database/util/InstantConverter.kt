package com.routee.android.core.database.util

import androidx.room.TypeConverter
import java.time.Instant

/** `Instant`를 epoch ms로 저장 */
internal class InstantConverter {
    @TypeConverter
    fun toEpochMilli(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)
}
