package app.netlify.devalihassan.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.netlify.devalihassan.R
import com.example.core.ui.icons.TablerIcons

/** A bottom-nav tab: one Tabler outline icon for both states - selection is shown by colour. */
sealed class BottomNavItem(
    val route: String,
    @StringRes val titleResId: Int,
    @DrawableRes val iconResId: Int,
) {
    object Home : BottomNavItem(
        route = "home_screen",
        titleResId = R.string.home,
        iconResId = TablerIcons.Home,
    )

    object Search : BottomNavItem(
        route = "search_screen",
        titleResId = R.string.nav_search,
        iconResId = TablerIcons.Search,
    )

    object StudyScreen : BottomNavItem(
        route = Routes.STUDY_SCREEN,
        titleResId = R.string.study_zone,
        iconResId = TablerIcons.School,
    )

    object Profile : BottomNavItem(
        route = "profile_screen",
        titleResId = R.string.nav_account,
        iconResId = TablerIcons.User,
    )

    companion object {
        val all: List<BottomNavItem> get() = listOf(Home, Search, StudyScreen, Profile)
    }
}
