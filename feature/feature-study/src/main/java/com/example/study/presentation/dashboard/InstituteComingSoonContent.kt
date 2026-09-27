package com.example.study.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil.compose.AsyncImage
import com.example.core.ui.R
import com.example.core.ui.components.Illustration
import com.example.core.ui.components.IllustrationBox
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.Student
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.BidiText
import com.example.study.presentation.utils.formatBatchName

/**
 * Shown instead of [StudentDashboardContent] while student interaction is
 * temporarily paused. Remove this file and switch StudyScreen back to
 * StudentDashboardContent to re-enable the dashboard.
 *
 * No "open channel" button: the institute channel's URL isn't in the app. No "notify me"
 * either: it needs a backend (FCM topic + sender).
 */
@Composable
fun InstituteComingSoonContent(
    studentData: Student,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { InstituteProfileCard(studentData) }
        item { ComingSoonCard(isLinked = studentData.isConnectedToTelegram) }
    }
}

/** Avatar with an accent ring, name, "@handle", and the membership status chip. */
@Composable
private fun InstituteProfileCard(student: Student) {
    val colors = Brand.colors
    val status = if (student.isCourseMember) {
        val base = stringResource(R.string.institute_student)
        student.batch?.let { base + ArabicNumerals.DATE_SEPARATOR + formatBatchName(it) } ?: base
    } else {
        stringResource(R.string.not_institute_student)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(0.5.dp, colors.divider),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = student.photoUrl,
                contentDescription = null,
                placeholder = painterResource(R.drawable.dr_hassan_photo),
                error = painterResource(R.drawable.dr_hassan_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .border(2.dp, colors.accent, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val handle = BidiText.handle(student.username)
                if (handle.isNotEmpty()) {
                    Text(text = handle, style = MaterialTheme.typography.bodySmall, color = colors.textMuted, maxLines = 1)
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            if (student.isCourseMember) colors.successContainer else colors.surfaceMuted,
                            RoundedCornerShape(50),
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .background(if (student.isCourseMember) colors.success else colors.textMuted, CircleShape),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (student.isCourseMember) colors.onSuccessContainer else colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ComingSoonCard(isLinked: Boolean) {
    val colors = Brand.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(0.5.dp, colors.divider),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IllustrationBox(Illustration.Journey, height = 160.dp)
            Spacer(Modifier.height(14.dp))
            Text(
                text = "منصة المعهد قريبًا",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "نجهّز محتوى الدراسة داخل التطبيق، وسيُفعَّل قريبًا إن شاء الله.",
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 1.7.em),
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            if (isLinked) {
                HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = colors.divider)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(TablerIcons.CircleCheck),
                        contentDescription = null,
                        tint = colors.success,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "تم ربط حسابك بقناة المعهد على تيليجرام",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
            }
        }
    }
}

private val previewStudent = Student(
    telegramId = 123,
    name = "علي حسن",
    username = "ali_7assan",
    photoUrl = "",
    isCourseMember = true,
    membershipState = "member",
    isConnectedToTelegram = true,
    batch = "batch_1",
)

@Preview(name = "Institute - light", locale = "ar", widthDp = 360, heightDp = 560, showBackground = true, backgroundColor = 0xFFF4EEE5)
@Composable
private fun InstituteComingSoonLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) { InstituteComingSoonContent(studentData = previewStudent) }
}

@Preview(name = "Institute - dark", locale = "ar", widthDp = 360, heightDp = 560, showBackground = true, backgroundColor = 0xFF1A1512)
@Composable
private fun InstituteComingSoonDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { InstituteComingSoonContent(studentData = previewStudent) }
}
