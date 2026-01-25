package com.example.study.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.ui.R
import com.example.study.domain.model.Student

@Composable
fun StudentDashboardContent(studentData: Student, onDisconnect: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp) // Space between sections
    ) {
        item {
            StudentHeader(
                name = studentData.name,
                photoUrl = studentData.photoUrl,
                isMember = studentData.isCourseMember,
                onDisconnect = onDisconnect
            )
        }


    }
}

@Composable
fun StudentHeader(name: String, photoUrl: String?, isMember: Boolean, onDisconnect: () -> Unit) {
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
                    Text(name, style = MaterialTheme.typography.titleLarge)
                }
                OutlinedButton(onClick = onDisconnect) {
                    Icon(Icons.Default.LinkOff, contentDescription = "Disconnect")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Membership Status Badge
            val statusText = if (isMember) "Active Member" else "Not a Member Yet"
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
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

