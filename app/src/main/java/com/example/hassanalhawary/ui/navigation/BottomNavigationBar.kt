package com.example.hassanalhawary.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.hassanalhawary.ui.theme.CairoTypography


@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.StudyZone,
        BottomNavItem.Profile
    )

    // Observe the current back stack entry to determine the selected route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
//            val isHomeItem = screen.route == BottomNavItem.Home.route // Identify the Home item

            // Animation values
            val iconSize by animateDpAsState(
                targetValue = if (isSelected) 30.dp else 24.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "iconSize"
            )

            val textFontSize by animateFloatAsState(
                targetValue = if (isSelected) 13f else 12f,
                animationSpec = tween(durationMillis = 200),
                label = "textFontSize"
            )

            val itemPadding by animateDpAsState(
                targetValue = 4.dp,
                animationSpec = tween(durationMillis = 200),
                label = "itemPadding"
            )

            val homeIndicatorOffsetY by animateDpAsState(
                targetValue = 0.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "homeIndicatorOffsetY"
            )

            val homeIconColor =
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant



            NavigationBarItem(
                icon = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                    ) {
                        Icon(
                            painter = if (isSelected) screen.selectedIconPainter() else screen.unselectedIconPainter(),
                            contentDescription = screen.route,
                            modifier = Modifier.size(iconSize), // Animated icon size
                            tint = homeIconColor // Custom tint for home selected
                        )
                    }
                },
                label = {
                    Text(
                        stringResource(screen.titleResId),
                        fontSize = textFontSize.sp, // Animated text size
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.alpha( 1f),
                        style = CairoTypography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    // For non-Home items or Home when not using special indicator
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    // Standard indicator for non-Home items
                    indicatorColor =  Color.Transparent
                ),
                alwaysShowLabel = true
            )
        }
    }
}