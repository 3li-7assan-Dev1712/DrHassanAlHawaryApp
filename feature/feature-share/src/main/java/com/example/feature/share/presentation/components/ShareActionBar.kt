package com.example.feature.share.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.core.ui.R

/**
 * Tapping this is what starts generation - progress itself is shown by the
 * full-screen [GenerationOverlay], not by this button, so it stays a plain
 * button rather than a fill indicator.
 */
@Composable
fun ShareActionBar(
    enabled: Boolean,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onShareClick,
        enabled = enabled,
        shape = RoundedCornerShape(28.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .height(56.dp),
    ) {
        Text(stringResource(R.string.share_button_label))
    }
}
