package com.example.admin.ui.control_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.ui.R

private data class ControlPanelItem(
    val title: String,
    val icon: Int,
    val route: String
)

@Composable
fun ControlScreen(onNavigate: (String) -> Unit) {
    val controlItems = remember {
        listOf(
            ControlPanelItem("Article", R.drawable.articles_icon, "articles_upload"),
            ControlPanelItem("Audio", R.drawable.audios_icon, "audios_upload"),
            ControlPanelItem("Images", R.drawable.images_icon, "images_upload"),
            ControlPanelItem("Video", R.drawable.videos_icon, "videos_upload"),
            ControlPanelItem("Institute", R.drawable.study_zone_icon, "telegram_login"),
            ControlPanelItem("Institute", R.drawable.student_icon, "profile_screen"),
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(controlItems) { controlItem ->
            GridItem(
                item = controlItem,
                onClick = { onNavigate(controlItem.route) }
            )
        }
    }
}

@Composable
private fun GridItem(item: ControlPanelItem, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = item.icon),
                contentDescription = item.title,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
