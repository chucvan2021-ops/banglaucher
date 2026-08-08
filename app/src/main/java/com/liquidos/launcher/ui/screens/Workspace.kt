package com.liquidos.launcher.ui.screens

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun WorkspaceScreen(viewModel: LauncherViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // XỬ LÝ CHỤM 2 NGÓN TAY (Pinch to zoom)
                detectTransformGestures { _, pan, zoom, _ ->
                    if (zoom < 0.9f) { // Nếu chụm lại
                        viewModel.toggleEditMode(true)
                    }
                    // Nếu vuốt mạnh xuống dưới, mở Control Center iOS
                    if (pan.y > 50f) {
                        viewModel.toggleControlCenter(true)
                    }
                }
            }
    ) {
        // Lưới icon nằm ở đây
        // ...

        // XỬ LÝ ẤN GIỮ ICON MỞ MENU NGỮ CẢNH (Context Menu)
        /* Modifier trên từng Icon sẽ có dạng:
           Modifier.pointerInput(Unit) {
               detectDragGesturesAfterLongPress(
                   onDragStart = { 
                       // Rung máy
                       // Hiện Menu tùy chọn (Xóa, Shizuku Force Stop, v.v.)
                   },
                   onDrag = { change, dragAmount -> 
                       // Di chuyển icon theo tay (Drag & Drop)
                   }
               )
           }
        */
    }
}
