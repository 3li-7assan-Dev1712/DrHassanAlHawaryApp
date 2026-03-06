package com.example.feature.image.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.core.ui.R
import com.example.feature.image.presentation.components.ImageGroupCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagesGroupsScreen(
    viewModel: ImagesGroupsViewModel = hiltViewModel(),
    onGroupClick: (groupId: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    // 1. Collect the Flow as LazyPagingItems
    val lazyPagingItems = viewModel.imageGroups.collectAsLazyPagingItems()

    Scaffold(
        topBar = {

            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Text(
                        text = stringResource(R.string.images),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                    )

                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 2. Handle the different loading states
            when (val refreshState = lazyPagingItems.loadState.refresh) {
                is LoadState.Loading -> {
                    // Full screen loading indicator for the initial load
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is LoadState.Error -> {
                    // Show an error message if the initial load fails
                    Text(
                        text = "Failed to load groups.\nPull to refresh.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is LoadState.NotLoading -> {
                    if (lazyPagingItems.itemCount == 0) {
                        Text(
                            text = "No design groups found.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                count = lazyPagingItems.itemCount,
                                key = lazyPagingItems.itemKey { it.id } // Use the item's ID as a stable key
                            ) { index ->
                                val group = lazyPagingItems[index]
                                if (group != null) {
                                    ImageGroupCard(
                                        group = group,
                                        onClick = { onGroupClick(group.id) }
                                    )
                                }
                            }

                            if (lazyPagingItems.loadState.append is LoadState.Loading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
