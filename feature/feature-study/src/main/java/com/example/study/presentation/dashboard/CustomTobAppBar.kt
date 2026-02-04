package com.example.study.presentation.dashboard


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CustomTopAppBar(
    modifier: Modifier = Modifier,
    titleLine1: String = "مَعْهَدُ الشَّيْخِ حَسَنِ الهُوَّارِي الفِقْهيِ",
    titleLine2: String = "فقه وتأصيل",
) {
    // Teal glass strip (closer to your screenshot)
    val top = Color(0xFF3A6871)
    val bottom = Color(0xFF2E5E67)

    // Mint pill
    val mint1 = Color(0xFF2BC2A5)
    val mint2 = Color(0xFF1FAF96)

    val barHeight = 68.dp
    val pillShape = RoundedCornerShape(999.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(top, bottom)))
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val centerY = h * 0.56f
                val hair = Color.White.copy(alpha = 0.12f)

                drawLine(
                    color = hair,
                    start = Offset(w * 0.08f, centerY),
                    end = Offset(w * 0.32f, centerY),
                    strokeWidth = 1.6f
                )
                drawLine(
                    color = hair,
                    start = Offset(w * 0.68f, centerY),
                    end = Offset(w * 0.92f, centerY),
                    strokeWidth = 1.6f
                )

                // Tiny diamonds close to center (very subtle)
                drawTinyDiamond(Offset(w * 0.34f, centerY), h * 0.10f, hair)
                drawTinyDiamond(Offset(w * 0.66f, centerY), h * 0.10f, hair)

                drawCornerFlourish(
                    anchor = Offset(w * 0.04f, h * 0.22f),
                    dir = 1f,
                    color = Color.White.copy(alpha = 0.06f)
                )
                drawCornerFlourish(
                    anchor = Offset(w * 0.96f, h * 0.22f),
                    dir = -1f,
                    color = Color.White.copy(alpha = 0.06f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = titleLine1,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White.copy(alpha = 0.92f),
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(Brush.horizontalGradient(listOf(mint1, mint2)))
                        .padding(horizontal = 14.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = titleLine2,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawTinyDiamond(
    center: Offset,
    size: Float,
    color: Color
) {
    val half = size / 2f
    val p = Path().apply {
        moveTo(center.x, center.y - half)
        lineTo(center.x + half, center.y)
        lineTo(center.x, center.y + half)
        lineTo(center.x - half, center.y)
        close()
    }
    drawPath(p, color = color, style = Stroke(width = 1.4f))
}

private fun DrawScope.drawCornerFlourish(
    anchor: Offset,
    dir: Float,
    color: Color
) {
    val s = size.height * 0.22f
    val p = Path().apply {
        moveTo(anchor.x, anchor.y)
        cubicTo(
            anchor.x + (s * 0.9f * dir), anchor.y - (s * 0.25f),
            anchor.x + (s * 1.1f * dir), anchor.y + (s * 0.25f),
            anchor.x + (s * 1.6f * dir), anchor.y
        )
    }
    drawPath(p, color = color, style = Stroke(width = 2.0f))
}

@Preview
@Composable
private fun AppBarPrev() {
    CustomTopAppBar()
}