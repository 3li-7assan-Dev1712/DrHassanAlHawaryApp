package com.example.study.presentation.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.core.ui.R
import com.example.core.ui.animation.LoadingScreen
import com.example.core.ui.components.shimmer
import com.example.domain.module.Level
import com.example.domain.module.QuizType
import com.example.domain.module.Student
import com.example.study.presentation.model.DashboardSection

@Composable
fun StudentDashboardContent(
    studentData: Student,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    onDisconnect: () -> Unit,
    onLevelClick: (String) -> Unit,
    onQuizClick: (String) -> Unit,
) {
    var selectedSection by remember { mutableStateOf<DashboardSection>(DashboardSection.Study) }
    val uiState by dashboardViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            val userRank = remember(uiState.topStudents, studentData.telegramId) {
                val rank =
                    uiState.topStudents.indexOfFirst { it.telegramId == studentData.telegramId }
                if (rank != -1) rank + 1 else null
            }

            StudentHeader(
                name = studentData.name,
                username = studentData.username,
                photoUrl = studentData.photoUrl,
                isMember = studentData.isCourseMember,
                userScore = uiState.userQuizScore,
                totalQuestions = uiState.latestQuizTotalQuestions,
                userRank = userRank
            )
        }

        if (uiState.hasNewQuiz && uiState.latestQuizId != null && uiState.userQuizScore == null) {
            item {
                QuizReminderSection(
                    quizId = uiState.latestQuizId!!,
                    quizType = uiState.latestQuizType ?: QuizType.WEEKLY,
                    onClick = { onQuizClick(it) }
                )
            }
        }

        item {
            MotivationMessagesSection(
                messages = uiState.motivationalMessages,
                isLoading = uiState.loadingMotivationalMessages
            )
        }

        item {
            DashboardChips(
                selectedSection = selectedSection,
                onSectionSelected = { newSection -> selectedSection = newSection }
            )
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (selectedSection) {
                    DashboardSection.Study -> {
                        LevelsJourneyMap(
                            levels = List(6) {
                                LevelNode(index = it + 1, isUnlocked = it < uiState.levels.count())
                            },
                            isLoading = uiState.loadingLevels,
                            currentLevelIndex = 3,
                            hasPlayedAnimation = uiState.hasJourneyAnimationPlayed,
                            onAnimationFinished = {
                                dashboardViewModel.onJourneyAnimationFinished()
                            },
                            onNodeClick = { index ->
                                val levelId = "level_$index"
                                onLevelClick(levelId)
                            }
                        )
                    }

                    DashboardSection.TopStudents -> TopStudentsContent()

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun QuizReminderSection(
    quizId: String,
    quizType: QuizType,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFinalExam = quizType == QuizType.FINAL_EXAM
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(quizId) },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFinalExam) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.primary
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFinalExam) Icons.Default.School else Icons.Default.Quiz,
                    contentDescription = null,
                    tint = if (isFinalExam) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                val title = if (isFinalExam) "اختبار نهائي متاح!" else "اختبار أسبوعي جديد متاح!"
                val subtitle =
                    if (isFinalExam) "اجتز هذا الاختبار لتنتقل للمرحلة التالية." else "اضغط هنا للبدء في حل الاختبار."

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MotivationMessagesSection(
    modifier: Modifier = Modifier,
    messages: List<String>,
    isLoading: Boolean
) {


    val pagerState = rememberPagerState(pageCount = { messages.size })

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shimmer(isLoading = isLoading),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp
        ) { pageIndex ->
            MotivationMessageCard(message = messages[pageIndex])
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dots indicator (calmer)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(messages.size) { i ->
                val active = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )
                        .size(if (active) 9.dp else 7.dp)
                )
            }
        }

    }
}

@Composable
fun MotivationMessageCard(message: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(22.dp)

    // Lighter, calmer "hint" style (not heavy card)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 92.dp),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
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
    userScore: Int? = null,
    totalQuestions: Int? = null,
    userRank: Int? = null
) {
    val shape = RoundedCornerShape(28.dp)

    val cardColor = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp, shape = shape, clip = false),
        shape = shape,
        color = cardColor,
        border = BorderStroke(1.dp, outline),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with clean ring
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .shadow(6.dp, CircleShape, clip = false)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(2.dp) // ring thickness
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                            .padding(2.dp)
                    ) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Student Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            placeholder = painterResource(R.drawable.dr_hassan_photo),
                            error = painterResource(R.drawable.dr_hassan_photo),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "@$username",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(10.dp))

                    // Membership chip
                    val statusText = if (isMember) "من طلاب المعهد" else "ليس من طلاب المعهد"
                    val chipBg =
                        if (isMember) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(chipBg)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isMember) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // PERFORMANCE VIEWS INSTEAD OF LOGOUT
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. RANK MEDAL (Emoji Style)
                    if (userRank != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when (userRank) {
                                    1 -> "🥇"
                                    2 -> "🥈"
                                    3 -> "🥉"
                                    else -> "🏅"
                                },
                                fontSize = 24.sp
                            )
                            val suffix = if (userRank in 11..13) "th"
                            else when (userRank % 10) {
                                1 -> "st"
                                2 -> "nd"
                                3 -> "rd"
                                else -> "th"
                            }
                            Text(
                                text = "$userRank$suffix",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // 2. QUIZ GRADE PROGRESS (Smaller)
                    if (userScore != null && totalQuestions != null) {
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val progress = if (totalQuestions > 0) userScore.toFloat() / totalQuestions else 0f
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "$userScore/$totalQuestions",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
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
    val shape = RoundedCornerShape(999.dp)

    val isStudy = selectedSection is DashboardSection.Study
    val selectedIndex = if (isStudy) 0 else 1

    // Sizing close to the mockup
    val height = 56.dp
    val outerPadding = 6.dp

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val totalWidth = maxWidth
        val segmentWidth = (totalWidth - (outerPadding * 2)) / 2

        val indicatorOffset by animateDpAsState(
            targetValue = outerPadding + (segmentWidth * selectedIndex),
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            label = "segmented_offset"
        )

        // Outer pill container (soft glass)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.60f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f), shape)
                .padding(outerPadding)
        ) {
            // Sliding indicator (selected pill)
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
            )

            Row(modifier = Modifier.fillMaxSize()) {
                SegmentItem(
                    width = segmentWidth,
                    text = stringResource(R.string.study),
                    selected = isStudy,
                    onClick = { onSectionSelected(DashboardSection.Study) }
                )

                SegmentItem(
                    width = segmentWidth,
                    text = stringResource(R.string.competition),
                    selected = !isStudy,
                    onClick = { onSectionSelected(DashboardSection.TopStudents) }
                )
            }
        }
    }
}

@Composable
private fun SegmentItem(
    width: Dp,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}


@Composable
fun PolishedStudyTopBar(
    modifier: Modifier = Modifier,
    titleLine1: String = "مَعْهَدُ الشَّيْخِ حَسَنِ الهُوَّارِي الفِقْهِ",
    titleLine2: String = "فقه وتأصيل",
) {
    // Colors close to your screenshot (teal -> deeper teal)
    val top = Color(0xFF3A6871)
    val bottom = Color(0xFF2E5E67)

    // Accent mint pill
    val mint1 = Color(0xFF2BC2A5)
    val mint2 = Color(0xFF1FAF96)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(top, bottom)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = titleLine1,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(Modifier.height(4.dp))

                // Ornament row: line + diamond + mint pill + diamond + line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OrnamentLine(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.55f)
                    )
                    OrnamentDiamond(color = Color.White.copy(alpha = 0.65f))
                    Spacer(Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(mint1, mint2)
                                )
                            )
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = titleLine2,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(Modifier.width(10.dp))
                    OrnamentDiamond(color = Color.White.copy(alpha = 0.65f))
                    OrnamentLine(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrnamentLine(modifier: Modifier, color: Color) {
    Box(
        modifier = modifier
            .height(1.dp)
            .background(color)
    )
}

@Composable
private fun OrnamentDiamond(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}


/**
 * The main screen to display the list of levels.
 */
@Composable
fun LevelsContent(
    levels: List<Level>,
    onLevelClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (levels.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingScreen()
        }
    } else {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(levels.size) { index ->
                    LevelItem(
                        level = levels[index],
                        onClick = { onLevelClick(levels[index].id) })
                }
            }
        }
    }
}

@Composable
fun LevelItem(
    level: Level,
    onClick: () -> Unit
) {
    val backgroundColor = if (!level.isLocked) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.80f)
    }

    val contentColor = if (!level.isLocked) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = !level.isLocked, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = level.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = if (level.isLocked) "Unlocked" else "Locked",
                    fontSize = 14.sp,
                    color = contentColor
                )
            }
            if (level.isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Enter Level",
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
            }
        }
    }
}

val sampleLevels = listOf(
    Level("1", "Level 1", 1, true),
    Level("2", "Level 2", 2, false),
    Level("3", "Level 3", 3, false),
)

@Preview(showBackground = true)
@Composable
fun LevelsContentPreview() {
    MaterialTheme {
        LevelsContent(
            levels = sampleLevels,
            modifier = Modifier,
            onLevelClick = {}
        )
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
        }
    }
}

@Preview
@Composable
private fun StudentHeaderPreview() {
    StudentHeader(
        name = "Hassan Al-Hawary",
        username = "3li_7assan",
        photoUrl = null,
        isMember = true,
        userScore = 15,
        totalQuestions = 20,
        userRank = 1
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(sampleLevels.size) { index ->
            LevelItem(
                level = sampleLevels[index],
                onClick = {})
        }
    }
}
