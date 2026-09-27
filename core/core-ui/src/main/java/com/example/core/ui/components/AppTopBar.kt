package com.example.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.ui.R
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme

/**
 * The top bar for every screen except home: start-aligned title, optional back arrow
 * (Tabler arrow-right: in RTL "back" points right) and optional actions at the end.
 * Tab roots (search, institute, profile) pass no [onBack]. Flat, on the screen's
 * background: no divider strip or tinted container.
 */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    containerColor: Color = Brand.colors.background,
    contentColor: Color = Brand.colors.textPrimary,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(TablerIcons.ArrowRight),
                    contentDescription = stringResource(R.string.back),
                    tint = contentColor,
                    modifier = Modifier.size(24.dp),
                )
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
        )
        Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

/** A top-bar action: an outline icon tinted [Brand.colors.textPrimary]. */
@Composable
fun AppTopBarAction(icon: Int, contentDescription: String, onClick: () -> Unit, tint: Color = Brand.colors.textPrimary) {
    IconButton(onClick = onClick) {
        Icon(painterResource(icon), contentDescription = contentDescription, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Preview(name = "Top bar - light", locale = "ar", widthDp = 360)
@Composable
private fun AppTopBarLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) {
        AppTopBar(title = "الفيديوهات", onBack = {}) {
            AppTopBarAction(TablerIcons.Share, "مشاركة", {})
        }
    }
}

@Preview(name = "Top bar - dark", locale = "ar", widthDp = 360)
@Composable
private fun AppTopBarDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) {
        AppTopBar(title = "البحث")
    }
}
