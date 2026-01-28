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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
fun StudentDashboardContent(
    studentData: Student,
    onDisconnect: () -> Unit,
    onLevelClick: (Int) -> Unit,
) {

    var selectedSection by remember { mutableStateOf<DashboardSection>(DashboardSection.Study) }


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp) // Space between sections
    ) {


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
            MotivationMessagesSection()
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
                    DashboardSection.Study -> LevelsContent(levels = sampleLevels, onLevelClick = {
                        onLevelClick(it)
                    })

                    DashboardSection.TopStudents -> TopStudentsContent()
                    DashboardSection.Summaries -> SummariesContent()
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyTopAppBar(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.top_bar_banner),
        contentDescription = stringResource(R.string.app_name),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentScale = ContentScale.Crop
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class) // Required for Pager
@Composable
fun MotivationMessagesSection(modifier: Modifier = Modifier) {
    val messages = remember {
        listOf(
            "اطلب العلم ولا تكسل، فما أبعد الخير عن أهل الكسل.",
            "كل إناء يضيق بما جعل فيه إلا وعاء العلم، فإنه يتسع.",
            "لا يزال المرء عالماً ما طلب العلم، فإذا ظن أنه قد علم، فقد جهل.",
            "العلم يرفع بيوتاً لا عماد لها، والجهل يهدم بيت العز والشرف.",
            "رحلة الألف ميل تبدأ بخطوة. خطوتك اليوم هي علم تتعلمه.",
            "من لم يذق مر التعلم ساعة، تجرع ذل الجهل طول حياته.",
            "استثمر في نفسك، فالعلم هو الزاد الذي لا ينضب."
        )
    }

    // 1. Remember the state for the pager (current page, etc.)
    val pagerState = rememberPagerState(pageCount = { messages.size })

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally // Center the dots indicator
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
        ) { pageIndex ->
            MotivationMessageCard(message = messages[pageIndex])
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Dots Indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(messages.size) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@Composable
fun MotivationMessageCard(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth() // Each card will fill the available space within the pager's content padding
            .height(100.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(0.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lightbulb,
                contentDescription = null, // Decorative icon
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "@$username")

            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                OutlinedButton(onClick = onDisconnect) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Disconnect")
                }

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
            label = {
                Text(
                    text = stringResource(
                        R.string.study,

                        ),
                    modifier = Modifier.padding(8.dp)
                )
            }
        )

        // Top Students Chip
        FilterChip(
            selected = selectedSection is DashboardSection.TopStudents,
            onClick = { onSectionSelected(DashboardSection.TopStudents) },
            label = {
                Text(
                    text = stringResource(R.string.competition),
                    modifier = Modifier.padding(8.dp)
                )
            }
        )

        /* // Summaries Chip
         FilterChip(
             selected = selectedSection is DashboardSection.Summaries,
             onClick = { onSectionSelected(DashboardSection.Summaries) },
             label = {
                 Text(
                     text = stringResource(R.string.summary),
                     modifier = Modifier.padding(8.dp)
                 )
             }
         )*/
    }
}


@Composable
fun SummariesContent(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
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


@Preview()
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

@Preview
@Composable
private fun MotivationMessageCardPreview() {
    MotivationMessageCard("Sample Message")
}

@Preview
@Composable
private fun DashboardChipsPreview() {
    DashboardChips(selectedSection = DashboardSection.Study, onSectionSelected = {})
}

@Preview
@Composable
private fun LevelsContentPrev() {
    LevelsContent(
        levels = sampleLevels,
        onLevelClick = {}
    )
}