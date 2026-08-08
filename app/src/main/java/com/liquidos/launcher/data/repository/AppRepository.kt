package com.liquidos.launcher.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.liquidos.launcher.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

interface AppRepository {
    val installedApps: StateFlow<List<AppInfo>>
    fun reloadApps()
    fun launchApp(context: Context, packageName: String, className: String)
}

@Singleton
class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppRepository {

    private val packageManager: PackageManager = context.packageManager
    private val ioScope = CoroutineScope(Dispatchers.IO)
    
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    override val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    init {
        // Lấy danh sách app ngay khi khởi tạo
        reloadApps()
        registerPackageReceiver()
    }

    // Thuật toán lấy danh sách app tối ưu
    override fun reloadApps() {
        ioScope.launch {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            
            // Lấy danh sách các Activity có cờ LAUNCHER
            val resolveInfos: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)
            
            val apps = resolveInfos.mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val className = resolveInfo.activityInfo.name
                
                // Bỏ qua chính Launcher của chúng ta để không tự hiển thị icon của mình
                if (packageName == context.packageName) return@mapNotNull null

                val label = resolveInfo.loadLabel(packageManager).toString()
                val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                AppInfo(
                    packageName = packageName,
                    className = className,
                    label = label,
                    isSystemApp = isSystem
                )
            }.sortedBy { it.label.lowercase() } // Sắp xếp Alphabetically

            _installedApps.value = apps
        }
    }

    // Hàm khởi chạy ứng dụng mượt mà
    override fun launchApp(context: Context, packageName: String, className: String) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = android.content.ComponentName(packageName, className)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Có thể thêm Toast báo lỗi nếu app không tồn tại
        }
    }

    // Lắng nghe sự kiện cài đặt/gỡ cài đặt để tự động cập nhật danh sách
    private fun registerPackageReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Có thay đổi về package, gọi reload lại danh sách trên luồng IO
                reloadApps()
            }
        }
        context.registerReceiver(receiver, filter)
    }
}
