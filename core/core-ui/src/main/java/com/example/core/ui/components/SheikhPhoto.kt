package com.example.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.ui.R
import com.example.core.ui.theme.Brand

/**
 * The sheikh's photo cropped to a circle with one thin ring, the same treatment as the
 * welcome screen. Uses the plain square `dr_hassan_photo`: `dr_hassan_image` is a
 * finished logo with a dark frame and a speech-bubble corner that shows through any
 * circular clip.
 */
@Composable
fun SheikhPhoto(
    modifier: Modifier = Modifier,
    size: Dp = 112.dp,
    ringWidth: Dp = 2.dp,
    ringColor: Color = Brand.colors.accent,
    contentDescription: String? = null,
) {
    Image(
        painter = painterResource(R.drawable.dr_hassan_photo),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(ringWidth, ringColor, CircleShape),
    )
}
