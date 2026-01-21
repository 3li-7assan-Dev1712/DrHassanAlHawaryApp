package com.example.search.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.ui.R
import com.example.domain.module.SearchResultMetaData
import com.example.search.presentation.model.SearchHit


@Composable
fun AudioResultCard(
    hit: SearchHit,
    modifier: Modifier = Modifier,
    onItemClick: (SearchResultMetaData) -> Unit
) {
    Card(
        modifier = modifier.clickable {
            onItemClick(
                SearchResultMetaData(
                    objectID = hit.objectID,
                    title = hit.title,
                    type = hit.type,
                    url = hit.url
                )
            )
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.audios_icon),
                contentDescription = "Audio",
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = hit.highlightedTitle ?: buildAnnotatedString {
                    append(hit.title ?: "No Title")
                },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
