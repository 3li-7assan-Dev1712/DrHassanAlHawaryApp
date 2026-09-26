package com.example.core.ui.icons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.R
import com.example.core.ui.theme.BrandTokens

/**
 * The app's one icon set: Tabler Icons outline (MIT, https://tabler.io/icons),
 * converted to vector drawables with a white 2-unit stroke, so `tint` sets the
 * colour. Add new icons the same way (ic_tabler_<name>.xml) rather than mixing
 * in another style.
 */
object TablerIcons {
    @DrawableRes val Notebook = R.drawable.ic_tabler_notebook
    @DrawableRes val Headphones = R.drawable.ic_tabler_headphones
    @DrawableRes val Video = R.drawable.ic_tabler_video
    @DrawableRes val MessageQuestion = R.drawable.ic_tabler_message_question
    @DrawableRes val Photo = R.drawable.ic_tabler_photo
    @DrawableRes val UserCircle = R.drawable.ic_tabler_user_circle
    @DrawableRes val Home = R.drawable.ic_tabler_home
    @DrawableRes val Search = R.drawable.ic_tabler_search
    @DrawableRes val School = R.drawable.ic_tabler_school
    @DrawableRes val User = R.drawable.ic_tabler_user
    @DrawableRes val PlayerPlay = R.drawable.ic_tabler_player_play
    @DrawableRes val Bell = R.drawable.ic_tabler_bell

    val all: List<Pair<String, Int>> = listOf(
        "notebook" to Notebook, "headphones" to Headphones, "video" to Video,
        "message-question" to MessageQuestion, "photo" to Photo, "user-circle" to UserCircle,
        "home" to Home, "search" to Search, "school" to School, "user" to User,
        "player-play" to PlayerPlay, "bell" to Bell,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(name = "Tabler icons", widthDp = 360)
@Composable
private fun TablerIconsPreview() {
    FlowRow(
        modifier = Modifier
            .background(BrandTokens.background)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TablerIcons.all.forEach { (name, icon) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(painterResource(icon), contentDescription = name, tint = BrandTokens.gold, modifier = Modifier.size(24.dp))
                Text(name, color = BrandTokens.textMuted, fontSize = 9.sp)
            }
        }
    }
}
