package com.example.feature.home.presentation.components

import android.util.Log
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    // For this example, we'll use a predefined list of drawable resources.
    // Later, you can load these from a ViewModel.

    val TAG = "ImageCarousel"

    val pagerState = rememberPagerState(pageCount = { imageList.size })

    Log.d(TAG, "ImageCarousel: count: ${imageList.size}")
    // Auto-scroll effect
    LaunchedEffect(pagerState.pageCount) {
        if (pagerState.pageCount > 0) {
            launch {
                while (true) {
                    delay(3000)
                    val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(225.dp)
            .padding(horizontal = 8.dp)
            .shimmer(cornerRadius = 16.dp, isLoadingImages),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
        // Adjust height as needed
    ) {
        if (imageList.isNotEmpty() && !isLoadingImages) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
            ) { page ->
                CarouselItem(imageUrl = imageList[page].imageUrl)
            }

            Spacer(modifier = Modifier.height(4.dp))

        }
        if (imageList.isNotEmpty()) {
            Row(
                Modifier
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color =
                        if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.5f
                        )
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)

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
            .padding(horizontal = 8.dp), // Space between pages
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // The shadow is on the Box now
    ) {

        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "Carousel Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .shimmer(16.dp)
                )
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .shimmer(16.dp)
                )
            }
        )

    }
}