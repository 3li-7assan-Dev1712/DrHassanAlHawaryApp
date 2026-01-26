package com.example.study.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.ui.R
import com.example.study.domain.model.Student
import com.example.study.presentation.model.DashboardSection

@Composable
fun StudentDashboardContent(studentData: Student, onDisconnect: () -> Unit) {

    var selectedSection by remember { mutableStateOf<DashboardSection>(DashboardSection.Study) }


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp) // Space between sections
    ) {

        item {
            BannerSection()
        }

        item {
            StudentHeader(
                name = studentData.name,
                username = studentData.username,
                photoUrl = studentData.photoUrl,
                isMember = studentData.isCourseMember,
                onDisconnect = onDisconnect
            )
        }

        item {
            DashboardChips(
                selectedSection = selectedSection,
                onSectionSelected = { newSection ->
                    selectedSection = newSection
                }
            )
        }

        item {
            // A Box to elegantly switch content based on the selected chip
            Box(modifier = Modifier.fillMaxWidth()) {
                when (selectedSection) {
                    DashboardSection.Study -> LessonsContent()
                    DashboardSection.TopStudents -> TopStudentsContent()
                    DashboardSection.Summaries -> SummariesContent()
                }
            }
        }
    }
}


@Composable
fun BannerSection(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Image(
            painter = painterResource(id = R.drawable.main_banner),
            contentDescription = stringResource(R.string.main_screen), //
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun StudentHeader(
    name: String,
    username: String,
    photoUrl: String?,
    isMember: Boolean,
    onDisconnect: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Student Avatar",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        placeholder = painterResource(R.drawable.dr_hassan_photo),
                        error = painterResource(R.drawable.dr_hassan_photo)
                    )
                    Column {
                        Text(name, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "@$username")

                    }
                }
                OutlinedButton(onClick = onDisconnect) {
                    Icon(Icons.Default.LinkOff, contentDescription = "Disconnect")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Membership Status Badge
            val statusText = if (isMember) "من طلاب المعهد" else "ليس من طلاب المعهد"
            val backgroundColor =
                if (isMember) Color(0xFF4CAF50) else MaterialTheme.colorScheme.secondary
            val textColor = Color.White
            Text(
                text = statusText,
                color = textColor,
                modifier = Modifier
                    .background(
                        backgroundColor,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun DashboardChips(
    selectedSection: DashboardSection,
    onSectionSelected: (DashboardSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        // Study Chip
        FilterChip(
            selected = selectedSection is DashboardSection.Study,
            onClick = { onSectionSelected(DashboardSection.Study) },
            label = { Text(text = stringResource(R.string.study)) }
        )

        // Top Students Chip
        FilterChip(
            selected = selectedSection is DashboardSection.TopStudents,
            onClick = { onSectionSelected(DashboardSection.TopStudents) },
            label = { Text(text = stringResource(R.string.competition)) }
        )

        // Summaries Chip
        FilterChip(
            selected = selectedSection is DashboardSection.Summaries,
            onClick = { onSectionSelected(DashboardSection.Summaries) },
            label = { Text(text = stringResource(R.string.summary)) }
        )
    }
}


@Composable
fun SummariesContent(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Summaries Section", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Here students can browse public summaries and upload their own.")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { /* TODO: Handle upload summary */ }) {
                Text("Upload My Summary")
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
private fun StudentHeaderPreview() {
    StudentHeader(
        name = "Hassan Al-Hawary",
        username = "3li_7assan",
        photoUrl = null,
        isMember = true,
        onDisconnect = {}
    )
}

