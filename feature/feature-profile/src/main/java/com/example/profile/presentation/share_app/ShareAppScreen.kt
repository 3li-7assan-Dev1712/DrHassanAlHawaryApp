package com.example.profile.presentation.share_app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.components.Illustration
import com.example.core.ui.components.IllustrationBox
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.domain.text.BidiText
import com.example.profile.domain.use_case.ShareAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The id the bundled QR (res/drawable-nodpi/qr_play_store.png) encodes. */
private const val QR_APPLICATION_ID = "app.netlify.devalihassan"

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val shareAppUseCase: ShareAppUseCase,
    @ApplicationContext private val context: Context
) : androidx.lifecycle.ViewModel() {

    fun storeLink(packageName: String) = "https://play.google.com/store/apps/details?id=$packageName"

    /** The system share sheet (ACTION_SEND, text/plain) with the message and the link. */
    fun share(packageName: String) {
        shareAppUseCase("${context.getString(R.string.share_app_message)}\n${storeLink(packageName)}")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareAppScreen(
    packageName: String,
    onBack: () -> Unit,
    viewModel: ShareViewModel = hiltViewModel()
) {
    val colors = Brand.colors
    val context = LocalContext.current
    val link = viewModel.storeLink(packageName)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedText = stringResource(R.string.share_app_link_copied)
    var showQr by remember { mutableStateOf(false) }
    // The bundled QR encodes this exact id; any other build would show a wrong code.
    val qrAvailable = packageName == QR_APPLICATION_ID

    Scaffold(
        containerColor = colors.background,
        topBar = { AppTopBar(title = stringResource(R.string.share_app_title), onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IllustrationBox(Illustration.PersonAndGlobe, modifier = Modifier.widthIn(max = 170.dp), height = 150.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.share_app_heading),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = colors.surface,
                border = BorderStroke(0.5.dp, colors.divider),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.share_app_message_label), style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.share_app_message),
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 1.7.em),
                        color = colors.textPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(BidiText.ltr(link), style = MaterialTheme.typography.labelSmall, color = colors.accentText)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { viewModel.share(packageName) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentStrong, contentColor = colors.onGold),
            ) {
                Icon(painterResource(TablerIcons.Share), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.share_app_share_link), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryButton(TablerIcons.Copy, stringResource(R.string.share_app_copy_link), Modifier.weight(1f)) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("link", link))
                    scope.launch { snackbarHostState.showSnackbar(copiedText) }
                }
                if (qrAvailable) {
                    SecondaryButton(TablerIcons.QrCode, stringResource(R.string.share_app_qr), Modifier.weight(1f)) { showQr = true }
                }
            }
        }
    }

    if (showQr) {
        ModalBottomSheet(onDismissRequest = { showQr = false }, containerColor = colors.surface) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.share_app_qr_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(16.dp))
                // White behind the code in both themes, so any scanner reads it.
                Box(
                    Modifier
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                ) {
                    Image(painterResource(R.drawable.qr_play_store), contentDescription = link, modifier = Modifier.size(200.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.share_app_qr_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SecondaryButton(icon: Int, label: String, modifier: Modifier, onClick: () -> Unit) {
    val colors = Brand.colors
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, colors.divider),
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = colors.textPrimary)
    }
}
