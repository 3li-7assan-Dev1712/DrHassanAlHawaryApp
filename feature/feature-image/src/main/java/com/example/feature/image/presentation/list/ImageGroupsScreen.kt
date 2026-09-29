package com.example.feature.image.presentation.list

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import com.example.core.ui.theme.ContentPhase
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.animateStaggeredGridItem
import com.example.core.ui.theme.reducedMotion
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ImageNotSupported
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.components.EmptyState
import com.example.core.ui.components.Illustration
import com.example.core.ui.theme.Brand
import com.example.feature.image.presentation.components.DesignTile
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagesGroupsScreen(
    viewModel: ImagesGroupsViewModel = hiltViewModel(),
    onGroupClick: (groupId: String) -> Unit,
    onImageClick: (groupId: String, index: Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val lazyPagingItems = viewModel.imageGroups.collectAsLazyPagingItems()

    Scaffold(
        topBar = { AppTopBar(title = stringResource(R.string.images), onBack = onNavigateBack) },
        containerColor = Brand.colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val phase = when (lazyPagingItems.loadState.refresh) {
                is LoadState.Loading -> ContentPhase.Loading
                is LoadState.Error -> ContentPhase.Error
                is LoadState.NotLoading ->
                    if (lazyPagingItems.itemCount == 0) ContentPhase.Empty else ContentPhase.Content
            }
            val reduced = reducedMotion
            AnimatedContent(
                targetState = phase,
                transitionSpec = { Motion.contentSwap(reduced) },
                label = "designsContent",
            ) { shown ->
                when (shown) {
                    ContentPhase.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                strokeWidth = 3.dp,
                                color = Brand.colors.accentStrong
                            )
                        }
                    }
    
                    ContentPhase.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            EmptyState(
                                illustration = Illustration.ComputerAndServer,
                                title = stringResource(R.string.empty_no_connection),
                            )
                        }
                    }
    
                    ContentPhase.Empty, ContentPhase.Content -> {
                        if (shown == ContentPhase.Empty) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "لا توجد مجموعات تصاميم حاليًا.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Brand.colors.textMuted
                                )
                            }
                        } else {
                            // Designs come in many shapes: a staggered grid keeps each image's
                            // own proportions instead of cropping them all to one ratio.
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalItemSpacing = 16.dp,
                            ) {
                                items(
                                    count = lazyPagingItems.itemCount,
                                    key = lazyPagingItems.itemKey { it.id }
                                ) { index ->
                                    val group = lazyPagingItems[index]
                                    if (group != null) {
                                        // Only for the count badge; loaded lazily per visible tile.
                                        val images by viewModel.imagesForGroup(group.id)
                                            .collectAsStateWithLifecycle()
                                        Box(animateStaggeredGridItem()) {
                                            DesignTile(
                                                group = group,
                                                imageCount = images.size,
                                                onClick = { onGroupClick(group.id) },
                                            )
                                        }
                                    }
                                }
    
                                if (lazyPagingItems.loadState.append is LoadState.Loading) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Brand.colors.accentStrong)
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
}
