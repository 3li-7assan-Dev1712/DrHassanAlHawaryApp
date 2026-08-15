package com.example.feature.video.presentation.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.core.ui.components.CategoryGridTile
import com.example.domain.module.ContentCategory

@Composable
fun VideoCategoryScreen(
    onCategoryClick: (id: String, title: String) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: VideoCategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    VideoCategoryContent(
        uiState = uiState,
        onCategoryClick = onCategoryClick,
        onNavigateUp = onNavigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoCategoryContent(
    uiState: VideoCategoryUiState,
    onCategoryClick: (id: String, title: String) -> Unit,
    onNavigateUp: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        "تصنيفات الفيديوهات",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.categories) { category ->
                        VideoCategoryItem(
                            category = category,
                            onClick = { onCategoryClick(category.id, category.title) }
                        )
                    }
                }
            }
        }
    }
}

private sealed class CategoryIcon {
    data class Drawable(val resId: Int) : CategoryIcon()
    data class Vector(val imageVector: ImageVector) : CategoryIcon()
}

private fun resolveCategoryIcon(categoryId: String): CategoryIcon = when (categoryId) {
    "fatawah" -> CategoryIcon.Drawable(R.drawable.fatawah_icon)
    "scientific_lessons" -> CategoryIcon.Drawable(R.drawable.scientific_lessons_icon)
    "khotab" -> CategoryIcon.Drawable(R.drawable.khotab_jumah_icon)
    "lectures" -> CategoryIcon.Drawable(R.drawable.lectures_icon)
    "telawat" -> CategoryIcon.Vector(Icons.AutoMirrored.Filled.MenuBook)
    ALL_VIDEO_CATEGORIES_ID -> CategoryIcon.Vector(Icons.Filled.Apps)
    else -> CategoryIcon.Drawable(R.drawable.dr_hassan_image)
}

@Composable
private fun VideoCategoryItem(
    category: ContentCategory,
    onClick: () -> Unit
) {
    CategoryGridTile(
        title = category.title,
        onClick = onClick
    ) {
        when (val icon = resolveCategoryIcon(category.id)) {
            is CategoryIcon.Drawable -> Icon(
                painter = painterResource(id = icon.resId),
                contentDescription = category.title,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )

            is CategoryIcon.Vector -> Icon(
                imageVector = icon.imageVector,
                contentDescription = category.title,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
