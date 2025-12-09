package com.example.hassanalhawary.ui.screens.home_screen.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hassanalhawary.R


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel(
    modifier: Modifier = Modifier
) {
    // For this example, we'll use a predefined list of drawable resources.
    // Later, you can load these from a ViewModel.
    val imageList = listOf(
        R.drawable.image_1,
        R.drawable.image_3,
        R.drawable.image_1
    )

    val pagerState = rememberPagerState(pageCount = { imageList.size })

    // Auto-scroll effect
  /*  LaunchedEffect(pagerState) {
        launch {
            while (true) {
                delay(3000) // Wait for 3 seconds
                val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }*/

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp) // Adjust height as needed
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            CarouselItem(imageRes = imageList[page])
        }

        // Page indicators (dots)
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
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

@Composable
fun CarouselItem(imageRes: Int) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp), // Space between pages
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // The shadow is on the Box now
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Carousel Image",
            contentScale = ContentScale.Crop, // Crop to fill the card bounds
            modifier = Modifier.fillMaxSize()
        )
    }
}