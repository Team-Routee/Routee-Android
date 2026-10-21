package com.routee.android.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.routee.android.core.database.dao.WorkoutDao
import com.routee.android.core.database.entity.ActivityEntity
import com.routee.android.core.database.entity.TrackPointEntity

@Database(
    entities = [ActivityEntity::class, TrackPointEntity::class],
    version = 1,
)
internal abstract class RouteeDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}
