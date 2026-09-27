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
    // Not auto-mirrored: RTL screens use arrow-right for back and chevron-left for forward.
    @DrawableRes val ArrowRight = R.drawable.ic_tabler_arrow_right
    @DrawableRes val ChevronLeft = R.drawable.ic_tabler_chevron_left
    @DrawableRes val X = R.drawable.ic_tabler_x
    @DrawableRes val Check = R.drawable.ic_tabler_check
    @DrawableRes val Clock = R.drawable.ic_tabler_clock
    @DrawableRes val Share = R.drawable.ic_tabler_share
    @DrawableRes val TextSize = R.drawable.ic_tabler_text_size
    @DrawableRes val PlayerPause = R.drawable.ic_tabler_player_pause
    @DrawableRes val Download = R.drawable.ic_tabler_download
    @DrawableRes val CircleCheck = R.drawable.ic_tabler_circle_check
    @DrawableRes val Send = R.drawable.ic_tabler_send
    @DrawableRes val Logout = R.drawable.ic_tabler_logout
    @DrawableRes val Trash = R.drawable.ic_tabler_trash
    @DrawableRes val Sun = R.drawable.ic_tabler_sun
    @DrawableRes val Moon = R.drawable.ic_tabler_moon
    @DrawableRes val DeviceMobile = R.drawable.ic_tabler_device_mobile
    @DrawableRes val World = R.drawable.ic_tabler_world
    @DrawableRes val ZoomIn = R.drawable.ic_tabler_zoom_in
    @DrawableRes val InfoCircle = R.drawable.ic_tabler_info_circle
    @DrawableRes val Star = R.drawable.ic_tabler_star
    @DrawableRes val Headset = R.drawable.ic_tabler_headset
    @DrawableRes val ShieldLock = R.drawable.ic_tabler_shield_lock
    @DrawableRes val FileText = R.drawable.ic_tabler_file_text
    @DrawableRes val Code = R.drawable.ic_tabler_code
    @DrawableRes val BrandTelegram = R.drawable.ic_tabler_brand_telegram
    @DrawableRes val ExternalLink = R.drawable.ic_tabler_external_link
    /** Counter-clockwise: "back 10 s" in the player. */
    @DrawableRes val Rotate = R.drawable.ic_tabler_rotate
    /** Clockwise: "forward 10 s" in the player. */
    @DrawableRes val RotateClockwise = R.drawable.ic_tabler_rotate_clockwise
    @DrawableRes val Messages = R.drawable.ic_tabler_messages
    @DrawableRes val Books = R.drawable.ic_tabler_books
    @DrawableRes val BuildingMosque = R.drawable.ic_tabler_building_mosque
    @DrawableRes val Microphone = R.drawable.ic_tabler_microphone
    @DrawableRes val Book = R.drawable.ic_tabler_book
    @DrawableRes val LayoutGrid = R.drawable.ic_tabler_layout_grid
    @DrawableRes val Copy = R.drawable.ic_tabler_copy
    @DrawableRes val QrCode = R.drawable.ic_tabler_qrcode
    @DrawableRes val Music = R.drawable.ic_tabler_music

    val all: List<Pair<String, Int>> = listOf(
        "notebook" to Notebook, "headphones" to Headphones, "video" to Video,
        "message-question" to MessageQuestion, "photo" to Photo, "user-circle" to UserCircle,
        "home" to Home, "search" to Search, "school" to School, "user" to User,
        "player-play" to PlayerPlay, "bell" to Bell,
        "arrow-right" to ArrowRight, "chevron-left" to ChevronLeft, "x" to X, "check" to Check,
        "clock" to Clock, "share" to Share, "text-size" to TextSize, "player-pause" to PlayerPause,
        "download" to Download, "circle-check" to CircleCheck, "send" to Send, "logout" to Logout,
        "trash" to Trash, "sun" to Sun, "moon" to Moon, "device-mobile" to DeviceMobile,
        "world" to World, "zoom-in" to ZoomIn, "info-circle" to InfoCircle, "star" to Star,
        "headset" to Headset, "shield-lock" to ShieldLock, "file-text" to FileText, "code" to Code,
        "brand-telegram" to BrandTelegram, "external-link" to ExternalLink,
        "rotate" to Rotate, "rotate-clockwise" to RotateClockwise, "messages" to Messages, "books" to Books,
        "building-mosque" to BuildingMosque, "microphone" to Microphone, "book" to Book,
        "layout-grid" to LayoutGrid, "copy" to Copy, "qrcode" to QrCode, "music" to Music,
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
