package com.example.study.presentation.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.animation.LoadingScreen

data class LevelNode(
    val index: Int,
    val isUnlocked: Boolean
)

@Composable
fun LevelsJourneyMap(
    isLoading: Boolean,
    levels: List<LevelNode>,
    currentLevelIndex: Int,
    hasPlayedAnimation: Boolean,
    onAnimationFinished: () -> Unit,
    onNodeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center

        ) {
            LoadingScreen()
        }
    } else {

        val colorScheme = MaterialTheme.colorScheme
        val textMeasurer = rememberTextMeasurer()

        val nodePositions = remember { mutableStateListOf<Offset>() }

        // One PathMeasure for everything
        val pathMeasure = remember { android.graphics.PathMeasure() }
        var totalLength by remember { mutableStateOf(0f) }

        val pathAnim = remember { Animatable(0f) }
        var startPulse by remember { mutableStateOf(false) }

        // Pulse starts only after path animation finishes
        val pulseScale by animateFloatAsState(
            targetValue = if (startPulse) 1.15f else 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = ""
        )

        LaunchedEffect(currentLevelIndex, totalLength, hasPlayedAnimation) {
            if (totalLength <= 0f) return@LaunchedEffect

            val step = totalLength / (levels.size + 1)
            val targetDistance = step * currentLevelIndex

            if (hasPlayedAnimation) {
                // If animation already played, jump to the target directly
                pathAnim.snapTo(targetDistance)
                startPulse = true
            } else {
                // Otherwise, run the animation
                startPulse = false
                pathAnim.snapTo(0f)
                pathAnim.animateTo(
                    targetValue = targetDistance,
                    animationSpec = tween(
                        durationMillis = 1400,
                        easing = FastOutSlowInEasing
                    )
                )
                startPulse = true
                onAnimationFinished() // Mark as played
            }
        }

        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(420.dp)
                .pointerInput(levels) {
                    detectTapGestures { tap ->
                        nodePositions.forEachIndexed { i, center ->
                            if ((tap - center).getDistance() < 42f) {
                                onNodeClick(i + 1)
                            }
                        }
                    }
                }
        ) {
            nodePositions.clear()

            val w = size.width
            val h = size.height

            // Snake Path
            val path = Path().apply {
                moveTo(w * 0.85f, h * 0.08f)
                cubicTo(w * 0.1f, h * 0.18f, w * 0.9f, h * 0.30f, w * 0.5f, h * 0.40f)
                cubicTo(w * 0.1f, h * 0.50f, w * 0.9f, h * 0.62f, w * 0.4f, h * 0.72f)
                cubicTo(w * 0.2f, h * 0.82f, w * 0.6f, h * 0.92f, w * 0.15f, h * 0.96f)
            }

            // Update measure based on actual drawn path
            pathMeasure.setPath(path.asAndroidPath(), false)
            val len = pathMeasure.length
            if (kotlin.math.abs(len - totalLength) > 0.5f) totalLength = len

            // Base road
            drawPath(
                path = path,
                color = colorScheme.surfaceVariant,
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )

            // Animated filled segment
            val segment = android.graphics.Path()
            val end = pathAnim.value.coerceIn(0f, len)
            pathMeasure.getSegment(0f, end, segment, true)

            drawPath(
                path = segment.asComposePath(),
                color = colorScheme.primary,
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )

            // Nodes positions
            val step = if (len > 0f) len / (levels.size + 1) else 0f
            val pos = FloatArray(2)

            levels.forEachIndexed { i, level ->
                val dist = step * (i + 1)
                pathMeasure.getPosTan(dist, pos, null)

                val center = Offset(pos[0], pos[1])
                nodePositions.add(center)

                val isCurrent = level.index == currentLevelIndex

                drawCoinPolished(
                    center = center,
                    radius = 26f,
                    scale = if (isCurrent) pulseScale else 1f,
                    isLocked = !level.isUnlocked,
                    isCurrent = isCurrent,
                    primary = colorScheme.primary,
                    outline = colorScheme.outline
                )

                val label = when (i) {
                    0 -> "المرحلة\nالأولى"
                    1 -> "المرحلة\nالثانية"
                    2 -> "المرحلة\nالثالثة"
                    3 -> "المرحلة\nالرابعة"
                    4 -> "المرحلة\nالخامسة"
                    5 -> "المرحلة\nالسادسة"
                    else -> ""
                }

                val textLayout = textMeasurer.measure(
                    AnnotatedString(label),
                    style = TextStyle(
                        color = colorScheme.onSurface,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                )

                val nodeRadius = 26f
                val gap = 20f
                val isRight = center.x > size.width / 2

                val offset = if (isRight) {
                    Offset(
                        center.x - nodeRadius - gap - textLayout.size.width,
                        center.y - textLayout.size.height / 2
                    )
                } else {
                    Offset(
                        center.x + nodeRadius + gap,
                        center.y - textLayout.size.height / 2
                    )
                }

                // padding around text background
                val padX = 14f
                val padY = 10f

                val bgTopLeft = Offset(offset.x - padX, offset.y - padY)
                val bgSize = androidx.compose.ui.geometry.Size(
                    width = textLayout.size.width + padX * 2,
                    height = textLayout.size.height + padY * 2
                )

                drawRoundRect(
                    color = colorScheme.surface.copy(alpha = 0.90f),
                    topLeft = bgTopLeft,
                    size = bgSize,
                    cornerRadius = CornerRadius(18f, 18f)
                )

                drawRoundRect(
                    color = colorScheme.outline.copy(alpha = 0.25f),
                    topLeft = bgTopLeft,
                    size = bgSize,
                    cornerRadius = CornerRadius(18f, 18f),
                    style = Stroke(width = 1.5f)
                )

                drawText(
                    textLayoutResult = textLayout,
                    topLeft = offset
                )
            }

            // ✅ FLAGS: place them exactly on the road using PathMeasure points
            val endPointArr = FloatArray(2)   // start of path (top/right) -> END flag (gold)
            val startPointArr = FloatArray(2) // end of path (bottom/left) -> START flag (green)

            pathMeasure.getPosTan(0f, endPointArr, null)
            pathMeasure.getPosTan(len, startPointArr, null)

            val endFlagPosition = Offset(endPointArr[0], endPointArr[1])
            val startFlagPosition = Offset(startPointArr[0], startPointArr[1])

            val startFlagColor = Color(0xFFFFC107)                // green-ish (matches your theme)
            val endFlagColor = colorScheme.primary                // gold
            val poleColor = colorScheme.outline

            // Start flag (bottom-left) = green
            drawFlag(
                position = startFlagPosition,
                flagColor = startFlagColor,
                poleColor = poleColor
            )

            // End flag (top-right) = gold
            drawFlag(
                position = endFlagPosition,
                flagColor = endFlagColor,
                poleColor = poleColor
            )
        }
    }
}

fun DrawScope.drawFlag(
    position: Offset,
    flagColor: Color,
    poleColor: Color
) {
    // Pole (keep same)
    drawLine(
        color = poleColor,
        start = position,
        end = Offset(position.x, position.y - 60f),
        strokeWidth = 8f,
        cap = StrokeCap.Round
    )

    // Flag cloth (keep same)
    val flagPath = Path().apply {
        moveTo(position.x, position.y - 60f)
        cubicTo(
            position.x + 45f, position.y - 55f,
            position.x + 45f, position.y - 25f,
            position.x, position.y - 30f
        )
        close()
    }

    drawPath(flagPath, flagColor)
}

fun DrawScope.drawCoinPolished(
    center: Offset,
    radius: Float,
    scale: Float,
    isLocked: Boolean,
    isCurrent: Boolean,
    primary: Color,
    outline: Color
) {
    val r = radius * scale

    val base = when {
        isLocked -> outline.copy(alpha = 0.55f)
        else -> primary
    }
    val rim = when {
        isLocked -> outline.copy(alpha = 0.60f)
        else -> base.copy(alpha = 0.90f)
    }

    val face = when {

        isLocked -> outline.copy(alpha = 0.45f)
        else ->
            base.copy(alpha = 0.78f)

    }


    // Current glow ring
    if (isCurrent && !isLocked) {
        drawCircle(
            color = base.copy(alpha = 0.22f),
            radius = r * 1.35f,
            center = center
        )
    }

    // Soft shadow (only for unlocked/current)
    if (!isLocked) {
        drawCircle(
            color = Color.Black.copy(alpha = 0.12f),
            radius = r,
            center = center + Offset(0f, r * 0.15f)
        )
    }

    // Outer rim
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(rim, rim.copy(alpha = 0.70f)),
            center = center
        ),
        radius = r,
        center = center
    )

    // Inner face
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(face, face.copy(alpha = 0.65f)),
            center = Offset(center.x - r * 0.10f, center.y - r * 0.10f)
        ),
        radius = r * 0.82f,
        center = center
    )

    // Shine (only if not locked)
    if (!isLocked) {
        drawCircle(
            color = Color.White.copy(alpha = 0.22f),
            radius = r * 0.45f,
            center = Offset(center.x - r * 0.28f, center.y - r * 0.28f)
        )
    }

    // Edge
    drawCircle(
        color = Color.Black.copy(alpha = 0.12f),
        radius = r,
        center = center,
        style = Stroke(width = 3f)
    )
}
