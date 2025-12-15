package com.example.hassanalhawary.ui.screens.category_screen

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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.data.util.FakeDataSource
import com.example.domain.module.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categories: List<Category>,
    onCategoryClick: (categoryId: String, categoryName: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Creates a 2-column grid
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                CategoryCard(
                    category = category,
                    onClick = {
                        onCategoryClick(category.id, category.name)
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Makes the card a square
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // Use a Column to arrange the image and text vertically
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Box to contain the image/icon, allowing for size control
            Box(
                modifier = Modifier.weight(1f), // Takes up available space
                contentAlignment = Alignment.Center
            ) {
                // Use Coil's AsyncImage to load the URL
                /*AsyncImage(
                    model = category.imageUrl,
                    contentDescription = category.name, // Good for accessibility
                    modifier = Modifier.size(64.dp), // Control the size of the icon
                    contentScale = ContentScale.Fit
                )*/
            }

            // The title text below the image
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp) // Space between image and text
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryScreenPreview() {
    // This preview uses the fake data to show you how the screen will look.
    CategoryScreen(
        categories = FakeDataSource.audioCategories,
        onCategoryClick = { _, _ -> },
        onNavigateBack = {}
    )
}