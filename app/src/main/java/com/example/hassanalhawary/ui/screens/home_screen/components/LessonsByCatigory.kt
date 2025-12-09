package com.example.hassanalhawary.ui.screens.home_screen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hassanalhawary.R
import com.example.hassanalhawary.ui.navigation.Routes
import com.example.hassanalhawary.ui.theme.HassanAlHawaryTheme


data class Category(
    val route: String,
    val name: String,
    val iconRes: Int
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(
    category: Category,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    Card(
        onClick = { onClick(category.route) },
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Use surface color for a clean white/black background
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(category.iconRes),
                contentDescription = category.name,
                modifier = Modifier.size(40.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
fun LessonsByCategory(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- Section Title ---
        Text(
            text = stringResource(R.string.audios),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.take(3).forEach { category ->
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        category = category,
                        onClick = { onCategoryClick(category.route) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.drop(3).take(3).forEach { category ->
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        category = category,
                        onClick = { onCategoryClick(category.route) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LessonsByCategoryShelfPreview() {
    // Create mock data for the preview
    val mockCategories = listOf(
        Category(Routes.ARTICLES_SCREEN, stringResource(R.string.articles), R.drawable.articles_icon),
        Category(Routes.AUDIO_LIST_SCREEN, stringResource(R.string.audios), R.drawable.audios_icon),
        Category(Routes.VIDEOS_SCREEN, stringResource(R.string.videos), R.drawable.videos_icon),
        Category(Routes.KHOTAB_SCREEN, stringResource(R.string.khotab_aljumah), R.drawable.jummah_icon),
        Category(Routes.IMAGES_SCREEN, stringResource(R.string.war), R.drawable.war_icon),
        Category(Routes.IMPORTANT_QUESTIONS_SCREEN, stringResource(R.string.most_important), R.drawable.most_important_icon)
    )

    HassanAlHawaryTheme {
        LessonsByCategory(
            categories = mockCategories,
            onCategoryClick = { categoryId ->
                // In a real app, this would trigger navigation
                println("Category clicked: $categoryId")
            }
        )
    }
}