package com.example.profile.presentation.rate_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.core.ui.R
import com.example.profile.domain.use_case.RateAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RateViewModel @Inject constructor(
    private val rageAppUseCase: RateAppUseCase
) : ViewModel() {
    fun rate(packageName: String) = rageAppUseCase(packageName)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateAppScreen(
    onBack: () -> Unit,
    viewModel: RateViewModel = hiltViewModel(),
    packageName: String
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.rate_app),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.rate_illu),
                contentDescription = stringResource(id = R.string.rating_illustration),
                modifier = Modifier.fillMaxWidth(0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.your_opinion_matters),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.rate_us_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.rate(packageName) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = stringResource(id = R.string.rating_star_icon)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(id = R.string.open_rating_page),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview
@Composable
private fun Prev() {
    RateAppScreen(onBack = {}, packageName = "")
}
