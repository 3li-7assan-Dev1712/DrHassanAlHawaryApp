package com.example.study.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.module.Level


val sampleLevels = listOf(
    Level(id = 1, title = "Level 1", isLocked = false),
    Level(id = 2, title = "Level 2", isLocked = false),
    Level(id = 3, title = "Level 3", isLocked = true),
    Level(id = 4, title = "Level 4", isLocked = true),
    Level(id = 5, title = "Level 5", isLocked = true)
)

/**
 * The main screen to display the list of levels.
 *
 * @param levels The list of levels to display.
 * @param onLevelClick The action to perform when an unlocked level is clicked. It passes the level ID.
 * @param modifier Modifier for this composable.
 */
@Composable
fun LevelsContent(
    levels: List<Level>,
    onLevelClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (levels.isEmpty()) {
        // Show a loading indicator or an empty state message
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(levels.size) { index ->
                    LevelItem(level = levels[index], onClick = { onLevelClick(levels[index].id) })
                }
            }
        }
    }
}

/**
 * A composable that displays a single level item in the list.
 *
 * @param level The level data to display.
 * @param onClick The action to perform when the item is clicked.
 */
@Composable
fun LevelItem(
    level: Level,
    onClick: () -> Unit
) {
    val backgroundColor = if (!level.isLocked) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.80f)
    }

    val contentColor = if (!level.isLocked) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = !level.isLocked, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = level.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = if (level.isLocked) "Unlocked" else "Locked",
                    fontSize = 14.sp,
                    color = contentColor
                )
            }
            if (level.isLocked) {

                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Enter Level",
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LevelsContentPreview() {
    // Sample data for the preview


    MaterialTheme {
        // Preview the LevelsScreen which includes navigation logic
        LevelsContent(
            levels = sampleLevels, modifier = Modifier,
            onLevelClick = {

            }
        )
    }
}