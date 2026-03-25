package com.example.feature.about_dr_hassan.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.ui.R
import com.example.domain.module.doctorProfileData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDrHassanScreen(
    onNavigateBack: () -> Unit
) {
    val doctor = doctorProfileData
    val scrollState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_dr_hassan)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        },

        ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            state = scrollState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            item {
                HeroSection(
                    name = doctor.name,
                    title = doctor.title,
                    imageUrl = doctor.profileImageUrl
                )
            }

            item {
                SocialRow()
            }

            item {
                InfoCard(
                    title = "نبذة تعريفية",
                    content = doctor.bio
                )
            }

            item {
                ListCard(
                    title = "المؤهلات الأكاديمية 🎓",
                    items = doctor.education
                )
            }

            item {
                ListCard(
                    title = "المساهمات الإعلامية 📺",
                    items = doctor.mediaResponsibilities
                )
            }

            item {
                ListCard(
                    title = "المساهمات التعليمية ✍\uFE0F",
                    items = doctor.studyingResponsibilities
                )
            }

            item {
                ListCard(
                    title = "البحوث العلمية 📚",
                    items = doctor.researches
                )
            }
        }
    }
}

@Composable
fun HeroSection(
    name: String,
    title: String,
    imageUrl: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AsyncImage(
            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
            model = imageUrl,
            contentDescription = "Profile",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.dr_hassan_image),
            error = painterResource(R.drawable.dr_hassan_image)
        )

        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SocialRow() {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        SocialButton(Icons.Default.Email) {
            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")))
        }

        SocialButton(Icons.Default.Phone) {
            context.startActivity(Intent(Intent.ACTION_DIAL))
        }

        SocialButton(Icons.Default.Public) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com")))
        }

        SocialButton(Icons.Default.Share) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Check this app!")
            }
            context.startActivity(intent)
        }
    }
}

@Composable
fun SocialButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(52.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Icon(icon, contentDescription = null)
    }
}

@Composable
fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(title, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun ListCard(title: String, items: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(title, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { item ->
                    Row {
                        Text("• ", fontWeight = FontWeight.Bold)
                        Text(item, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}