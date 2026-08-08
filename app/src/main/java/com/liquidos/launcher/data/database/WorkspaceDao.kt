package com.liquidos.launcher.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.liquidos.launcher.model.WorkspaceItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {
    // Lấy toàn bộ icon trên một trang cụ thể, tự động update lên UI nhờ Flow
    @Query("SELECT * FROM workspace_items WHERE screenId = :screenId")
    fun getItemsForScreen(screenId: Int): Flow<List<WorkspaceItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WorkspaceItemEntity)

    @Update
    suspend fun updateItem(item: WorkspaceItemEntity)

    @Delete
    suspend fun deleteItem(item: WorkspaceItemEntity)
    
    @Query("DELETE FROM workspace_items WHERE packageName = :packageName")
    suspend fun deleteItemsByPackage(packageName: String)
}
