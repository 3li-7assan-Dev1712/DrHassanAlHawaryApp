package com.example.study.presentation.detail

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.core.player.PlaybackService
import com.example.core.ui.R
import com.example.core.ui.animation.LoadingScreen
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.Lesson
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.TimeUnit

@Composable
fun LessonDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: LessonDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val sessionToken = remember {
        SessionToken(context, ComponentName(context,  PlaybackService::class.java))
    }

    val controllerFuture: ListenableFuture<MediaController> = remember {
        MediaController.Builder(context, sessionToken).buildAsync()
    }

    LaunchedEffect(controllerFuture) {
        viewModel.mediaControllerFuture = controllerFuture
    }

    LaunchedEffect(Unit) {
        val serviceIntent = Intent(context, PlaybackService::class.java)
        context.startService(serviceIntent)
    }

    fun openPdf(url: String) {
        val intent = if (url.startsWith("http")) {
            // It's a remote URL, open in browser
            Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            // It's a local file path, use FileProvider
            val file = File(url)
            val authority = "${context.packageName}.provider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)

            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No application can handle this request.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    LessonDetailContent(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onPlayPauseClick = viewModel::onPlayPauseClick,
        onSeekForward = viewModel::onSeekForward,
        onSeekBackward = viewModel::onSeekBackward,
        onSeekBarPositionChanged = viewModel::onSeekBarPositionChanged,
        onOpenPdfClick = { uiState.lesson?.pdfUrl?.let { openPdf(it) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailContent(
    modifier: Modifier = Modifier,
    uiState: PlayerUiState,
    onNavigateBack: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekBarPositionChanged: (Long) -> Unit,
    onOpenPdfClick: () -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.lesson?.title ?: stringResource(R.string.downloading_msg),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenPdfClick) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = "Open PDF Summary",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingScreen(
                    )
                }
            } else if (uiState.lesson != null) {
                Spacer(modifier = Modifier.height(32.dp))

                // Lesson Art Section
                LessonTitleSection(uiState)

                Spacer(modifier = Modifier.height(56.dp))

                // Player UI
                PlayerControls(
                    isPlaying = uiState.isPlaying,
                    isBuffering = uiState.isBuffering,
                    currentPosition = uiState.currentPosition,
                    totalDuration = uiState.totalDuration,
                    onPlayPauseClick = onPlayPauseClick,
                    onSeekForward = onSeekForward,
                    onSeekBackward = onSeekBackward,
                    onSeekBarPositionChanged = onSeekBarPositionChanged,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))
                
            }
        }
    }
}

@Composable
private fun LessonTitleSection(
    uiState: PlayerUiState,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Highly Polished Image Container
        Surface(
            modifier = Modifier
                .size(280.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = R.drawable.institute_logo),
                    contentDescription = "Lesson Art",
                    contentScale = ContentScale.Crop,
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = uiState.lesson?.title ?: "",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
                letterSpacing = 0.25.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPosition: Long,
    totalDuration: Long,
    onPlayPauseClick: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekBarPositionChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isUserSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(currentPosition.toFloat()) }

    // Sync with player ONLY when not dragging
    LaunchedEffect(currentPosition) {
        if (!isUserSeeking) {
            sliderPosition = currentPosition.toFloat()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- Seek Bar ---
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = sliderPosition,
                valueRange = 0f..(totalDuration.toFloat().coerceAtLeast(1f)),
                onValueChange = {
                    isUserSeeking = true
                    sliderPosition = it
                },
                onValueChangeFinished = {
                    isUserSeeking = false
                    onSeekBarPositionChanged(sliderPosition.toLong())
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                enabled = totalDuration > 0 && !isBuffering,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )
            if (isBuffering && totalDuration > 0) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(sliderPosition.toLong()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDuration(totalDuration),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Main Player Action Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlIconButton(
                onClick = onSeekBackward,
                enabled = !isBuffering,
                painter = painterResource(id = R.drawable.round_backword_icon),
                contentDescription = "Rewind 10 seconds",
                iconSize = 36.dp
            )

            // Play/Pause with standard fade transition
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp)) {
                if (isBuffering && totalDuration == 0L) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(72.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    AnimatedContent(
                        targetState = isPlaying,
                        label = "PlayPause",
                        transitionSpec = {
                            fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                        }
                    ) { playing ->
                        MainPlayButton(
                            isPlaying = playing,
                            onClick = onPlayPauseClick,
                            enabled = !isBuffering && totalDuration > 0
                        )
                    }
                }
            }

            ControlIconButton(
                onClick = onSeekForward,
                enabled = !isBuffering,
                painter = painterResource(id = R.drawable.round_forward_icon),
                contentDescription = "Forward 10 seconds",
                iconSize = 36.dp
            )
        }
    }
}

@Composable
fun MainPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(90.dp)
            .shadow(20.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        enabled = enabled,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(
                    id = if (isPlaying) R.drawable.round_pause_icon else R.drawable.round_play_icon
                ),
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ControlIconButton(
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(iconSize + 24.dp)
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = if (enabled) MaterialTheme.colorScheme.onSurface 
                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
        )
    }
}

private fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Preview(showBackground = true, showSystemUi = false, device = Devices.PIXEL_7, name = "تفاصيل الدرس العلمي", )
@Composable
fun LessonDetailPolishedPreview() {
    val dummyLesson = Lesson(
        id = "101",
        title = "شرح متن الآجرومية - الدرس الثالث",
        order = 3,
        audioUrl = "",
        pdfUrl = "summary.pdf",
        duration = "45:00"
    )

    HassanAlHawaryTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            LessonDetailContent(
                uiState = PlayerUiState(
                    lesson = dummyLesson,
                    isPlaying = false,
                    currentPosition = 300000L,
                    totalDuration = 2700000L,
                    isLoading = false
                ),
                onNavigateBack = {},
                onPlayPauseClick = {},
                onSeekForward = {},
                onSeekBackward = {},
                onSeekBarPositionChanged = {},
                onOpenPdfClick = {}
            )
        }
    }
}
