package com.example.hassanalhawary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.hassanalhawary.R

sealed class BottomNavItem(
    val route: String,
    val title: String,
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
        title = "Home",
        selectedIconResId = R.drawable.home_icon,
        unselectedIconResId = R.drawable.home_icon
    )

    object Articles : BottomNavItem(
        route = "articles_screen",
        title = "Articles",
        selectedIconResId = R.drawable.article_icon,
        unselectedIconResId = R.drawable.article_icon
    )

    object Audios : BottomNavItem(
        route = "audios_screen",
        title = "Audios",
        selectedIconResId = R.drawable.audio_icon,
        unselectedIconResId = R.drawable.audio_icon
    )

    object Questions : BottomNavItem(
        route = "questions_screen",
        title = "Questions",
        selectedIconResId = R.drawable.question_icon,
        unselectedIconResId = R.drawable.question_icon
    )
}