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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.core.ui.theme.BrandTokens
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
        isSelected = { item ->
            when (item.route) {
                Routes.STUDY_SCREEN -> currentRoute?.startsWith(Routes.STUDY_SCREEN) == true
                else -> currentRoute == item.route
            }
        },
        onSelect = { item ->
            navController.navigate(item.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
    )
}

/**
 * The bottom navigation as designed: brand background with a hairline on top, no
 * selection pill; the active tab is goldSoft (icon + label), the rest textMuted.
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
            .background(BrandTokens.background)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        HorizontalDivider(thickness = 0.5.dp, color = BrandTokens.divider)
        NavigationBar(
            containerColor = BrandTokens.background,
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
                        selectedIconColor = BrandTokens.goldSoft,
                        selectedTextColor = BrandTokens.goldSoft,
                        unselectedIconColor = BrandTokens.textMuted,
                        unselectedTextColor = BrandTokens.textMuted,
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
