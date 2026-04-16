package com.example.profile.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.example.core.ui.animation.LoadingScreen
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
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(state.signOutResult) {
        val result = state.signOutResult ?: return@LaunchedEffect
        if (result.success) onLogout()
        viewModel.onSignOutResultConsumed()
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("حذف الحساب", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "هل أنت متأكد من رغبتك في حذف حسابك؟\n\n" +
                            "تنبيه: هذا الإجراء سيؤدي إلى حذف جميع بياناتك، سجل الدراسة، وتقدمك في الاختبارات بشكل نهائي ولا يمكن التراجع عنه.",
                    textAlign = TextAlign.Start
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف نهائي", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    val userName = state.userData?.username ?: "زائر التطبيق"
    val userEmail = state.userData?.email ?: ""
    val profileUrl = state.userData?.userProfilePictureUrl.orEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(com.example.core.ui.R.string.profile),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    windowInsets = WindowInsets(0.dp)
                )
            }
        ) { padding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // --- Header Section ---
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            tonalElevation = 2.dp
                        ) {
                            SubcomposeAsyncImage(
                                model = profileUrl,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentDescription = "profile image",
                                loading = {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    }
                                },
                                error = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.padding(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = userName,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        if (userEmail.isNotBlank()) {
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // --- Settings Section ---
                item {
                    ProfileSectionCard(title = "الإعدادات العامة") {
                        ThemeSwitcher(isDarkTheme = isDarkTheme, onThemeChange = onThemeChanged)
                    }
                }

                if (!isAdmin) {
                    // --- App Info Section ---
                    item {
                        ProfileSectionCard(title = "التطبيق") {
                            ProfileRow(
                                icon = Icons.Default.Info,
                                title = "عن التطبيق",
                                onClick = { onNavigate(ProfileRoute.About) }
                            )
                            ProfileRow(
                                icon = Icons.Default.Share,
                                title = "مشاركة التطبيق",
                                onClick = { onNavigate(ProfileRoute.Share) }
                            )
                            ProfileRow(
                                icon = Icons.Default.Star,
                                title = "تقييم التطبيق",
                                isLast = true,
                                onClick = { onNavigate(ProfileRoute.Rate) }
                            )
                        }
                    }

                    // --- Support & Legal Section ---
                    item {
                        ProfileSectionCard(title = "الدعم والسياسات") {
                            ProfileRow(
                                icon = Icons.Default.SupportAgent,
                                title = "الدعم والتواصل",
                                onClick = { onNavigate(ProfileRoute.Support) }
                            )
                            ProfileRow(
                                icon = Icons.Default.Policy,
                                title = "سياسة الخصوصية",
                                onClick = { onNavigate(ProfileRoute.Privacy) }
                            )
                            ProfileRow(
                                icon = Icons.Default.Gavel,
                                title = "الشروط والأحكام",
                                onClick = { onNavigate(ProfileRoute.Terms) }
                            )
                            ProfileRow(
                                icon = Icons.Default.Code,
                                title = "التراخيص والمصادر",
                                isLast = true,
                                onClick = { onNavigate(ProfileRoute.Licenses) }
                            )
                        }
                    }
                }

                // --- Danger Zone Section ---
                item {
                    ProfileSectionCard(title = "منطقة الخطر") {
                        ProfileRow(
                            icon = Icons.AutoMirrored.Default.ExitToApp,
                            title = "تسجيل الخروج",
                            iconColor = MaterialTheme.colorScheme.primary,
                            onClick = { viewModel.signOut() }
                        )
                        ProfileRow(
                            icon = Icons.Default.DeleteForever,
                            title = "حذف الحساب نهائياً",
                            iconColor = MaterialTheme.colorScheme.error,
                            isLast = true,
                            onClick = { showDeleteConfirmation = true }
                        )
                    }
                }
                
                item {
                    Text(
                        text = "الإصدار ${state.currentAppVersion}",
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        if (state.isDeleting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                LoadingScreen()
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    title: String,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight, // RTL arrow
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    )
    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ThemeSwitcher(isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onThemeChange(!isDarkTheme) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            Text(
                text = "الوضع الداكن",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        },
        leadingContent = {
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onThemeChange(it) }
            )
        }
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(
        onNavigate = {},
        onThemeChanged = {},
        isDarkTheme = false,
        onLogout = {}
    )
}
