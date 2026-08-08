package com.liquidos.launcher.ui.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

// Mã Shader C++ (AGSL) để tạo ra chất lỏng lấp lánh
const val LIQUID_SHADER = """
    uniform float2 resolution;
    uniform float time;
    uniform shader composable;
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord.xy / resolution.xy;
        // Tạo gợn sóng dựa trên thời gian
        uv.x += sin(uv.y * 10.0 + time) * 0.02;
        uv.y += cos(uv.x * 10.0 + time) * 0.02;
        
        half4 color = composable.eval(uv * resolution);
        // Thêm độ phát sáng (glow)
        float glow = sin(time * 2.0) * 0.1 + 0.1;
        return color + half4(glow, glow, glow, 0.0);
    }
"""

@Composable
fun AnimatedLiquidDock(modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(0f) }

    // Vòng lặp thời gian để Shader chuyển động
    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameMillis { frameTime ->
                time = frameTime / 1000f
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .graphicsLayer {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val shader = RuntimeShader(LIQUID_SHADER)
                    shader.setFloatUniform("resolution", size.width, size.height)
                    shader.setFloatUniform("time", time)
                    renderEffect = RenderEffect.createRuntimeShaderEffect(
                        shader, "composable"
                    ).asComposeRenderEffect()
                } else {
                    // Fallback cho máy Android dưới 13: Dùng Blur tĩnh
                    renderEffect = androidx.compose.ui.graphics.BlurEffect(25f, 25f)
                }
            }
    ) {
        // Icon Dock sẽ nằm trong này
    }
}
