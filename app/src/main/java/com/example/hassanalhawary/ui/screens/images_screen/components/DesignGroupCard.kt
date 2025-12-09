package com.example.hassanalhawary.ui.screens.images_screen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hassanalhawary.R
import com.example.hassanalhawary.ui.screens.gallery_screen.Design
import com.example.hassanalhawary.ui.screens.gallery_screen.DesignGroup
import com.example.hassanalhawary.ui.theme.HassanAlHawaryTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignGroupCard(
    group: DesignGroup,
    onGroupClick: (String) -> Unit, // Pass the group ID
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onGroupClick(group.id) },
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Use the first image of the group as a preview
            Image(
                painter = painterResource(id = group.designs.firstOrNull()?.imageRes ?: R.drawable.search_illustration),
                contentDescription = group.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Progress Indicator Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Example progress, this would come from your ViewModel
                    val progress = 0.3f // e.g., 3 out of 10 viewed
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${(progress * group.designs.size).toInt()}/${group.designs.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DesignGroupCardPreview() {
    val mockGroup1 = DesignGroup(
        id = "1",
        title = "Patience During Hardship",
        designs = listOf(
            Design("d1", R.drawable.design_1),
            Design("d1", R.drawable.design_2),

        )
    )
    val mockGroup2 = DesignGroup(
        id = "2",
        title = "Patience During Hardship",
        designs = listOf(
            Design("d1", R.drawable.design_3),
            Design("d1", R.drawable.design_4),
        )
    )
    val mockGroup3 = DesignGroup(
        id = "3",
        title = "Patience During Hardship",
        designs = listOf(

            Design("d1", R.drawable.design_5),
            Design("d2", R.drawable.design_7)
        )
    )
    HassanAlHawaryTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DesignGroupCard(group = mockGroup1, onGroupClick = {})
        }
    }
}