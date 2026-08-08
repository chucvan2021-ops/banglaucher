package com.liquidos.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.liquidos.launcher.presentation.LauncherViewModel
import com.liquidos.launcher.ui.components.DynamicIslandOverlay
import com.liquidos.launcher.ui.screens.WorkspaceScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Kéo UI tràn xuống dưới Status Bar và Navigation Bar
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val showControlCenter = viewModel.showControlCenter.collectAsState().value
            
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Lớp dưới cùng: Hình nền và Lưới ứng dụng
                WorkspaceScreen(viewModel)
                
                // 2. Lớp giữa: Control Center (ẩn/hiện)
                if (showControlCenter) {
                    // IOSControlCenterScreen()
                }

                // 3. Lớp trên cùng: Dynamic Island
                DynamicIslandOverlay(viewModel)
            }
        }
    }

    // Chặn nút Back để không thoát Launcher
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (viewModel.showControlCenter.value) {
            viewModel.toggleControlCenter(false)
        }
        // Không gọi super.onBackPressed()
    }
}
