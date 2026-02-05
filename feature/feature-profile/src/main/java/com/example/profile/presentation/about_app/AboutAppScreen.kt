package com.example.profile.presentation.about_app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(
    onBack: () -> Unit,
    viewModel: AboutViewModel = hiltViewModel()
) {
    val info by viewModel.appInfo.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.about_app), fontWeight = FontWeight.Bold) },
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
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ElevatedCard(shape = RoundedCornerShape(18.dp)) {
                ListItem(
                    headlineContent = { Text(info.appName, fontWeight = FontWeight.SemiBold) },
                    supportingContent = {
                        Text(stringResource(id = R.string.app_version, info.versionName))
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = stringResource(id = R.string.app_info_icon)
                        )
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(id = R.string.app_description),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
