package com.example.profile.presentation.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToAboutApp: () -> Unit,
    viewModel: ProfileScreenViewModel = hiltViewModel()
) {


    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.signOutResult) {
        if (state.signOutResult != null) {
            // If the sign-out was successful, trigger the navigation
            if (state.signOutResult!!.success) {
                onLogout()
            }
            // After handling, reset the event in the ViewModel
            viewModel.onSignOutResultConsumed()
        }
    }

    val userName = state.userData?.username ?: "زائر التطبيق"
    val userEmail =
        state.userData?.let { "hassan.app@example.com" } ?: "hassan.app@example.com"
    val profileUrl = state.userData?.userProfilePictureUrl ?: ""

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("الحساب الشخصي", fontWeight = FontWeight.Bold)
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Profile Avatar Header
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(120.dp)
            ) {
                // Use SubcomposeAsyncImage to handle loading and error states explicitly
                SubcomposeAsyncImage(
                    model = profileUrl,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "user profile image",
                    loading = {
                        // Show a loading indicator while the image is being fetched
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Fallback profile icon",
                            modifier = Modifier.padding(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

            }

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(text = userEmail, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            Spacer(modifier = Modifier.height(40.dp))

            // 2. Settings List
            ProfileMenuItem(
                icon = Icons.Default.Info,
                label = "عن التطبيق",
                onClick = onNavigateToAboutApp
            )

            ProfileMenuItem(
                icon = Icons.Default.Share,
                label = "مشاركة التطبيق مع الآخرين",
                onClick = { /* Implement system share intent */ }
            )

            ProfileMenuItem(
                icon = Icons.Default.Star,
                label = "تقييم التطبيق",
                onClick = { /* Open Play Store link */ }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 3. Logout Section
            Button(
                onClick = { viewModel.signOut() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("تسجيل الخروج", fontWeight = FontWeight.Bold)
            }

            Text(
                text = "الإصدار 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 16.dp),
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft, // RTL arrow
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}