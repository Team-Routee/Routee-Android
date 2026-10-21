package com.routee.android.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.routee.android.core.database.dao.WorkoutDao
import com.routee.android.core.database.entity.TrackPointEntity
import com.routee.android.core.database.entity.WorkoutEntity
import com.routee.android.core.database.util.InstantConverter

@Database(
    entities = [WorkoutEntity::class, TrackPointEntity::class],
    version = 1,
)
@TypeConverters(InstantConverter::class)
internal abstract class RouteeDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}
