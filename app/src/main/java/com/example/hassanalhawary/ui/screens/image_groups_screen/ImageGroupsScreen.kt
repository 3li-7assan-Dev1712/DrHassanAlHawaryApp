package com.example.hassanalhawary.ui.screens.image_groups_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.domain.module.ImageGroup

@Composable
fun ImagesGroupsScreen(
    viewModel: ImagesGroupsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        when (val state = uiState) {
            is ImageGroupsUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is ImageGroupsUiState.Error -> {
                Text(
                    text = "Failed to load groups: ${state.message}",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is ImageGroupsUiState.Success -> {
                if (state.groups.isEmpty()) {
                    Text(
                        text = "No design groups found.\nUpload one from the 'New Design' tab.",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.groups, key = { it.id }) { group ->
                            ImageGroupCard(group = group)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImageGroupCard(group: ImageGroup) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            AsyncImage(
                model = group.previewImageUrl,
                contentDescription = "Preview for ${group.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f) // Standard image aspect ratio
                    .clip(
                        RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp
                        )
                    ),
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