package com.routee.android.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.routee.android.core.database.dao.WorkoutDao
import com.routee.android.core.database.dao.WorkoutPhotoDao
import com.routee.android.core.database.entity.TrackPointEntity
import com.routee.android.core.database.entity.WorkoutEntity
import com.routee.android.core.database.entity.WorkoutPhotoEntity
import com.routee.android.core.database.util.InstantConverter

@Database(
    entities = [WorkoutEntity::class, TrackPointEntity::class, WorkoutPhotoEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2, spec = Migration1To2::class)],
)
@TypeConverters(InstantConverter::class)
internal abstract class RouteeDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    abstract fun workoutPhotoDao(): WorkoutPhotoDao
}

@DeleteColumn(tableName = "workout", columnName = "map_image_object_key")
@DeleteColumn(tableName = "workout", columnName = "map_image_path")
internal class Migration1To2 : AutoMigrationSpec
