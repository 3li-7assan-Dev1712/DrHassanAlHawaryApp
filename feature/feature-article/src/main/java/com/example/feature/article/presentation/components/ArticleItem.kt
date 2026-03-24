package com.example.feature.article.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.ui.R
import com.example.domain.module.Article
import java.util.Date

@Composable
fun ArticleItem(
    article: Article,
    onReadMoreClicked: () -> Unit,
    formatDate: (Date) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val relativeTime = getRelativeTimeText(article.publishDate, formatDate(article.publishDate))
                Text(
                    text = stringResource(R.string.published_since, relativeTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Button(
                    onClick = onReadMoreClicked,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.read_more),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun getRelativeTimeText(date: Date, defaultFormattedDate: String): String {
    val diff = System.currentTimeMillis() - date.time
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7

    return when {
        minutes < 1 -> stringResource(R.string.just_now)
        minutes < 60 -> stringResource(R.string.since_minutes, minutes.toInt())
        hours < 24 -> stringResource(R.string.since_hours, hours.toInt())
        days == 2L -> stringResource(R.string.since_2_days)
        days < 7 -> stringResource(R.string.since_days, days.toInt())
        weeks == 1L -> stringResource(R.string.since_week)
        weeks == 2L -> stringResource(R.string.since_2_weeks)
        days < 30 -> stringResource(R.string.since_days, days.toInt())
        days in 30..60 -> stringResource(R.string.since_month)
        else -> defaultFormattedDate
    }
}
