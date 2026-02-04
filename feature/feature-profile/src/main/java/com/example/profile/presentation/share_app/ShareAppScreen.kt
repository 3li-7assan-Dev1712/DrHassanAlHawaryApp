package com.example.profile.presentation.share_app


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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.profile.domain.use_case.ShareAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ShareViewModel @Inject constructor(
    private val shareAppUseCase: ShareAppUseCase
) : androidx.lifecycle.ViewModel() {

    fun share(packageName: String) {
        val link = "https://play.google.com/store/apps/details?id=$packageName"
        shareAppUseCase("جرّب هذا التطبيق الرائع:\n$link")
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
                title = { Text("مشاركة التطبيق", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Button(
            onClick = { viewModel.share(packageName) },
            modifier = Modifier
                .padding(padding)
                .padding(20f.dp)
                .fillMaxWidth()
        ) {
            Icon(Icons.Default.Share, null)
            androidx.compose.foundation.layout.Spacer(Modifier.width(10f.dp))
            Text("مشاركة رابط التطبيق")
        }
    }
}