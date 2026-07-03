package com.dragonic.system.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dragonic.system.data.model.IntruderLog
import com.dragonic.system.data.model.IntruderLogDao
import com.dragonic.system.data.model.OwnerFace
import com.dragonic.system.data.model.OwnerFaceDao

@Database(
    entities = [IntruderLog::class, OwnerFace::class],
    version = 1,
    exportSchema = false
)
abstract class DragonicDatabase : RoomDatabase() {
    abstract fun intruderLogDao(): IntruderLogDao
    abstract fun ownerFaceDao(): OwnerFaceDao
}
