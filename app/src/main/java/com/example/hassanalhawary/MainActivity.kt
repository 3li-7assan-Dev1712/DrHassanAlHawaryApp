package app.netlify.devalihassan

import com.example.core.ui.theme.Brand
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.animation.PathInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.AnimatedContentScope
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import app.netlify.devalihassan.ui.navigation.AppRail
import com.example.core.ui.components.WindowMargin
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.theme.layoutTokens
import com.example.core.ui.theme.WindowClass
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.NavBackStackEntry
import com.example.core.ui.theme.LocalSharedTransitionScope
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.ReducedMotionOverride
import com.example.core.ui.theme.animationsRemoved
import com.example.core.ui.theme.ProvideNavAnimatedScope
import com.example.core.ui.theme.SharedKeys
import com.example.core.ui.theme.sharedContainer
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.RectangleShape
import backgroundDark
import backgroundLight
import kotlinx.coroutines.delay
import com.example.core.ui.theme.reducedMotion
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.netlify.devalihassan.core.util.LocaleForce
import app.netlify.devalihassan.ui.navigation.BottomNavigationBar
import app.netlify.devalihassan.ui.navigation.Routes
import app.netlify.devalihassan.ui.theme.HassanAlHawaryTheme
import com.example.core.ui.components.ContentCategories
import com.example.core.ui.components.UpdateScreen
import com.example.core_ui.splash_screen.SplashScreen
import com.example.feature.about_dr_hassan.presentation.AboutDrHassanScreen
import com.example.feature.article.presentation.detail.ArticleDetailScreen
import com.example.feature.article.presentation.detail.ArticleReaderPane
import com.example.feature.article.presentation.detail.ARTICLE_READER_PANE_KEY
import com.example.feature.audio.presentation.detail.AudioPlayerPane
import com.example.domain.module.SearchResultMetaData
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.feature.article.presentation.list.ArticleListScreen
import com.example.feature.article.presentation.share.ArticleShareSelectionScreen
import com.example.feature.audio.presentation.category.AudioCategoryScreen
import com.example.feature.audio.presentation.detail.AudioDetailScreen
import com.example.feature.audio.presentation.detail.AudioSelection
import com.example.feature.audio.presentation.detail.AudioShareAction
import com.example.feature.audio.presentation.list.AudioListScreen
import com.example.feature.auth.presentation.auth.AuthScreen
import com.example.feature.home.presentation.HomeScreen
import com.example.feature.home.presentation.notifications.NotificationsScreen
import com.example.feature.image.presentation.detail.ImageScreen
import com.example.feature.image.presentation.list.ImagesGroupsScreen
import com.example.feature.onboarding.presentation.OnboardingScreen
import com.example.feature.share.presentation.SharePreviewScreen
import com.example.feature.share.presentation.TextCardPreviewScreen
import com.example.feature.video.presentation.category.ALL_VIDEO_CATEGORIES_ID
import com.example.feature.video.presentation.category.VideoCategoryScreen
import com.example.feature.video.presentation.detail.VideoPlayerScreen
import com.example.feature.video.presentation.list.VideosScreen
import com.example.profile.presentation.about_app.AboutAppScreen
import com.example.profile.presentation.components.LegalTextScreen
import com.example.profile.presentation.components.ProfileRoute
import com.example.profile.presentation.navigation.ProfileDestinations
import com.example.profile.presentation.profile.ProfileScreen
import com.example.profile.presentation.share_app.ShareAppScreen
import com.example.profile.presentation.support.SupportScreen
import com.example.search.presentation.SearchScreen
import com.example.study.presentation.StudyScreen
import com.example.study.presentation.detail.LessonDetailScreen
import com.example.study.presentation.lessons.LessonsListScreen
import com.example.study.presentation.playlist.PlaylistScreen
import com.example.study.presentation.quiz.AnswerQuizScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleForce.wrap(newBase))
    }

    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    val TAG = "MainActivity"
    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        // Debug builds only: launch with
        //   adb shell am start -n app.netlify.devalihassan/.MainActivity --ez force_reduced_motion true
        // to test the "Remove animations" behaviour without changing the phone's settings.
        if (BuildConfig.DEBUG && intent?.getBooleanExtra(EXTRA_FORCE_REDUCED_MOTION, false) == true) {
            ReducedMotionOverride.forced = true
        }
        val splashScreen = installSplashScreen()


        splashScreen.setKeepOnScreenCondition {
            !mainActivityViewModel.appReady.value
        }

        // Exit: the logo fades and shrinks to 0.9 while the splash fades over `medium`,
        // revealing the first screen underneath. Removed at once under reduced motion.
        splashScreen.setOnExitAnimationListener { provider ->
            if (animationsRemoved()) {
                provider.remove()
                return@setOnExitAnimationListener
            }
            val leaving = PathInterpolator(0.3f, 0f, 0.8f, 0.15f) // Motion.EmphasizedAccelerate
            runCatching {
                provider.iconView.animate()
                    .alpha(0f).scaleX(0.9f).scaleY(0.9f)
                    .setDuration(Motion.MEDIUM.toLong())
                    .setInterpolator(leaving)
                    .start()
            }
            provider.view.animate()
                .alpha(0f)
                .setDuration(Motion.MEDIUM.toLong())
                .setInterpolator(leaving)
                .withEndAction { provider.remove() }
                .start()
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeState by mainActivityViewModel.themeState.collectAsState()
            val onboardingCompleted by mainActivityViewModel.onboardingCompleted.collectAsState()
            val mainActivityState by mainActivityViewModel.state.collectAsState()
            val appConfig by mainActivityViewModel.appConfig.collectAsState()

            if (!mainActivityViewModel.appReady.collectAsState().value) return@setContent

            // "تلقائي" follows the phone; otherwise the explicit light/dark choice.
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = if (themeState.followSystem) systemDark else themeState.isDarkTheme

            HassanAlHawaryTheme(darkTheme = isDarkTheme, brandTheme = themeState.brandTheme) {

                // Follow the app's own light/dark setting, not the phone's: the plain
                // enableEdgeToEdge() above picks bar icon colours from the system mode, so
                // they vanished whenever the two differed. The window background (the XML
                // theme is Material.Light) is repainted too, so nothing flashes white in
                // dark mode. Declared before the screens so their own overrides (home and
                // sign-in force light icons) apply on top.
                // The colours animate over `medium` on a theme switch; the bar icons (and the
                // window background behind everything) flip once that has finished, so they
                // never contrast wrongly with the half-way colours.
                val reducedMotionOn = reducedMotion
                var isFirstBarStyle by remember { mutableStateOf(true) }
                LaunchedEffect(isDarkTheme) {
                    if (!isFirstBarStyle && !reducedMotionOn) delay(Motion.MEDIUM.toLong())
                    isFirstBarStyle = false
                    val transparent = android.graphics.Color.TRANSPARENT
                    val barStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(transparent)
                    } else {
                        SystemBarStyle.light(transparent, transparent)
                    }
                    enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
                    val windowBackground = if (isDarkTheme) backgroundDark else backgroundLight
                    window.decorView.setBackgroundColor(windowBackground.toArgb())
                }

                var flexibleUpdateDismissed by remember { mutableStateOf(false) }

                // Determine update type
                val updateType = remember(appConfig, flexibleUpdateDismissed) {
                    val currentVersion = BuildConfig.VERSION_CODE
                    val minVersion = appConfig?.minVersionCode ?: 0
                    val latestVersion = appConfig?.latestVersionCode ?: 0

                    when {
                        currentVersion < minVersion -> "force"
                        currentVersion < latestVersion && !flexibleUpdateDismissed -> "flexible"
                        else -> "none"
                    }
                }

                if (updateType != "none" && appConfig != null) {
                    UpdateScreen(
                        updateUrl = appConfig!!.updateUrl,
                        isForceUpdate = updateType == "force",
                        onDismiss = { flexibleUpdateDismissed = true }
                    )
                } else {
                    when (onboardingCompleted) {
                        null -> {
                            // still loading onboarding flag -> splash is still visible anyway
                            return@HassanAlHawaryTheme
                        }

                        false -> {
                            OnboardingScreen(
                                onFinished = { mainActivityViewModel.updateOnboardingCompleted() }
                            )
                        }

                        true -> {
                            when {
                                mainActivityState.isLoading -> {
                                    Box(
                                        Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                mainActivityState.isUserLoggedIn -> {
                                    val deepLinkUri = intent?.data
                                    MainAppContent(
                                        onLogout = { mainActivityViewModel.logoutSuccess() },
                                        isDarkThemeEnabled = isDarkTheme,
                                        followSystemTheme = themeState.followSystem,
                                        userEmail = mainActivityState.currentUserDate?.email ?: "",
                                        idToken = mainActivityState.idToken ?: "",
                                        deepLinkUri = deepLinkUri

                                    )
                                }

                                else -> {
                                    AuthScreen(onSuccessfulAuth = {
                                        mainActivityViewModel.loginSuccess()
                                    })
                                }
                            }
                        }
                    }
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = {

                    }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
    }


    @Composable
    @UnstableApi
    fun MainAppContent(
        onLogout: () -> Unit,
        isDarkThemeEnabled: Boolean = false,
        followSystemTheme: Boolean = false,
        userEmail: String,
        idToken: String,
        deepLinkUri: Uri?
    ) {
        val navController = rememberNavController()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // routes where the bottom nav should be shown
        val routesWithBottomNav = remember {
            setOf(
                Routes.HOME_SCREEN,
                Routes.SEARCH_SCREEN,
                Routes.PROFILE_SCREEN,
                "${Routes.STUDY_SCREEN}?data={data}",
            )
        }

        var handled by remember { mutableStateOf(false) }

        LaunchedEffect(deepLinkUri) {
            if (!handled && deepLinkUri != null) {
                handled = true

                val data = deepLinkUri.getQueryParameter("data")
                if (data != null) {
                    val encodedData = Uri.encode(data)

                    navController.navigate("${Routes.STUDY_SCREEN}?data=$encodedData") {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }

        val shouldShowBottomNav = remember(currentRoute) {
            derivedStateOf {
                currentRoute != null && routesWithBottomNav.any { routePattern ->
                    // Simple check for exact match or prefix match for routes with arguments
                    if (routePattern.contains("{")) {
                        val baseRoutePattern = routePattern.substringBefore("/{")
                        currentRoute.startsWith(baseRoutePattern)
                    } else {
                        currentRoute == routePattern
                    }
                }
            }.value
        }



        // The reader's share action: choosing the passage to share.
        val shareArticle: (String) -> Unit = { articleId ->
            navController.navigate("${Routes.ARTICLE_SHARE_SELECTION_SCREEN}/${Uri.encode(articleId)}")
        }
        // The player's share action (the player screen and the player beside the audio list):
        // the clip share preview, a dialog over the player on a tablet (Figma Share preview).
        val shareAsDialog = !layoutTokens.isCompact
        val shareAudio: AudioShareAction = { audioUrl, title, category, localFilePath, startMs, totalDurationMs ->
            val encodedUrl = Uri.encode(audioUrl)
            val encodedTitle = Uri.encode(title)
            val encodedCategory = Uri.encode(category ?: "")
            val encodedLocalFilePath = Uri.encode(localFilePath ?: "")
            val route = if (shareAsDialog) SHARE_PREVIEW_DIALOG else Routes.SHARE_PREVIEW_SCREEN
            navController.navigate(
                "$route/$encodedUrl?title=$encodedTitle&category=$encodedCategory&localFilePath=$encodedLocalFilePath&startMs=$startMs&totalDurationMs=$totalDurationMs"
            )
        }

        // Medium and Expanded windows: a navigation rail instead of the bottom bar, on every
        // main-app screen (secondary ones too) except the immersive designs viewer.
        val tokens = layoutTokens
        // The designs viewer is immersive on a tablet: no rail, drawn to the window edges on its
        // dark background (it pads for the system bars itself).
        val immersiveViewer = !tokens.isCompact && currentRoute?.startsWith(Routes.IMAGE_DETAIL_SCREEN) == true
        val showRail = !tokens.isCompact && !immersiveViewer

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            // This colour fills the status-bar padding above every screen: the brand
            // background (light or dark with the theme), never the old gray surfaceVariant strip.
            containerColor = Brand.colors.background,
            bottomBar = {
                if (tokens.isCompact && shouldShowBottomNav) {
                    BottomNavigationBar(
                        modifier = Modifier.fillMaxWidth(), navController = navController
                    )
                }
            }) { innerPadding ->

            // Screen transitions, set once here: fade through between bottom-nav tabs,
            // shared axis X (END → START going deeper, mirrored on back) for everything else.
            val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
            val axisOffsetPx = with(LocalDensity.current) { Motion.SHARED_AXIS_OFFSET_DP.dp.roundToPx() }
            val reduced = reducedMotion
            fun AnimatedContentTransitionScope<NavBackStackEntry>.betweenTabs() =
                initialState.destination.route in routesWithBottomNav &&
                    targetState.destination.route in routesWithBottomNav

            // The rail runs the full window height on the start edge (right in RTL) and pads
            // itself for the system bars; the screens keep the Scaffold's insets.
            val layoutDirection = LocalLayoutDirection.current
            // On Medium and Expanded each destination pads for the status and navigation bars
            // itself (WindowMargin; the immersive viewer draws under them), so the container
            // keeps the same bounds through every transition. The rail takes the start side.
            val contentPadding = if (tokens.isCompact) {
                innerPadding
            } else {
                PaddingValues(
                    start = if (showRail) 0.dp else innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                )
            }
            Row(modifier = Modifier.fillMaxSize()) {
            if (showRail) {
                AppRail(
                    navController = navController,
                    modifier = Modifier.padding(start = innerPadding.calculateStartPadding(layoutDirection)),
                )
            }
            // Container transforms (card → screen) for three flows; see core-ui SharedElements.kt.
            SharedTransitionLayout(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(contentPadding)
            ) {
                CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                    NavHost(
                        navController,
                        startDestination = "home_screen",
                        enterTransition = {
                            if (betweenTabs()) Motion.fadeThroughEnter(reduced)
                            else Motion.sharedAxisEnter(axisOffsetPx, isRtl, reduced)
                        },
                        exitTransition = {
                            if (betweenTabs()) Motion.fadeThroughExit(reduced)
                            else Motion.sharedAxisExit(axisOffsetPx, isRtl, reduced)
                        },
                        popEnterTransition = {
                            if (betweenTabs()) Motion.fadeThroughEnter(reduced)
                            else Motion.sharedAxisPopEnter(axisOffsetPx, isRtl, reduced)
                        },
                        popExitTransition = {
                            if (betweenTabs()) Motion.fadeThroughExit(reduced)
                            else Motion.sharedAxisPopExit(axisOffsetPx, isRtl, reduced)
                        },
                    ) {

                        screen("splash_screen") {
                            SplashScreen(
                                onShowSplashScreenTimeEnd = {
                                    navController.navigate("home_screen") {
                                        popUpTo("splash_screen") {
                                            inclusive = true
                                        }
                                    }
                                })
                        }

                        screen("home_screen") {
                            val homeTokens = layoutTokens
                            ProvideNavAnimatedScope(this) {
                            HomeScreen(onNavigateToDetailArticle = { articleId ->
                                navController.navigate("detail_article_screen/$articleId")

                            }, onNavigateToDetailAudio = { title, audioUrl ->
                                val encodedUrl = Uri.encode(audioUrl)
                                val encodedTitle = Uri.encode(title)
                                navController.navigate("audio_detail_screen/$encodedTitle/$encodedUrl")
                            }, onCategoryClick = { route ->
                                when (route) {
                                    Routes.AUDIO_LIST_SCREEN -> navController.navigate(Routes.AUDIO_CATEGORY_SCREEN)
                                    // A tablet has no video categories screen: the categories are pills
                                    // above the videos grid (Figma Videos).
                                    Routes.VIDEOS_SCREEN -> navController.navigate(
                                        if (homeTokens.isCompact) Routes.VIDEO_CATEGORY_SCREEN else Routes.VIDEOS_SCREEN
                                    )
                                    else -> navController.navigate(route)
                                }
                            }, onNotificationsClick = {
                                navController.navigate(Routes.NOTIFICATIONS_SCREEN)
                            }


                            )
                            }
                        }
                        screen("search_screen") { entry ->
                            // Expanded: an article or an audio opens beside the results (Figma
                            // 59:939); videos and designs open their own screens, as on the phone.
                            // The chosen result is kept in the entry (rotation, resizing).
                            val handle = entry.savedStateHandle
                            val selectedId by handle.getStateFlow<String?>(SELECTED_RESULT_ID, null).collectAsState()
                            val selectedType by handle.getStateFlow<String?>(SELECTED_RESULT_TYPE, null).collectAsState()
                            val selectedTitle by handle.getStateFlow<String?>(SELECTED_RESULT_TITLE, null).collectAsState()
                            val selectedUrl by handle.getStateFlow<String?>(SELECTED_RESULT_URL, null).collectAsState()
                            val selected = selectedId?.let {
                                SearchResultMetaData(objectID = it, title = selectedTitle, type = selectedType, url = selectedUrl)
                            }
                            val clearSelection: () -> Unit = { handle[SELECTED_RESULT_ID] = null }
                            val expanded = layoutTokens.isExpanded
                            if (!expanded && selected != null) {
                                // Narrowed to one pane with a result open: it stays open here, on
                                // the same reader or player ViewModel (an audio keeps playing);
                                // back returns to the results.
                                BackHandler(onBack = clearSelection)
                                SearchResultDetail(selected, onBack = clearSelection, onShareArticle = shareArticle, onShareAudio = shareAudio)
                            } else {
                                SearchScreen(
                                    selectedResult = selected.takeIf { expanded },
                                    detailPane = { result ->
                                        SearchResultDetail(result, onBack = null, onShareArticle = shareArticle, onShareAudio = shareAudio)
                                    },
                                ) { searchResultMetaData ->
                                    if (expanded && searchResultMetaData.type in RESULT_TYPES_IN_PANE) {
                                        handle[SELECTED_RESULT_TYPE] = searchResultMetaData.type
                                        handle[SELECTED_RESULT_TITLE] = searchResultMetaData.title
                                        handle[SELECTED_RESULT_URL] = searchResultMetaData.url
                                        handle[SELECTED_RESULT_ID] = searchResultMetaData.objectID
                                    } else {
                                        val encodedUrl = Uri.encode(searchResultMetaData.url)
                                        val encodedTitle = Uri.encode(searchResultMetaData.title)
                                        when (searchResultMetaData.type) {
                                            "article" -> {

                                                val objectID = searchResultMetaData.objectID

                                                val route = "detail_article_screen/$objectID"
                                                Log.d(TAG, "MainAppContent: route")
                                                navController.navigate(route)
                                            }

                                            "audio" -> navController.navigate("audio_detail_screen/${encodedTitle}/${encodedUrl}")
                                            "image_group" -> navController.navigate("${Routes.IMAGE_DETAIL_SCREEN}/${searchResultMetaData.objectID}")
                                            "video" -> navController.navigate("${Routes.VIDEO_PLAYER_SCREEN}/${encodedUrl}/${encodedTitle}")
                                            else -> {

                                            }
                                        }
                                    }
                                }
                            }
                        }
                        screen("articles_screen") { entry ->
                            // Expanded: the article chosen beside the list, kept in the entry so
                            // it survives rotation and resizing. Once the window narrows to one
                            // pane, a chosen article opens in the reader.
                            val selected by entry.savedStateHandle
                                .getStateFlow<String?>(SELECTED_ARTICLE, null).collectAsState()
                            val expanded = layoutTokens.isExpanded
                            LaunchedEffect(expanded) {
                                val chosen = selected
                                if (!expanded && chosen != null) {
                                    entry.savedStateHandle[SELECTED_ARTICLE] = null
                                    navController.navigate("detail_article_screen/$chosen")
                                }
                            }
                            ArticleListScreen(
                                onNavigateToArticleDetail = { articleId ->
                                    navController.navigate("detail_article_screen/$articleId")
                                },
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToShareSelection = { articleId ->
                                    navController.navigate("${Routes.ARTICLE_SHARE_SELECTION_SCREEN}/${Uri.encode(articleId)}")
                                },
                                selectedArticleId = selected,
                                onSelectArticle = { entry.savedStateHandle[SELECTED_ARTICLE] = it },
                            )
                        }
                        screen(
                            // Update the route to include an optional parameter
                            route = "detail_article_screen/{articleId}",
                            arguments = listOf(
                                navArgument("articleId") { type = NavType.StringType },
                            )
                        ) { entry ->
                            val articleId = entry.arguments?.getString("articleId").orEmpty()
                            // Expanded: the reader sits beside the articles (Figma has no reader
                            // of its own there), with this article selected. Another one chosen
                            // there is still the one shown when the window narrows to the reader.
                            val selected by entry.savedStateHandle
                                .getStateFlow(SELECTED_ARTICLE, articleId).collectAsState()
                            val onShare: (String) -> Unit = { id ->
                                navController.navigate("${Routes.ARTICLE_SHARE_SELECTION_SCREEN}/${Uri.encode(id)}")
                            }
                            ProvideNavAnimatedScope(this) {
                            if (layoutTokens.isExpanded) {
                                ArticleListScreen(
                                    onNavigateToArticleDetail = { id -> navController.navigate("detail_article_screen/$id") },
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToShareSelection = onShare,
                                    selectedArticleId = selected,
                                    onSelectArticle = { entry.savedStateHandle[SELECTED_ARTICLE] = it },
                                )
                            } else {
                                ArticleDetailScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToShareSelection = onShare,
                                    modifier = Modifier.sharedContainer(SharedKeys.article(articleId)),
                                    articleId = selected,
                                )
                            }
                            }
                        }

                        screen(
                            route = "${Routes.ARTICLE_SHARE_SELECTION_SCREEN}/{articleId}",
                            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
                        ) {
                            ArticleShareSelectionScreen(
                                onNavigateUp = { navController.popBackStack() },
                                onContinueToPreview = { articleId, selectionStart, selectionEnd ->
                                    // Offsets, not the excerpt: any length can be shared, and a whole
                                    // article URL-encoded into the route would not scale.
                                    navController.navigate(
                                        "${Routes.TEXT_CARD_PREVIEW_SCREEN}/${Uri.encode(articleId)}" +
                                            "?selectionStart=$selectionStart&selectionEnd=$selectionEnd"
                                    )
                                }
                            )
                        }

                        screen(
                            route = "${Routes.TEXT_CARD_PREVIEW_SCREEN}/{articleId}?selectionStart={selectionStart}&selectionEnd={selectionEnd}",
                            arguments = listOf(
                                navArgument("articleId") { type = NavType.StringType },
                                navArgument("selectionStart") { type = NavType.IntType; defaultValue = 0 },
                                navArgument("selectionEnd") { type = NavType.IntType; defaultValue = 0 },
                            )
                        ) {
                            TextCardPreviewScreen(
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                        screen(Routes.AUDIO_CATEGORY_SCREEN) {
                            AudioCategoryScreen(
                                onCategoryClick = { categoryId, categoryTitle ->
                                    if (categoryId == ContentCategories.ALL_ID) {
                                        // "الكل": the list with no category filter (its args are nullable).
                                        navController.navigate(Routes.AUDIO_LIST_SCREEN)
                                    } else {
                                        navController.navigate("${Routes.AUDIO_LIST_SCREEN}?categoryId=$categoryId&categoryTitle=${Uri.encode(categoryTitle)}")
                                    }
                                },
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                        screen(
                            route = "${Routes.AUDIO_LIST_SCREEN}?categoryId={categoryId}&categoryTitle={categoryTitle}",
                            arguments = listOf(
                                navArgument("categoryId") {
                                    type = NavType.StringType
                                    nullable = true
                                },
                                navArgument("categoryTitle") {
                                    type = NavType.StringType
                                    nullable = true
                                }
                            )
                        ) { entry ->
                            // Expanded: the audio chosen beside the list, kept in the entry so it
                            // survives rotation and resizing (narrowed to one pane, it stays open
                            // in the player here, see AudioListScreen).
                            val handle = entry.savedStateHandle
                            val selectedUrl by handle.getStateFlow<String?>(SELECTED_AUDIO_URL, null).collectAsState()
                            val selectedTitle by handle.getStateFlow(SELECTED_AUDIO_TITLE, "").collectAsState()
                            AudioListScreen(
                                onNavigateToAudioDetail = { title, audioUrl ->
                                    val encodedUrl = Uri.encode(audioUrl)
                                    val encodedTitle = Uri.encode(title)
                                    navController.navigate("audio_detail_screen/$encodedTitle/$encodedUrl")
                                },
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToShare = shareAudio,
                                selectedAudio = selectedUrl?.let { AudioSelection(selectedTitle, it) },
                                onSelectAudio = {
                                    handle[SELECTED_AUDIO_TITLE] = it.title
                                    handle[SELECTED_AUDIO_URL] = it.audioUrl
                                },
                                onClearSelection = { handle[SELECTED_AUDIO_URL] = null },
                            )
                        }

                        screen(
                            route = "audio_detail_screen/{title}/{audioUrl}",
                            arguments = listOf(navArgument("title") {
                                type = NavType.StringType
                            }, navArgument("audioUrl") {
                                type = NavType.StringType
                            })
                        ) { entry ->
                            val audioUrl = entry.arguments?.getString("audioUrl").orEmpty()
                            val title = entry.arguments?.getString("title").orEmpty()
                            // Expanded: the player sits beside the audio list (Figma has no player
                            // of its own there), with this audio selected. Another one chosen there
                            // is still the one shown when the window narrows to the player. Both
                            // layouts use this entry's own player ViewModel, so playback carries on.
                            val handle = entry.savedStateHandle
                            val selectedUrl by handle.getStateFlow(SELECTED_AUDIO_URL, audioUrl).collectAsState()
                            val selectedTitle by handle.getStateFlow(SELECTED_AUDIO_TITLE, title).collectAsState()
                            val selection = AudioSelection(selectedTitle, selectedUrl)
                            ProvideNavAnimatedScope(this) {
                            if (layoutTokens.isExpanded) {
                                AudioListScreen(
                                    onNavigateToAudioDetail = { _, _ -> },
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToShare = shareAudio,
                                    selectedAudio = selection,
                                    onSelectAudio = {
                                        handle[SELECTED_AUDIO_TITLE] = it.title
                                        handle[SELECTED_AUDIO_URL] = it.audioUrl
                                    },
                                    playerViewModelKey = null,
                                )
                            } else {
                                AudioDetailScreen(
                                    onNavigateUp = {
                                        navController.popBackStack()
                                    },
                                    onNavigateToShare = shareAudio,
                                    modifier = Modifier.sharedContainer(SharedKeys.audio(audioUrl)),
                                    audio = selection,
                                )
                            }
                            }
                        }

                        screen(
                            route = "${Routes.SHARE_PREVIEW_SCREEN}/{audioUrl}?title={title}&category={category}&localFilePath={localFilePath}&startMs={startMs}&totalDurationMs={totalDurationMs}",
                            arguments = listOf(
                                navArgument("audioUrl") { type = NavType.StringType },
                                navArgument("title") { type = NavType.StringType; nullable = true },
                                navArgument("category") { type = NavType.StringType; nullable = true },
                                navArgument("localFilePath") { type = NavType.StringType; nullable = true },
                                navArgument("startMs") { type = NavType.LongType; defaultValue = 0L },
                                navArgument("totalDurationMs") { type = NavType.LongType; defaultValue = 0L },
                            )
                        ) {
                            SharePreviewScreen(
                                onNavigateUp = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        // Medium and Expanded: the same preview as a dialog over the player.
                        dialog(
                            route = "$SHARE_PREVIEW_DIALOG/{audioUrl}?title={title}&category={category}&localFilePath={localFilePath}&startMs={startMs}&totalDurationMs={totalDurationMs}",
                            arguments = listOf(
                                navArgument("audioUrl") { type = NavType.StringType },
                                navArgument("title") { type = NavType.StringType; nullable = true },
                                navArgument("category") { type = NavType.StringType; nullable = true },
                                navArgument("localFilePath") { type = NavType.StringType; nullable = true },
                                navArgument("startMs") { type = NavType.LongType; defaultValue = 0L },
                                navArgument("totalDurationMs") { type = NavType.LongType; defaultValue = 0L },
                            ),
                            dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
                        ) {
                            SharePreviewScreen(
                                onNavigateUp = { navController.popBackStack() },
                                asDialog = true,
                            )
                        }

                        screen(
                            route = "${Routes.STUDY_SCREEN}?data={data}",
                            arguments = listOf(
                                navArgument("data") {
                                    type = NavType.StringType
                                    nullable = true
                                },
                                navArgument("t") {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                }
                            ),
                        ) {

                            StudyScreen(
                                userEmail = userEmail,
                                idToken = idToken,
                                onLevelClick = { levelId ->
                                    navController.navigate("${Routes.PLAYLIST_SCREEN}/$levelId")
                                },
                                onNavigateToLogin = { },
                                onQuizClick = { quizId ->
                                    navController.navigate("${Routes.QUIZ_SCREEN}/$quizId")
                                }
                            )
                        }

                        screen(

                            route = "${Routes.PLAYLIST_SCREEN}/{levelId}",
                            arguments = listOf(navArgument("levelId") {
                                type = NavType.StringType
                            })

                        ) {
                            PlaylistScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onPlaylistClick = { playlistId ->
                                    navController.navigate("${Routes.LESSONS_SCREEN}/$playlistId")
                                }
                            )
                        }
                        screen(

                            route = "${Routes.QUIZ_SCREEN}/{quizId}",
                            arguments = listOf(navArgument("quizId") {
                                type = NavType.StringType
                            })

                        ) {
                            AnswerQuizScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        screen(
                            route = "${Routes.LESSONS_SCREEN}/{playlistId}",
                            arguments = listOf(navArgument("playlistId") {
                                type = NavType.StringType
                            })

                        ) {

                            LessonsListScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onLessonClick = { lessonId ->
                                    navController.navigate("${Routes.LESSON_DETAIL_SCREEN}/$lessonId")
                                }
                            )

                        }

                        screen(
                            route = "${Routes.LESSON_DETAIL_SCREEN}/{lessonId}",
                            arguments = listOf(navArgument("lessonId") {
                                type = NavType.StringType
                            })

                        ) {

                            LessonDetailScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )

                        }

                        screen(Routes.IMAGES_SCREEN, mediumMargin = AdaptivePanesDefaults.GridScreenMediumMargin) {
                            ProvideNavAnimatedScope(this) {
                            ImagesGroupsScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onGroupClick = { groupId ->
                                    navController.navigate("${Routes.IMAGE_DETAIL_SCREEN}/$groupId")
                                },
                                onImageClick = { groupId, index ->
                                    navController.navigate("${Routes.IMAGE_DETAIL_SCREEN}/$groupId?startIndex=$index")
                                }
                            )
                            }
                        }
                        composable(
                            route = "${Routes.IMAGE_DETAIL_SCREEN}/{groupId}?startIndex={startIndex}",
                            arguments = listOf(
                                navArgument("groupId") {
                                    type = NavType.StringType
                                },
                                navArgument("startIndex") {
                                    type = NavType.IntType
                                    defaultValue = 0
                                }
                            )
                        ) { entry ->
                            val groupId = entry.arguments?.getString("groupId").orEmpty()
                            ProvideNavAnimatedScope(this) {
                            ImageScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                modifier = Modifier.sharedContainer(SharedKeys.design(groupId), RectangleShape),
                            )
                            }

                        }
                        screen(Routes.ABOUT_DR_HASSAN_SCREEN) {
                            AboutDrHassanScreen {
                                navController.popBackStack()
                            }
                        }
                        screen(Routes.VIDEO_CATEGORY_SCREEN) {
                            VideoCategoryScreen(
                                onCategoryClick = { categoryId, categoryTitle ->
                                    val actualId = if (categoryId == ALL_VIDEO_CATEGORIES_ID) null else categoryId
                                    navController.navigate("${Routes.VIDEOS_SCREEN}?categoryId=$actualId&categoryTitle=${Uri.encode(categoryTitle)}")
                                },
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                        screen(
                            route = "${Routes.VIDEOS_SCREEN}?categoryId={categoryId}&categoryTitle={categoryTitle}",
                            arguments = listOf(
                                navArgument("categoryId") {
                                    type = NavType.StringType
                                    nullable = true
                                },
                                navArgument("categoryTitle") {
                                    type = NavType.StringType
                                    nullable = true
                                }
                            ),
                            mediumMargin = AdaptivePanesDefaults.GridScreenMediumMargin,
                        ) {

                            VideosScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }, onNavigateToVideo = { videoUrl, videoTitle ->
                                    val encodedUrl = Uri.encode(videoUrl)
                                    val encodedTitle = Uri.encode(videoTitle)
                                    navController.navigate("${Routes.VIDEO_PLAYER_SCREEN}/$encodedUrl/$encodedTitle")
                                }
                            )
                        }
                        screen(

                            route = "${Routes.VIDEO_PLAYER_SCREEN}/{videoUrl}/{videoTitle}",
                            arguments = listOf(
                                navArgument("videoUrl") {
                                    type = NavType.StringType
                                },
                                navArgument("videoTitle") {
                                    type = NavType.StringType
                                }),


                            ) {
                            val videoUrl = it.arguments?.getString("videoUrl")
                            val videoTitle = it.arguments?.getString("videoTitle")
                            if (videoUrl != null) {
                                VideoPlayerScreen(
                                    videoUrl = videoUrl, onNavigateBack = {
                                        navController.popBackStack()
                                    },
                                    videoTitle = videoTitle
                                )
                            }

                        }
                        screen(Routes.NOTIFICATIONS_SCREEN) {
                            NotificationsScreen(onBack = { navController.popBackStack() })
                        }
                        screen(Routes.Q_A_SCREEN) {
                            app.netlify.devalihassan.ui.q_a.QAScreen(
                                onNavigateBack = { navController.popBackStack() },
                                // The Search route takes no preset filter, so this opens plain Search.
                                onOpenSearch = { navController.navigate(Routes.SEARCH_SCREEN) },
                            )
                        }

                        // profile screens
                        screen(Routes.PROFILE_SCREEN) {
                            ProfileScreen(
                                onNavigate = { route ->
                                    when (route) {
                                        ProfileRoute.About -> navController.navigate(ProfileDestinations.ABOUT)
                                        ProfileRoute.Share -> navController.navigate(ProfileDestinations.SHARE)
                                        ProfileRoute.Privacy -> navController.navigate(ProfileDestinations.PRIVACY)
                                        ProfileRoute.Terms -> navController.navigate(ProfileDestinations.TERMS)
                                        ProfileRoute.Licenses -> navController.navigate(ProfileDestinations.LICENSES)
                                        ProfileRoute.Support -> navController.navigate(ProfileDestinations.SUPPORT)
                                    }
                                },
                                onThemeChanged = { isDarkTheme ->
                                    Log.d(TAG, "MainAppContent: isDarkTheme: $isDarkTheme")
                                    mainActivityViewModel.updateDarkThemePreference(isDarkTheme)
                                },
                                isDarkTheme = isDarkThemeEnabled,
                                followSystemTheme = followSystemTheme,
                                onFollowSystemThemeChanged = { mainActivityViewModel.updateFollowSystemTheme(it) },
                                onLogout = {
                                    onLogout()
                                }
                            )
                        }

                        screen(ProfileDestinations.ABOUT) {
                            AboutAppScreen(
                                onBack = { navController.popBackStack() },
                                onContact = { navController.navigate(ProfileDestinations.SUPPORT) },
                            )
                        }

                        screen(ProfileDestinations.SHARE) {
                            ShareAppScreen(
                                "app.netlify.devalihassan",
                                onBack = { navController.popBackStack() },

                                )
                        }


                        screen(ProfileDestinations.PRIVACY) {
                            LegalTextScreen(
                                title = "سياسة الخصوصية",
                                assetFileName = "privacy.md",
                                contactLabel = "للأسئلة عن خصوصيتك: تواصل معنا",
                                onBack = { navController.popBackStack() }
                            )
                        }

                        screen(ProfileDestinations.TERMS) {
                            LegalTextScreen(
                                title = "الشروط والأحكام",
                                assetFileName = "terms.md",
                                contactLabel = "للأسئلة عن الشروط: تواصل معنا",
                                onBack = { navController.popBackStack() }
                            )
                        }

                        screen(ProfileDestinations.LICENSES) {
                            LegalTextScreen(
                                title = "التراخيص والمصادر المفتوحة",
                                assetFileName = "licenses.md",
                                contactLabel = "للأسئلة عن التراخيص: تواصل معنا",
                                onBack = { navController.popBackStack() }
                            )
                        }

                        screen(ProfileDestinations.SUPPORT) {
                            SupportScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }


                    }
                }
            }
            }
        }
    }

    /**
     * A search result (an article or an audio) in the detail pane beside the results
     * ([onBack] null), or on its own once the window has narrowed to one pane. Both use the
     * search entry's one reader / player ViewModel.
     */
    @Composable
    @UnstableApi
    private fun SearchResultDetail(
        result: SearchResultMetaData,
        onBack: (() -> Unit)?,
        onShareArticle: (String) -> Unit,
        onShareAudio: AudioShareAction,
    ) {
        when (result.type) {
            "audio" -> AudioPlayerPane(
                title = result.title.orEmpty(),
                audioUrl = result.url.orEmpty(),
                onNavigateToShare = onShareAudio,
                onNavigateUp = onBack,
            )
            else -> if (onBack == null) {
                ArticleReaderPane(articleId = result.objectID, onShare = onShareArticle)
            } else {
                ArticleDetailScreen(
                    viewModel = hiltViewModel(key = ARTICLE_READER_PANE_KEY),
                    onNavigateBack = onBack,
                    onNavigateToShareSelection = onShareArticle,
                    articleId = result.objectID,
                )
            }
        }
    }

    private companion object {
        /** Debug builds: forces reduced motion for this run (see onCreate). */
        const val EXTRA_FORCE_REDUCED_MOTION = "force_reduced_motion"

        /** The article chosen beside the list on a tablet (an entry's saved state). */
        const val SELECTED_ARTICLE = "selectedArticleId"

        /** The clip share preview as a dialog (Medium and Expanded). */
        const val SHARE_PREVIEW_DIALOG = "share_preview_dialog"

        /** The audio chosen beside the list on a tablet (an entry's saved state). */
        const val SELECTED_AUDIO_URL = "selectedAudioUrl"
        const val SELECTED_AUDIO_TITLE = "selectedAudioTitle"

        /** The search result open beside the results on a tablet (an entry's saved state). */
        const val SELECTED_RESULT_ID = "selectedResultId"
        const val SELECTED_RESULT_TYPE = "selectedResultType"
        const val SELECTED_RESULT_TITLE = "selectedResultTitle"
        const val SELECTED_RESULT_URL = "selectedResultUrl"

        /** Search results shown in the detail pane; videos and designs open their own screens. */
        val RESULT_TYPES_IN_PANE = setOf("article", "audio")
    }
}

/**
 * A main-app destination: on Medium and Expanded windows the screen sits inside the window
 * margin (grid/margin, 24 or 32) beside the rail; on Compact it fills the space as before.
 * The immersive designs viewer is a plain `composable`: it draws to the window's edges.
 */
private fun NavGraphBuilder.screen(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    mediumMargin: Dp? = null,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composable(route = route, arguments = arguments) { entry ->
    val scope = this
    val margin = mediumMargin.takeIf { layoutTokens.windowClass == WindowClass.Medium }
    WindowMargin(margin = margin) { scope.content(entry) }
}
