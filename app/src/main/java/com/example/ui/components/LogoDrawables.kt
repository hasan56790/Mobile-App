package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp

@Composable
fun AlNasrBrandingLogo(
    modifier: Modifier = Modifier,
    goldColor: Color = Color(0xFFD4AF37)
) {
    // Elegant pulsing glow to represent the spiritual aura of luxury scent
    val infiniteTransition = rememberInfiniteTransition(label = "logo_glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Canvas(modifier = modifier.size(140.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2

        // Gold Gradient Paint
        val goldGradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF8B6508),
                Color(0xFFD4AF37),
                Color(0xFFFFF68F),
                Color(0xFFD4AF37),
                Color(0xFF8B6508)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )

        // Draw Outer Golden Star-Medallion/Arched Frame
        val archPath = Path().apply {
            // Upper Dome shape
            moveTo(cx, h * 0.10f * scale)
            cubicTo(cx - w * 0.20f, h * 0.15f, cx - w * 0.35f, h * 0.28f, cx - w * 0.35f, h * 0.45f)
            lineTo(cx - w * 0.35f, h * 0.82f)
            quadraticTo(cx - w * 0.35f, h * 0.88f, cx, h * 0.88f)
            quadraticTo(cx + w * 0.35f, h * 0.88f, cx + w * 0.35f, h * 0.82f)
            lineTo(cx + w * 0.35f, h * 0.45f)
            cubicTo(cx + w * 0.35f, h * 0.28f, cx + w * 0.20f, h * 0.15f, cx, h * 0.10f * scale)
            close()
        }

        // Draw double arch outline
        drawPath(
            path = archPath,
            brush = goldGradient,
            style = Stroke(width = 3.dp.toPx())
        )

        // Inner slightly offset arch
        val innerArchPath = Path().apply {
            moveTo(cx, h * 0.14f)
            cubicTo(cx - w * 0.17f, h * 0.18f, cx - w * 0.30f, h * 0.30f, cx - w * 0.30f, h * 0.45f)
            lineTo(cx - w * 0.30f, h * 0.80f)
            quadraticTo(cx - w * 0.30f, h * 0.84f, cx, h * 0.84f)
            quadraticTo(cx + w * 0.30f, h * 0.84f, cx + w * 0.30f, h * 0.80f)
            lineTo(cx + w * 0.30f, h * 0.45f)
            cubicTo(cx + w * 0.30f, h * 0.30f, cx + w * 0.17f, h * 0.18f, cx, h * 0.14f)
            close()
        }
        drawPath(
            path = innerArchPath,
            brush = goldGradient,
            style = Stroke(width = 1.dp.toPx())
        )

        // Draw Central Al-Nasr Minaret Point
        drawRect(
            brush = goldGradient,
            topLeft = Offset(cx - 3.dp.toPx(), h * 0.24f),
            size = Size(6.dp.toPx(), h * 0.22f)
        )

        // Top Minaret Dome Finial Peak
        val finialPath = Path().apply {
            moveTo(cx, h * 0.16f)
            lineTo(cx - 6.dp.toPx(), h * 0.22f)
            lineTo(cx + 6.dp.toPx(), h * 0.22f)
            close()
        }
        drawPath(path = finialPath, brush = goldGradient)
        drawCircle(
            brush = goldGradient,
            radius = 3.dp.toPx(),
            center = Offset(cx, h * 0.14f)
        )

        // Swirling Arabic Calligraphy Stylized Outlines
        val calligraphyPath1 = Path().apply {
            moveTo(cx - w * 0.22f, cy)
            quadraticTo(cx - w * 0.05f, cy + h * 0.18f, cx, cy + h * 0.18f)
            quadraticTo(cx + w * 0.22f, cy + h * 0.18f, cx + w * 0.22f, cy - h * 0.02f)
            quadraticTo(cx + w * 0.20f, cy - h * 0.12f, cx + w * 0.10f, cy - h * 0.05f)
            quadraticTo(cx, cy + h * 0.02f, cx - w * 0.10f, cy + h * 0.05f)
            quadraticTo(cx - w * 0.18f, cy + h * 0.05f, cx - w * 0.15f, cy - h * 0.08f)
        }
        drawPath(
            path = calligraphyPath1,
            brush = goldGradient,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Secondary swooshing line
        val calligraphyPath2 = Path().apply {
            moveTo(cx - w * 0.12f, cy + h * 0.10f)
            quadraticTo(cx + w * 0.02f, cy + h * 0.25f, cx + w * 0.15f, cy + h * 0.05f)
        }
        drawPath(
            path = calligraphyPath2,
            brush = goldGradient,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Little diamond highlights
        drawRect(
            brush = goldGradient,
            topLeft = Offset(cx - 4.dp.toPx(), h * 0.81f),
            size = Size(8.dp.toPx(), 8.dp.toPx())
        )
    }
}

@Composable
fun PerfumeBottleIllustration(
    modifier: Modifier = Modifier,
    bottleColor: Color,
    bottleGradientColor: Color
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2

        // Soft Golden Radial Background Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x19D4AF37), Color.Transparent),
                center = Offset(cx, cy + h * 0.05f),
                radius = w * 0.65f
            )
        )

        // 1. Crystal Stopper (The Crown)
        val crownPath = Path().apply {
            moveTo(cx - w * 0.12f, h * 0.12f)
            lineTo(cx + w * 0.12f, h * 0.12f)
            quadraticTo(cx + w * 0.16f, h * 0.18f, cx + w * 0.10f, h * 0.24f)
            lineTo(cx - w * 0.10f, h * 0.24f)
            quadraticTo(cx - w * 0.16f, h * 0.18f, cx - w * 0.12f, h * 0.12f)
            close()
        }
        
        val metalGoldGradient = Brush.linearGradient(
            colors = listOf(Color(0xFF8B6508), Color(0xFFD4AF37), Color(0xFFFFF68F), Color(0xFF8B6508)),
            start = Offset(cx - w * 0.15f, h * 0.1f),
            end = Offset(cx + w * 0.15f, h * 0.3f)
        )
        // Draw the stopper
        drawPath(path = crownPath, brush = metalGoldGradient)

        // Small gold gem on top of the crown
        drawCircle(
            brush = metalGoldGradient,
            radius = w * 0.04f,
            center = Offset(cx, h * 0.09f)
        )

        // 2. Heavy Gold Ring Collar (Neck)
        drawRoundRect(
            brush = metalGoldGradient,
            topLeft = Offset(cx - w * 0.07f, h * 0.24f),
            size = Size(w * 0.14f, h * 0.07f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // 3. Main Glass Body (Symmetrical Oriental Chalice shape)
        val bodyPath = Path().apply {
            moveTo(cx - w * 0.08f, h * 0.31f) // Connect to neck
            lineTo(cx + w * 0.08f, h * 0.31f)
            cubicTo(cx + w * 0.25f, h * 0.34f, cx + w * 0.38f, h * 0.45f, cx + w * 0.38f, h * 0.62f)
            cubicTo(cx + w * 0.38f, h * 0.76f, cx + w * 0.26f, h * 0.88f, cx + w * 0.18f, h * 0.90f)
            lineTo(cx - w * 0.18f, h * 0.90f)
            cubicTo(cx - w * 0.26f, h * 0.88f, cx - w * 0.38f, h * 0.76f, cx - w * 0.38f, h * 0.62f)
            cubicTo(cx - w * 0.38f, h * 0.45f, cx - w * 0.25f, h * 0.34f, cx - w * 0.08f, h * 0.31f)
            close()
        }

        // Draw outer glass chassis with custom color gradients
        val glassLiquidGradient = Brush.radialGradient(
            colors = listOf(
                bottleColor,
                bottleGradientColor,
                Color(0xFF0C0C0C)
            ),
            center = Offset(cx, cy + h * 0.10f),
            radius = w * 0.55f
        )
        drawPath(path = bodyPath, brush = glassLiquidGradient)

        // 4. Fill Level / Glowing Liquid Core (Inner slightly smaller path clipped to create a luxury liquid look)
        val liquidPath = Path().apply {
            moveTo(cx - w * 0.06f, h * 0.35f)
            lineTo(cx + w * 0.06f, h * 0.35f)
            cubicTo(cx + w * 0.20f, h * 0.38f, cx + w * 0.32f, h * 0.48f, cx + w * 0.32f, h * 0.62f)
            cubicTo(cx + w * 0.32f, h * 0.74f, cx + w * 0.22f, h * 0.84f, cx + w * 0.15f, h * 0.86f)
            lineTo(cx - w * 0.15f, h * 0.86f)
            cubicTo(cx - w * 0.22f, h * 0.84f, cx - w * 0.32f, h * 0.74f, cx - w * 0.32f, h * 0.62f)
            cubicTo(cx - w * 0.32f, h * 0.48f, cx - w * 0.20f, h * 0.38f, cx - w * 0.06f, h * 0.35f)
            close()
        }
        
        clipPath(path = liquidPath) {
            // Draw a shiny internal liquid look
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x22FFFFFF),
                        bottleColor.copy(alpha = 0.95f),
                        bottleGradientColor.copy(alpha = 0.98f)
                    )
                ),
                topLeft = Offset(cx - w * 0.4f, h * 0.32f),
                size = Size(w * 0.8f, h * 0.58f)
            )

            // Inner glass bubbles/swirls representing concentrated attar oil particles
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = w * 0.04f,
                center = Offset(cx - w * 0.1f, h * 0.55f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = w * 0.02f,
                center = Offset(cx + w * 0.12f, h * 0.65f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = w * 0.03f,
                center = Offset(cx - w * 0.05f, h * 0.72f)
            )
        }

        // 5. Metal Filigree Arabic Arch Carvings (Overlaid on Glass Body)
        drawPath(
            path = liquidPath,
            brush = metalGoldGradient,
            style = Stroke(width = 2.dp.toPx())
        )

        // Center Gold Medallion on the bottle
        drawCircle(
            brush = metalGoldGradient,
            radius = w * 0.08f,
            center = Offset(cx, h * 0.58f)
        )
        // Little central ornament in medallion
        drawRect(
            color = Color(0xFF0C0C0C),
            topLeft = Offset(cx - w * 0.03f, h * 0.55f),
            size = Size(w * 0.06f, h * 0.06f)
        )
        drawCircle(
            color = bottleColor,
            radius = w * 0.02f,
            center = Offset(cx, h * 0.58f)
        )

        // Secondary line decorations inside the bottle frame
        val filigreePath = Path().apply {
            moveTo(cx - w * 0.25f, h * 0.50f)
            quadraticTo(cx, h * 0.38f, cx + w * 0.25f, h * 0.50f)
            
            moveTo(cx - w * 0.22f, h * 0.70f)
            quadraticTo(cx, h * 0.82f, cx + w * 0.22f, h * 0.70f)
        }
        drawPath(
            path = filigreePath,
            brush = metalGoldGradient,
            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f)))
        )

        // 6. Heavy Base Plinth
        val plinthPath = Path().apply {
            moveTo(cx - w * 0.15f, h * 0.88f)
            lineTo(cx + w * 0.15f, h * 0.88f)
            lineTo(cx + w * 0.20f, h * 0.94f)
            lineTo(cx - w * 0.20f, h * 0.94f)
            close()
        }
        drawPath(path = plinthPath, brush = metalGoldGradient)

        // Absolute Glass Highlight/Reflex Glimmer Overlay (curves on the left glass wall)
        val highlightPath = Path().apply {
            moveTo(cx - w * 0.32f, h * 0.45f)
            quadraticTo(cx - w * 0.34f, h * 0.62f, cx - w * 0.26f, h * 0.78f)
        }
        drawPath(
            path = highlightPath,
            color = Color.White.copy(alpha = 0.25f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
