package app.netlify.devalihassan.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.core.ui.components.AppNavigationRail
import com.example.core.ui.components.RailDestination
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.Cairo

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BrandBottomBar(
        modifier = modifier,
        // The bar only shows on the four tab roots, so the tab is the current route's.
        isSelected = { item -> tabOf(currentRoute) == item },
        onSelect = { item -> navController.navigateToTab(item) },
    )
}

/**
 * The navigation rail of Medium and Expanded windows. Unlike the bottom bar it stays on
 * secondary screens too, so the selected tab is the flow you are in: the nearest tab root
 * in the back stack (Home for everything reached from Home, Search for a result opened
 * from Search, and so on).
 */
@Composable
fun AppRail(navController: NavHostController, modifier: Modifier = Modifier) {
    val backStack by navController.currentBackStack.collectAsState()
    val selectedTab = backStack.asReversed().firstNotNullOfOrNull { tabOf(it.destination.route) }
        ?: BottomNavItem.Home
    AppNavigationRail(
        destinations = BottomNavItem.all.map { item ->
            RailDestination(
                icon = item.iconResId,
                label = stringResource(item.titleResId),
                selected = item == selectedTab,
                onClick = { navController.navigateToTab(item) },
            )
        },
        modifier = modifier,
    )
}

/** The tab whose root [route] is (المعهد is registered with its deep-link argument). */
private fun tabOf(route: String?): BottomNavItem? = BottomNavItem.all.firstOrNull { item ->
    when (item.route) {
        Routes.STUDY_SCREEN -> route?.startsWith(Routes.STUDY_SCREEN) == true
        else -> route == item.route
    }
}

/** A tap on a bottom-bar tab or a rail destination. */
private fun NavHostController.navigateToTab(item: BottomNavItem) {
    val startId = graph.findStartDestination().id
    val stack = currentBackStack.value.filter { it.destination !is NavGraph }
    val tabIndex = stack.indexOfLast { tabOf(it.destination.route) != null }
    val tabEntry = stack.getOrNull(tabIndex)
    // The rail shows on secondary screens: choosing the tab you are already in, from deeper
    // in its flow, goes back to its root. (The bar only shows on roots: never the case there.)
    if (tabEntry != null && tabOf(tabEntry.destination.route) == item && tabIndex != stack.lastIndex) {
        popBackStack(tabEntry.destination.id, inclusive = false)
        return
    }
    // Keep the tab being left only when it is a tab of its own: one opened from the bar or
    // the rail (sitting directly on Home), with whatever was opened from it. A deeper stack,
    // such as Search opened from Q&A, is part of Home's flow: no tab restores it, so drop it
    // rather than hold its ViewModels. Home's own flow is never restored either (below).
    val keepTab = tabEntry != null && tabEntry.destination.id != startId &&
        stack.getOrNull(tabIndex - 1)?.destination?.id == startId
    navigate(item.route) {
        popUpTo(startId) {
            saveState = keepTab
        }
        launchSingleTop = true
        // Home is always at the bottom of the stack, so it means its own screen;
        // restoring would bring back whatever was stacked on it (Q&A -> Search), not Home.
        restoreState = item != BottomNavItem.Home
    }
}

/**
 * The bottom navigation as designed: brand background with a hairline on top, no
 * selection pill; the active tab is goldText (icon + label), the rest textMuted.
 * Pads itself for the system navigation bar (edge-to-edge).
 */
@Composable
fun BrandBottomBar(
    isSelected: (BottomNavItem) -> Boolean,
    onSelect: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Brand.colors.background)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        HorizontalDivider(thickness = 0.5.dp, color = Brand.colors.divider)
        NavigationBar(
            containerColor = Brand.colors.background,
            tonalElevation = 0.dp,
            // Insets are applied once, on the Column above.
            windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        ) {
            BottomNavItem.all.forEach { item ->
                val selected = isSelected(item)
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelect(item) },
                    icon = {
                        Icon(
                            painter = painterResource(item.iconResId),
                            contentDescription = null, // the label is read instead
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(item.titleResId),
                            fontFamily = Cairo,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Brand.colors.goldText,
                        selectedTextColor = Brand.colors.goldText,
                        unselectedIconColor = Brand.colors.textMuted,
                        unselectedTextColor = Brand.colors.textMuted,
                        indicatorColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}

@Preview(name = "Bottom nav - home", widthDp = 360)
@Composable
private fun BottomBarHomePreview() = BrandBottomBar(isSelected = { it == BottomNavItem.Home }, onSelect = {}, modifier = Modifier.fillMaxWidth())

@Preview(name = "Bottom nav - search", widthDp = 360)
@Composable
private fun BottomBarSearchPreview() = BrandBottomBar(isSelected = { it == BottomNavItem.Search }, onSelect = {}, modifier = Modifier.fillMaxWidth())

@Preview(name = "Bottom nav - institute", widthDp = 360)
@Composable
private fun BottomBarStudyPreview() = BrandBottomBar(isSelected = { it == BottomNavItem.StudyScreen }, onSelect = {}, modifier = Modifier.fillMaxWidth())

@Preview(name = "Bottom nav - account", widthDp = 360)
@Composable
private fun BottomBarAccountPreview() = BrandBottomBar(isSelected = { it == BottomNavItem.Profile }, onSelect = {}, modifier = Modifier.fillMaxWidth())
