package com.routee.android.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.routee.android.core.database.dao.WorkoutDao
import com.routee.android.core.database.dao.WorkoutPhotoDao
import com.routee.android.core.database.entity.TrackPointEntity
import com.routee.android.core.database.entity.WorkoutEntity
import com.routee.android.core.database.entity.WorkoutPhotoEntity
import com.routee.android.core.database.util.InstantConverter

@Database(
    entities = [WorkoutEntity::class, TrackPointEntity::class, WorkoutPhotoEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@TypeConverters(InstantConverter::class)
internal abstract class RouteeDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    abstract fun workoutPhotoDao(): WorkoutPhotoDao
}
