package com.example.feature.auth.presentation.auth

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.theme.BrandTokens
import com.example.core.ui.theme.Cairo
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.core.ui.util.LightSystemBarIcons
import com.example.feature.auth.R
import com.example.feature.auth.presentation.AuthUiState
import com.example.feature.auth.presentation.AuthViewModel
import com.example.feature.auth.presentation.components.LoginWithGoogleComp
import com.example.feature.auth.presentation.components.PrivacyPolicyLine
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

    LaunchedEffect(state) {
        if (state == AuthUiState.Success) {
            onSuccessfulAuth()
            viewModel.resetState()
        }
    }

    AuthScreenContent(
        modifier = modifier,
        state = state,
        isAdmin = isAdmin,
        onGoogleClick = { viewModel.loginWithGoogle(context) },
        onAddAccount = {
            viewModel.dismissMessage()
            context.openAddGoogleAccount()
        },
        onDismissMessage = viewModel::dismissMessage,
    )
}

@Composable
fun AuthScreenContent(
    state: AuthUiState,
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    onAddAccount: () -> Unit = {},
    onDismissMessage: () -> Unit = {},
) {
    // Always the dark brand palette, like home: the background also fills the
    // status/navigation bar areas (edge-to-edge), so their icons must be light.
    LightSystemBarIcons()
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(BrandTokens.background)
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        // At least the viewport tall, so the header centres in the space above the
        // bottom-anchored button; scrolls when it can't fit (landscape, large fonts).
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                WelcomeScreen(isAdmin = isAdmin)
            }

            LoginWithGoogleComp(
                modifier = Modifier.fillMaxWidth(),
                isLoading = state == AuthUiState.Loading,
                onElementClick = onGoogleClick
            )

            Spacer(Modifier.height(8.dp))

            PrivacyPolicyLine()

            Spacer(Modifier.height(8.dp))
        }

        when (state) {
            AuthUiState.Error -> AuthMessage(
                message = stringResource(R.string.auth_error_sign_in),
                actionLabel = stringResource(R.string.auth_retry),
                onAction = onGoogleClick,
                onDismiss = onDismissMessage,
            )

            AuthUiState.NoAccount -> AuthMessage(
                message = stringResource(R.string.auth_no_google_account),
                actionLabel = stringResource(R.string.auth_add_account),
                onAction = onAddAccount,
                onDismiss = onDismissMessage,
            )

            else -> Unit
        }
    }
}

/**
 * A snackbar driven straight by [AuthUiState] (no SnackbarHostState), so the screen stays
 * stateless and previewable. It stays until the user acts on it or closes it.
 */
@Composable
private fun BoxScope.AuthMessage(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    Snackbar(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .widthIn(max = 480.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        containerColor = BrandTokens.surface,
        contentColor = BrandTokens.textPrimary,
        action = {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = BrandTokens.gold, fontFamily = Cairo)
            }
        },
        dismissAction = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.auth_dismiss), color = BrandTokens.textSecondary, fontFamily = Cairo)
            }
        },
    ) {
        Text(message, fontFamily = Cairo)
    }
}

/** System "add account" screen, filtered to Google; plain Settings if a ROM lacks it. */
private fun Context.openAddGoogleAccount() {
    val addAccount = Intent(Settings.ACTION_ADD_ACCOUNT)
        .putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
    try {
        startActivity(addAccount)
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Settings.ACTION_SETTINGS))
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
            AuthScreenContent(state = AuthUiState.Idle, onGoogleClick = {})
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
            AuthScreenContent(state = AuthUiState.Loading, isAdmin = true, onGoogleClick = {})
        }
    }
}
