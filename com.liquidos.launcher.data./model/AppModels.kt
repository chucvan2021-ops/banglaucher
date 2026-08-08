package com.liquidos.launcher.model

import android.content.ComponentName
import androidx.room.Entity
import androidx.room.PrimaryKey

// Model đại diện cho một ứng dụng có thể khởi chạy trong ngăn kéo (App Drawer)
data class AppInfo(
    val packageName: String,
    val className: String, // Dùng để tạo ComponentName khởi chạy app
    val label: String,
    val isSystemApp: Boolean = false
) {
    val componentName: ComponentName
        get() = ComponentName(packageName, className)
}

// Model đại diện cho một icon/widget nằm trên lưới màn hình chính (Workspace)
@Entity(tableName = "workspace_items")
data class WorkspaceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val itemType: Int, // 0 = App Icon, 1 = Folder, 2 = Shizuku Shortcut
    val packageName: String?, // Null nếu là Folder
    val className: String?,   // Null nếu là Folder
    
    val cellX: Int, // Vị trí cột (Ví dụ: 0 đến 3 cho lưới 4x6)
    val cellY: Int, // Vị trí hàng (Ví dụ: 0 đến 5 cho lưới 4x6)
    val screenId: Int // Màn hình trang thứ mấy (0 là trang chủ)
)
