package com.example.admin.ui.institute_main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.admin.ui.theme.HassanAlHawaryTheme
import com.example.core.ui.R
import com.example.domain.module.Student

@Composable
fun InstituteMainScreen(
    viewModel: InstituteViewModel = hiltViewModel(),
    onUploadQuiz: () -> Unit,
    onUploadAnnouncement: () -> Unit,
    onUploadMotivationalMessages: () -> Unit,
    onLevelClick: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is InstituteScreenUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is InstituteScreenUiState.AdminDashboard -> {
            AdminDashboard(
                student = state.studentData,
                isRefreshing = state.isRefreshing,
                onUploadQuiz = onUploadQuiz,
                onUploadAnnouncement = onUploadAnnouncement,
                onUploadMotivationalMessages = onUploadMotivationalMessages,
                onLevelClick = onLevelClick,
            )
        }

        is InstituteScreenUiState.Guest -> {
            GuestScreen(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::onRefreshData
            )
        }

        is InstituteScreenUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AdminDashboard(
    student: Student,
    isRefreshing: Boolean,
    onUploadQuiz: () -> Unit,
    onUploadAnnouncement: () -> Unit,
    onUploadMotivationalMessages: () -> Unit,
    onLevelClick: (String) -> Unit,
) {
    val levels = (1..6).map { "level_$it" }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isRefreshing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                TelegramProfileHeader(
                    name = student.name,
                    username = "@${student.username}",
                    photoUrl = student.photoUrl,
                    membershipState = student.membershipState,
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UploadActionCard(
                        title = "Upload Quiz",
                        icon = Icons.Default.Quiz,
                        onClick = onUploadQuiz,
                        modifier = Modifier.weight(1f)
                    )
                    UploadActionCard(
                        title = "Upload Announcement",
                        icon = Icons.AutoMirrored.Default.Announcement,
                        onClick = onUploadAnnouncement,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                UploadActionCard(
                    title = "Motivational Messages",
                    icon = Icons.Default.Forum,
                    onClick = onUploadMotivationalMessages,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "Institute Levels",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            items(levels) { level ->
                LevelListItem(
                    levelName = level,
                    onClick = { onLevelClick(level) }
                )
            }
        }
    }
}

@Composable
fun GuestScreen(
    isRefreshing: Boolean,
    onRefresh: () -> Unit = {}
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "You are not allowed to upload admin data.",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isRefreshing) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    onRefresh()
                }
            ) {
                Text("Refresh")
            }
        }
    }
}

@Composable
fun TelegramProfileHeader(
    name: String,
    username: String,
    photoUrl: String,
    membershipState: String,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(64.dp)
            ) {
                AsyncImage(
                    model = photoUrl,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Profile Photo",
                    placeholder = painterResource(R.drawable.student_icon)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                AssistChip(
                    onClick = { /* No-op */ },
                    label = { Text("#$membershipState") },
                    leadingIcon = {
                        Icon(
                            if (membershipState == "creator" || membershipState == "administrator")
                                Icons.Default.Verified else
                                Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun LevelListItem(
    levelName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = levelName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go to $levelName",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun InstituteMainScreenPreview() {
    HassanAlHawaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val student = Student(
                telegramId = 123,
                name = "Hassan Al-Hawary",
                username = "hassan.alhawary",
                photoUrl = "",
                isCourseMember = true,
                membershipState = "admin",
                isConnectedToTelegram = false
            )
            AdminDashboard(student, false, {}, {}, {}, {})
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun GuestScreenPreview() {
    HassanAlHawaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GuestScreen(false)
        }
    }
}
