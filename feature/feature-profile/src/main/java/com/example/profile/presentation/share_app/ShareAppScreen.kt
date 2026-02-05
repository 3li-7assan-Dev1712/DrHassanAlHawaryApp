package com.example.profile.presentation.share_app

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.ui.R
import com.example.profile.domain.use_case.ShareAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val shareAppUseCase: ShareAppUseCase,
    @ApplicationContext private val context: Context
) : androidx.lifecycle.ViewModel() {

    fun share(packageName: String) {
        val link = "https://play.google.com/store/apps/details?id=$packageName"
        val shareMessage = context.getString(R.string.share_app_text, link)
        shareAppUseCase(shareMessage)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareAppScreen(
    packageName: String,
    onBack: () -> Unit,
    viewModel: ShareViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.share_app_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Button(
            onClick = { viewModel.share(packageName) },
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Share,
                contentDescription = stringResource(id = R.string.share_icon)
            )
            androidx.compose.foundation.layout.Spacer(Modifier.width(10.dp))
            Text(stringResource(id = R.string.share_app_link_button))
        }
    }
}
