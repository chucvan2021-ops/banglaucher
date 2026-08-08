package com.liquidos.launcher.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.liquidos.launcher.model.WorkspaceItemEntity

@Database(entities = [WorkspaceItemEntity::class], version = 1, exportSchema = false)
abstract class LauncherDatabase : RoomDatabase() {
    abstract val workspaceDao: WorkspaceDao
}
