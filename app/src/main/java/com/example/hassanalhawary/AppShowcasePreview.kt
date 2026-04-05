
package com.example.hassanalhawary

/*
*/
/**
 * ====================================================================
 * 🎨 APP SHOWCASE GALLERY (FIGMA-LIKE CANVAS)
 * ====================================================================
 * Open the "Design" tab in the top right corner of Android Studio.
 * Zoom out (Ctrl + Scroll Wheel or Pinch-to-Zoom) to see all screens
 * side-by-side, visualizing the entire user flow of the app!
 *//*


@Preview(showBackground = true, showSystemUi = false, device = Devices.PIXEL_7, name = "1. تسجيل الدخول (Login)")
@Composable
fun Flow01_LoginScreen() {
    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                com.example.feature.auth.presentation.login.LoginScreenContent(
                    state = com.example.feature.auth.presentation.AuthScreenState(),
                    onEmailChanged = {}, onPasswordChanged = {}, onLoginClick = {},
                    onGoogleLoginClick = {}, onForgotPasswordClick = {}, onRegisterClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false, device = Devices.PIXEL_7, name = "2. الرئيسية (Home)")
@Composable
fun Flow02_HomeScreen() {
    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            com.example.feature.home.presentation.HomeScreen(
                modifier = Modifier.fillMaxSize(),
                uiState = com.example.feature.home.presentation.HomeScreenUiState(
                    latestArticles = listOf(
                        com.example.feature.home.domain.model.ArticleFeed(
                            id = "1",
                            title = "أهمية الصلاة في وقتها",
                            contentPreview = "محتوى المقال..."
                        )
                    ),
                    latestAudios = listOf(
                        com.example.feature.home.domain.model.AudioFeed(
                            id = "1",
                            title = "تفسير سورة الفاتحة",
                            audioUrl = "",
                            duration = 300000
                        )
                    ),
                    loadingLatestArticles = false,
                    loadingLatestAudios = false,
                    loadingImages = false
                ),
                onNavigateToDetailArticle = {},
                onNavigateToDetailAudio = { _, _ -> },
                onCategoryClick = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = false, device = Devices.PIXEL_7, name = "3. البحث (Search)")
@Composable
fun Flow03_SearchScreen() {
    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            com.example.search.presentation.SearchScreenContent(
                searchQuery = "العقيدة",
                onQueryChanged = {}, onSearchClicked = {},
                selectedFilter = com.example.search.presentation.SearchFilter.ALL,
                onFilterSelected = {},
                state = com.example.search.presentation.model.SearchUiState.Idle,
                onNavigateToDetail = {}
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = false,
    device = Devices.PIXEL_7,
    name = "4. المعهد - رحلة العلم (Study Dashboard)"
)
@Composable
fun Flow04_StudyDashboardScreen() {
    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                com.example.study.presentation.dashboard.StudentDashboardContentInternal(
                    studentData = com.example.domain.module.Student(
                        telegramId = 123,
                        name = "أحمد محمد",
                        username = "ahmed123",
                        photoUrl = "",
                        isCourseMember = true,
                        membershipState = "member",
                        isConnectedToTelegram = true,
                        currentLevelId = "level_3",
                        batch = "الدفعة الأولى"
                    ),
                    uiState = com.example.study.presentation.model.DashboardUiState(
                        motivationalMessages = listOf("طلب العلم فريضة على كل مسلم، فاستعن بالله ولا تعجز."),
                        loadingMotivationalMessages = false,
                        loadingLevels = false,
                        hasJourneyAnimationPlayed = true
                    ),
                    onDisconnect = {},
                    onLevelClick = {},
                    onQuizClick = {},
                    onJourneyAnimationFinished = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false, device = Devices.PIXEL_7, name = "5. مشغل الصوتيات (Audio Player)")
@Composable
fun Flow05_AudioPlayerScreen() {
    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            com.example.feature.audio.presentation.detail.AudioDetailScreen(
                uiState = com.example.feature.audio.presentation.detail.AudioDetailUiState(
                    title = "مقدمة في العقيدة الواسطية",
                    description = "شرح مفصل للمقدمة مع بيان أهمية دراسة العقيدة في حياة المسلم.",
                    totalDurationMillis = 3600000L, currentPositionMillis = 1200000L,
                    isPlaying = true, isDownloaded = true, isLoadingDetails = false
                ),
                onNavigateUp = {}, onPlayPauseToggle = {}, onSeek = {}, onRewind = {},
                onForward = {}, onDownload = {}, onShare = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = false, device = Devices.PIXEL_7, name = "6. تفاصيل الدرس (Lesson Detail)")
@Composable
fun Flow06_LessonDetailScreen() {
    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            com.example.study.presentation.detail.LessonDetailContent(
                uiState = com.example.study.presentation.detail.PlayerUiState(
                    lesson = com.example.domain.module.Lesson(
                        id = "1", title = "شرح متن الآجرومية - الدرس الثالث", order = 3,
                        audioUrl = "", pdfUrl = "summary.pdf", duration = "45:00"
                    ),
                    isPlaying = false,
                    currentPosition = 300000L,
                    totalDuration = 2700000L,
                    isLoading = false
                ),
                onNavigateBack = {}, onPlayPauseClick = {}, onSeekForward = {},
                onSeekBackward = {}, onSeekBarPositionChanged = {}, onOpenPdfClick = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = false, device = Devices.PIXEL_7, name = "7. الحساب الشخصي (Profile)")
@Composable
fun Flow07_ProfileScreen() {
    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            com.example.profile.presentation.profile.ProfileScreenContent(
                isAdmin = false,
                state = com.example.profile.presentation.profile.ProfileUiState(
                    userData = com.example.domain.module.UserData(
                        "1",
                        "أحمد محمد",
                        "ahmed@example.com",
                        null,
                        null
                    )
                ),
                isDarkTheme = false,
                onNavigate = {}, onLogout = {}, onThemeChanged = {}
            )
        }
    }
}*/
