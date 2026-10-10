package com.example.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.ui.R
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme

/** Figma EmptyDetail (`57:138`), measured in dp. */
object EmptyDetailDefaults {
    val Width = 360.dp
    val CircleSize = 96.dp
    val IconSize = 36.dp
    val Spacing = 16.dp

    const val TestTag = "emptyDetail"
    const val CircleTestTag = "emptyDetailCircle"
}

/**
 * The detail pane of a two-pane layout before anything is selected: centred in the pane,
 * a 96dp accentContainer circle with the icon, then the title and subtitle, 16 apart.
 */
@Composable
fun EmptyDetail(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val colors = Brand.colors
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .testTag(EmptyDetailDefaults.TestTag)
                .width(EmptyDetailDefaults.Width),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(EmptyDetailDefaults.Spacing),
        ) {
            Box(
                modifier = Modifier
                    .testTag(EmptyDetailDefaults.CircleTestTag)
                    .size(EmptyDetailDefaults.CircleSize)
                    .background(colors.accentContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = colors.onAccentContainer,
                    modifier = Modifier.size(EmptyDetailDefaults.IconSize),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(name = "Empty detail - audio, light", locale = "ar", widthDp = 712, heightDp = 736)
@Composable
private fun EmptyDetailLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) {
        Box(Modifier.background(Brand.colors.background)) {
            EmptyDetail(
                icon = TablerIcons.Headphones,
                title = stringResource(R.string.empty_detail_audio_title),
                subtitle = stringResource(R.string.empty_detail_audio_subtitle),
            )
        }
    }
}

@Preview(name = "Empty detail - search, dark", locale = "ar", widthDp = 712, heightDp = 736)
@Composable
private fun EmptyDetailDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) {
        Box(Modifier.background(Brand.colors.background)) {
            EmptyDetail(
                icon = TablerIcons.Search,
                title = stringResource(R.string.empty_detail_search_title),
                subtitle = stringResource(R.string.empty_detail_search_subtitle),
            )
        }
    }
}
