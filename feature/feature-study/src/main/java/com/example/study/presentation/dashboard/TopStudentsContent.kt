package com.example.study.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.ui.R
import com.example.domain.module.LeaderboardStudent

@Composable
fun TopStudentsContent(modifier: Modifier = Modifier) {
    val leaderboard = remember {
        listOf(
            LeaderboardStudent(1, "أحمد محمد", 5800),
            LeaderboardStudent(2, "فاطمة الزهراء", 5750),
            LeaderboardStudent(3, "عبد الرحمن علي", 5600),
            LeaderboardStudent(4, "سارة إبراهيم", 5400),
            LeaderboardStudent(5, "يوسف خالد", 5350)
        )
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.competition),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(400.dp) // Example fixed height
        ) {
            items(leaderboard) { student ->
                LeaderboardItem(student = student)
            }
        }
    }
}

@Composable
fun LeaderboardItem(
    student: LeaderboardStudent,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rank
            Text(text = "#${student.rank}", style = MaterialTheme.typography.titleMedium)

            // Avatar
            AsyncImage(
                model = student.photoUrl,
                contentDescription = student.name,
                placeholder = painterResource(id = R.drawable.dr_hassan_photo),
                error = painterResource(id = R.drawable.dr_hassan_photo),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )

            // Name
            Text(
                text = student.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f) // Takes up remaining space
            )

            // Score
            Text(
                text = "${student.score}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}