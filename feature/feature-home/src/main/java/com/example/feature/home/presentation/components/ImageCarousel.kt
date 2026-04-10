package com.example.feature.home.presentation.components

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.core.ui.components.shimmer
import com.example.feature.home.domain.model.ImageFeed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel(
    modifier: Modifier = Modifier,
    imageList: List<ImageFeed> = listOf(),
    isLoadingImages: Boolean
) {
    val TAG = "ImageCarousel"

    val pagerState = rememberPagerState(pageCount = { imageList.size })

    Log.d(TAG, "ImageCarousel: count: ${imageList.size}")
    
    // Auto-scroll effect
    LaunchedEffect(pagerState.pageCount) {
        if (pagerState.pageCount > 0) {
            launch {
                while (true) {
                    delay(4000)
                    if (pagerState.pageCount > 0) {
                        val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                        pagerState.animateScrollToPage(nextPage)
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(vertical = 8.dp)
            .shimmer(cornerRadius = 16.dp, isLoadingImages),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (imageList.isNotEmpty() && !isLoadingImages) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                CarouselItem(imageUrl = imageList[page].imageUrl)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animated dot indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "indicator_width"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ImageCaruelPrev() {
    ImageCarousel(isLoadingImages = false)
}

@Composable
fun CarouselItem(imageUrl: String) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp), // Space between pages and edges
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image (Blurred and Cropped)
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(24.dp),
                loading = {
                    Box(modifier = Modifier.fillMaxSize().shimmer(16.dp))
                },
                error = {
                    Box(modifier = Modifier.fillMaxSize().shimmer(16.dp))
                }
            )
            
            // A semi-transparent overlay to make the foreground pop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
            
            // Foreground Image (Fit, without clipping)
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = "Carousel Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    // Handled by the background
                },
                error = {
                    // Handled by the background
                }
            )
        }
    }
}
