package com.liquidos.launcher.data.shizuku

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShizukuManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Trạng thái Shizuku có đang chạy (đã bật qua Wireless Debugging) hay không
    private val _isShizukuAvailable = MutableStateFlow(false)
    val isShizukuAvailable: StateFlow<Boolean> = _isShizukuAvailable.asStateFlow()

    // Trạng thái người dùng đã cấp quyền API cho Launcher chưa
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    init {
        checkStatus()
        registerListeners()
    }

    private fun checkStatus() {
        try {
            val available = Shizuku.pingBinder()
            _isShizukuAvailable.value = available
            if (available) {
                checkAndSyncPermission()
            }
        } catch (e: Throwable) {
            _isShizukuAvailable.value = false
            _hasPermission.value = false
        }
    }

    private fun checkAndSyncPermission() {
        if (Shizuku.isPreV23()) {
            _hasPermission.value = true
        } else {
            try {
                val granted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                _hasPermission.value = granted
            } catch (e: Throwable) {
                _hasPermission.value = false
            }
        }
    }

    private fun registerListeners() {
        // Lắng nghe khi dịch vụ Shizuku khởi động hoặc tắt
        Shizuku.addBinderReceivedListener(binderListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        
        // Lắng nghe kết quả hộp thoại xin quyền
        Shizuku.addRequestPermissionResultListener(permissionListener)
    }

    private val binderListener = Shizuku.OnBinderReceivedListener {
        _isShizukuAvailable.value = true
        checkAndSyncPermission()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _isShizukuAvailable.value = false
        _hasPermission.value = false
    }

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == REQUEST_CODE_PERMISSION) {
            _hasPermission.value = (grantResult == PackageManager.PERMISSION_GRANTED)
        }
    }

    // Gọi hàm này từ UI để hiển thị hộp thoại xin quyền Shizuku
    fun requestPermission() {
        if (!_isShizukuAvailable.value) return
        try {
            if (!(_hasPermission.value)) {
                Shizuku.requestPermission(REQUEST_CODE_PERMISSION)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * Thực thi lệnh Shell bất kỳ thông qua tiến trình của Shizuku.
     * Trả về kết quả đầu ra (stdout/stderr) dưới dạng chuỗi.
     */
    suspend fun executeCommand(command: String): String = withContext(Dispatchers.IO) {
        if (!isReady()) return@withContext "Error: Shizuku not ready or permission denied."
        
        var process: Process? = null
        var reader: BufferedReader? = null
        val output = StringBuilder()

        try {
            // Sử dụng Shizuku.newProcess để chạy lệnh với quyền shell hệ thống
            process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            reader = BufferedReader(InputStreamReader(process.inputStream))
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
        } catch (e: Throwable) {
            e.printStackTrace()
            output.append("Exception: ").append(e.message)
        } finally {
            try {
                reader?.close()
                process?.destroy()
            } catch (ignored: Exception) {}
        }
        output.toString().trim()
    }

    fun isReady(): Boolean {
        return _isShizukuAvailable.value && _hasPermission.value
    }

    // --- CÁC TÍNH NĂNG NÂNG CAO CHO LAUNCHER ---

    /**
     * 1. Buộc dừng ứng dụng ngay lập tức (Force Stop) qua Activity Manager
     */
    suspend fun forceStopApp(packageName: String): Boolean {
        val result = executeCommand("am force-stop $packageName")
        return result.isEmpty()
    }

    /**
     * 2. Gỡ cài đặt im lặng (Silent Uninstall), kể cả app hệ thống ở user 0
     */
    suspend fun silentUninstall(packageName: String): Boolean {
        val result = executeCommand("pm uninstall --user 0 $packageName")
        return result.contains("Success", ignoreCasey = true)
    }

    /**
     * 3. Tối ưu hóa tốc độ hệ thống: Ép các thông số hình động (animation scale) về 0.5x
     * Giúp hệ điều hành phản hồi nhanh và giảm cảm giác giật khung hình khi vuốt app.
     */
    suspend fun optimizeSystemSpeed(): Boolean {
        val cmd = """
            settings put global window_animation_scale 0.5
            settings put global transition_animation_scale 0.5
            settings put global animator_duration_scale 0.5
        """.trimIndent()
        val result = executeCommand(cmd)
        return result.isEmpty()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION = 1001
    }
}
