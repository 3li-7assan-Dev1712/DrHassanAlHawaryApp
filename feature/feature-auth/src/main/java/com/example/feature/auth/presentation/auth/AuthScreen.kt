package com.example.feature.auth.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.feature.auth.presentation.AuthScreenState
import com.example.feature.auth.presentation.AuthViewModel
import com.example.feature.auth.presentation.components.LoginWithGoogleComp
import com.example.feature.auth.presentation.components.WelcomeScreen

@Composable
fun AuthScreen(
    onSuccessfulAuth: () -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.isSignInSuccessful) {
        if (state.isSignInSuccessful) {
            onSuccessfulAuth()
            viewModel.resetState()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    AuthScreenContent(
        modifier = modifier,
        state = state,
        isAdmin = isAdmin,
        onGoogleClick = { viewModel.loginWithGoogle(context) }
    )
}

@Composable
fun AuthScreenContent(
    state: AuthScreenState,
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WelcomeScreen(isAdmin = isAdmin)

            Spacer(Modifier.height(40.dp))

            LoginWithGoogleComp(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                isLoading = state.showSignInProgressBar,
                onElementClick = onGoogleClick
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = false,
    device = Devices.PIXEL_7,
    name = "شاشة تسجيل الدخول"
)
@Composable
private fun AuthScreenArabicPreview() {
    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                AuthScreenContent(
                    state = AuthScreenState(),
                    onGoogleClick = {}
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = false,
    widthDp = 360,
    heightDp = 600,
    name = "شاشة الإدارة - جهاز صغير"
)
@Composable
private fun AuthScreenAdminSmallDevicePreview() {
    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                AuthScreenContent(
                    state = AuthScreenState(showSignInProgressBar = true),
                    isAdmin = true,
                    onGoogleClick = {}
                )
            }
        }
    }
}
