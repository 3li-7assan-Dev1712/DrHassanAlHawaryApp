package com.example.hassanalhawary.ui.screens.gallery_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.example.domain.module.ImageGroup


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    onNavigateBack: () -> Unit,
    // This would be fetched from a ViewModel based on groupId
    group: ImageGroup?
) {
    /*val pagerState = rememberPagerState(pageCount = { group?.designs?.size ?: 0 })

    LaunchedEffect(pagerState.currentPage) {
        // Here you would call viewModel.markAsViewed(group.designs[pagerState.currentPage].id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (group == null) {
            // Show a loading indicator or empty state
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { pageIndex ->
                    AsyncImage(
                        model = "https://firebasestorage.googleapis.com/v0/b/dr-hassan-al-hawary.appspot.com/o/images%2Fdr_hassan_photo.jpg?alt=media&token=793f5fef-9807-44a1-af29-e8b47819e3c8",
                        contentDescription = "Design ${pageIndex + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(R.drawable.ic_launcher_background),
                        error = painterResource(R.drawable.cv_icon)
                    )
                }
                // Pager position indicator text
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
    }*/
}