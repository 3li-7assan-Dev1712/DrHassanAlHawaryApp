package com.example.profile.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.example.profile.presentation.components.ProfileRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isAdmin: Boolean = false,
    onNavigate: (ProfileRoute) -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    viewModel: ProfileScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.signOutResult) {
        val result = state.signOutResult ?: return@LaunchedEffect
        if (result.success) onLogout()
        viewModel.onSignOutResultConsumed()
    }

    val userName = state.userData?.username ?: "زائر التطبيق"
    val userEmail = state.userData?.email ?: "hassan.app@example.com"
    val profileUrl = state.userData?.userProfilePictureUrl.orEmpty()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("الحساب الشخصي", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = 16.dp
            )
        ) {

            item {
                // Header card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            SubcomposeAsyncImage(
                                model = profileUrl,
                                modifier = Modifier.fillMaxSize(),
                                contentDescription = "user profile image",
                                loading = {
                                    Box(
                                        Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(26.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                },
                                error = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.padding(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(8.dp))
                            AssistChip(
                                onClick = { /* no-op */ },
                                label = { Text("الحساب متصل") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
            }

            if (!isAdmin) { // only show full polihsed screen for users not neccessary for admins
                item {
                    SectionCard(title = "السمة") {
                        ThemeSwitcher(isDarkTheme = isDarkTheme, onThemeChange = {
                            onThemeChanged(it)
                        })
                    }
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    SectionCard(title = "التطبيق") {
                        ProfileRow(
                            icon = Icons.Default.Info,
                            title = "عن التطبيق",
                            subtitle = "الإصدار، المطور، المصادر",
                            onClick = { onNavigate(ProfileRoute.About) }
                        )
                        ProfileRow(
                            icon = Icons.Default.Share,
                            title = "مشاركة التطبيق",
                            subtitle = "أرسل رابط التطبيق لمن تحب",
                            onClick = { onNavigate(ProfileRoute.Share) }
                        )
                        ProfileRow(
                            icon = Icons.Default.Star,
                            title = "تقييم التطبيق",
                            subtitle = "ساعدنا بتقييمك على المتجر",
                            onClick = { onNavigate(ProfileRoute.Rate) }
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }


                item {
                    SectionCard(title = "الدعم والسياسات") {
                        ProfileRow(
                            icon = Icons.Default.SupportAgent,
                            title = "الدعم والتواصل",
                            subtitle = "أسئلة، اقتراحات، مشاكل",
                            onClick = { onNavigate(ProfileRoute.Support) }
                        )
                        ProfileRow(
                            icon = Icons.Default.Policy,
                            title = "سياسة الخصوصية",
                            subtitle = "كيف نتعامل مع بياناتك",
                            onClick = { onNavigate(ProfileRoute.Privacy) }
                        )
                        ProfileRow(
                            icon = Icons.Default.Gavel,
                            title = "الشروط والأحكام",
                            subtitle = "بنود الاستخدام",
                            onClick = { onNavigate(ProfileRoute.Terms) }
                        )
                        ProfileRow(
                            icon = Icons.Default.Code,
                            title = "التراخيص والمصادر المفتوحة",
                            subtitle = "Open source licenses",
                            onClick = { onNavigate(ProfileRoute.Licenses) }
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                }
            }

            item {
                Button(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Default.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("تسجيل الخروج", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

}


@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            content()
        }
    }
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
    Divider(
        Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    )
}

@Composable
fun ThemeSwitcher(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text("الوضع الداكن", fontWeight = FontWeight.SemiBold) },
        leadingContent = {
            Icon(
                if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null
            )
        },
        trailingContent = {
            Switch(checked = isDarkTheme, onCheckedChange = {
                onThemeChange(!isDarkTheme)
            })
        },
        modifier = Modifier.clickable { onThemeChange(!isDarkTheme) }
    )
}

@Preview
@Composable
private fun Prev() {
    ProfileScreen(
        onNavigate = {},
        onThemeChanged = {},
        isDarkTheme = isSystemInDarkTheme(),
        onLogout = {}
    )
}
