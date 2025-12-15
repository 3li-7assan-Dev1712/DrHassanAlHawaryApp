package com.example.hassanalhawary.ui.screens.image_groups_screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.module.ImageGroup


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCard(
    group: ImageGroup,
    onGroupClick: (String) -> Unit,
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
            AsyncImage(
                model = "https://firebasestorage.googleapis.com/v0/b/dr-hassan-al-hawary.appspot.com/o/images%2Fdr_hassan_photo.jpg?alt=media&token=793f5fef-9807-44a1-af29-e8b47819e3c8",
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
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupCardPreview() {
    /*val mockGroup1 = ImageGroup(
        id = "1",
        title = "أحكام التعامل مع العملات الرقمية",
        designs = listOf(
            Image(
                id = "d1",
                imageUrl = "https://www.facebook.",
                orderIndex = 1
            )
        )

    )

    HassanAlHawaryTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GroupCard(group = mockGroup1, onGroupClick = {})
        }
    }*/
}