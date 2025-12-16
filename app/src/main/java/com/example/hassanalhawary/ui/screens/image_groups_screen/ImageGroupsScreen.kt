package com.example.hassanalhawary.ui.screens.image_groups_screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.example.domain.module.ImageGroup
import com.example.hassanalhawary.ui.components.TopAppBar


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
                title = "",
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                    )
                },
                showNavigationIcon = true

                // Add navigation icon if needed
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

                        // Optional: Show a loading spinner at the bottom when appending new items
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
                    /*if (lazyPagingItems.itemCount == 0) {
                        Text(
                            text = "No design groups found.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        // 3. Display the data in a LazyVerticalGrid
                    }*/
                }
            }
        }
    }
}

@Composable
private fun ImageGroupCard(group: ImageGroup, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            AsyncImage(
                model = group.previewImageUrl,
                contentDescription = "Preview for ${group.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f),
                contentScale = ContentScale.Crop
            )
            Text(
                text = group.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}