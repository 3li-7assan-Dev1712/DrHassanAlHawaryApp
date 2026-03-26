package com.example.feature.image.presentation.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.example.core.ui.components.shimmer


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ImageScreen(
    onNavigateBack: () -> Unit,
    viewModel: ImageDetailViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    // 2. Destructure the state for easier access in the UI
    val isLoading = uiState.isLoading
    val imageGroup = uiState.imageGroup?.group
    val images = uiState.imageGroup?.images ?: emptyList()
    val error = uiState.error

    // 3. Initialize the pager state, now aware of the number of pages from the data
    val pagerState = rememberPagerState(pageCount = { images.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(imageGroup?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    ) { paddingValues ->
        // Use a Box to easily switch between Loading, Error, and Content states
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                // 4. Handle the Loading state
                isLoading -> {
                    CircularProgressIndicator()
                }

                // 5. Handle the Error state
                error != null -> {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }

                // 6. Handle the Content (Success) state
                imageGroup != null && images.isNotEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { pageIndex ->
                            // Use the actual image URL from the `images` list
                            val imageUrl = images[pageIndex].imageUrl
                            SubcomposeAsyncImage(
                                modifier = Modifier.fillMaxSize(),
                                model = imageUrl,
                                loading = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .shimmer()
                                    )
                                },
                                error = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .shimmer()
                                    )
                                },
                                contentDescription = "Image ${pageIndex + 1}",
                                contentScale = ContentScale.Fit,


                                )
                        }
                        // Pager position indicator text
                        if (pagerState.pageCount > 0) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${pagerState.pageCount}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    Text(text = "No images found in this group.")
                }
            }
        }
    }
}