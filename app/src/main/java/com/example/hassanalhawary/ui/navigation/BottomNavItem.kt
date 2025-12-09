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

    object Search : BottomNavItem(
        route = "search_screen",
        titleResId = R.string.search_hint,
        selectedIconResId = R.drawable.search_icon,
        unselectedIconResId = R.drawable.search_icon
    )

    object StudyZone : BottomNavItem(
        route = "study_zone_screen",
        titleResId = R.string.study_zone,
        selectedIconResId = R.drawable.study_zone_filled_icon,
        unselectedIconResId = R.drawable.study_zone_icon
    )

    object Profile : BottomNavItem(
        route = "profile_screen",
        titleResId = R.string.profile,
        selectedIconResId = R.drawable.student_icon,
        unselectedIconResId = R.drawable.student_icon
    )
}