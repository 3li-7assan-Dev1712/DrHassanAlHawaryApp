package com.example.hassanalhawary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.hassanalhawary.R

sealed class BottomNavItem(
    val route: String,
    val titleResId:  Int,
    val selectedIconResId: Int,
    val unselectedIconResId: Int
) {
    // Helper composable function to get the painter resource
    @Composable
    fun selectedIconPainter(): Painter = painterResource(id = selectedIconResId)

    @Composable
    fun unselectedIconPainter(): Painter = painterResource(id = unselectedIconResId)

    object Home : BottomNavItem(
        route = "home_screen",
        titleResId = R.string.home,
        selectedIconResId = R.drawable.filled_home_icon,
        unselectedIconResId = R.drawable.home_icon
    )

    object Articles : BottomNavItem(
        route = "articles_screen",
        titleResId = R.string.articles,
        selectedIconResId = R.drawable.filled_article_icon,
        unselectedIconResId = R.drawable.article_icon
    )

    object Audios : BottomNavItem(
        route = "audios_screen",
        titleResId = R.string.lectures,
        selectedIconResId = R.drawable.filled_audio_icon,
        unselectedIconResId = R.drawable.audio_icon
    )

    object Questions : BottomNavItem(
        route = "questions_screen",
        titleResId = R.string.questions,
        selectedIconResId = R.drawable.question_icon,
        unselectedIconResId = R.drawable.question_icon
    )
}