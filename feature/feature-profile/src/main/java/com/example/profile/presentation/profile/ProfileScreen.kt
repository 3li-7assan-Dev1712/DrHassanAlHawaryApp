package com.example.profile.presentation.profile

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.example.core.ui.R
import com.example.core.ui.animation.LoadingScreen
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.components.PreviewTab
import androidx.compose.ui.tooling.preview.Preview
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.Motion
import com.example.core.ui.theme.reducedMotion
import com.example.core.ui.theme.stateChangeSpec
import com.example.core.ui.theme.layoutTokens
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.BidiText
import com.example.profile.presentation.components.ProfileRoute

/** The three appearance choices; [SYSTEM] follows the phone's dark mode. */
enum class ThemeChoice { SYSTEM, LIGHT, DARK }

@Composable
fun ProfileScreen(
    isAdmin: Boolean = false,
    onNavigate: (ProfileRoute) -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    /** True when "تلقائي" is on. */
    followSystemTheme: Boolean = false,
    onFollowSystemThemeChanged: (Boolean) -> Unit = {},
    viewModel: ProfileScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val readerFontStep by viewModel.readerFontStep.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val colors = Brand.colors

    LaunchedEffect(state.signOutResult) {
        val result = state.signOutResult ?: return@LaunchedEffect
        if (result.success) onLogout()
        viewModel.onSignOutResultConsumed()
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = colors.surface,
            title = { Text("حذف الحساب؟", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
            text = {
                Text(
                    "سيُحذف حسابك وجميع بياناتك وسجل دراستك نهائيًا، ولا يمكن التراجع عن ذلك.",
                    textAlign = TextAlign.Start,
                    color = colors.textSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.danger)
                ) {
                    Text("حذف الحساب", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("إلغاء", color = colors.textPrimary)
                }
            }
        )
    }

    val userName = state.userData?.username ?: "زائر التطبيق"
    val userEmail = state.userData?.email ?: ""
    val profileUrl = state.userData?.userProfilePictureUrl.orEmpty()
    val themeChoice = when {
        followSystemTheme -> ThemeChoice.SYSTEM
        isDarkTheme -> ThemeChoice.DARK
        else -> ThemeChoice.LIGHT
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ProfileContent(
            userName = userName,
            userEmail = userEmail,
            photoUrl = profileUrl,
            isAdmin = isAdmin,
            themeChoice = themeChoice,
            onThemeSelect = { choice ->
                when (choice) {
                    ThemeChoice.SYSTEM -> onFollowSystemThemeChanged(true)
                    ThemeChoice.LIGHT -> {
                        onFollowSystemThemeChanged(false)
                        onThemeChanged(false)
                    }
                    ThemeChoice.DARK -> {
                        onFollowSystemThemeChanged(false)
                        onThemeChanged(true)
                    }
                }
            },
            readerFontStep = readerFontStep,
            onReaderFontStepChange = viewModel::setReaderFontStep,
            appVersion = state.currentAppVersion,
            onNavigate = onNavigate,
            onRateApp = { openStoreListing(context) },
            onSignOut = { viewModel.signOut() },
            onDeleteAccount = { showDeleteConfirmation = true },
        )

        if (state.isDeleting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                LoadingScreen()
            }
        }
    }
}

/**
 * The account screen without its ViewModel (previews and UI tests use it too). Compact and
 * Medium: the phone's single column. Expanded: the top bar over two equal columns 24 apart,
 * sections 20 apart (Figma `59:1054`): the account card, المظهر, الإعدادات and التطبيق at the
 * start; الدعم والسياسات, الحساب and the version at the end.
 */
@Composable
fun ProfileContent(
    userName: String,
    userEmail: String,
    photoUrl: String,
    isAdmin: Boolean,
    themeChoice: ThemeChoice,
    onThemeSelect: (ThemeChoice) -> Unit,
    readerFontStep: Int,
    onReaderFontStepChange: (Int) -> Unit,
    appVersion: String,
    onNavigate: (ProfileRoute) -> Unit,
    onRateApp: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val colors = Brand.colors
    val header = @Composable { ProfileHeader(name = userName, email = userEmail, photoUrl = photoUrl) }
    val appearance = @Composable {
        ProfileSection(title = stringResource(R.string.appearance)) {
            ThemeSegmentedControl(selected = themeChoice, onSelect = onThemeSelect)
        }
    }
    val settings = @Composable {
        ProfileSection(title = stringResource(R.string.settings)) {
            FontSizeRow(step = readerFontStep, onStepChange = onReaderFontStepChange)
        }
    }
    val appSection = @Composable {
        ProfileSection(title = "التطبيق") {
            ProfileRow(TablerIcons.InfoCircle, "عن التطبيق") { onNavigate(ProfileRoute.About) }
            ProfileRow(TablerIcons.Share, "مشاركة التطبيق") { onNavigate(ProfileRoute.Share) }
            ProfileRow(TablerIcons.Star, "تقييم التطبيق", isLast = true, onClick = onRateApp)
        }
    }
    val support = @Composable {
        ProfileSection(title = "الدعم والسياسات") {
            ProfileRow(TablerIcons.Headset, "الدعم والتواصل") { onNavigate(ProfileRoute.Support) }
            ProfileRow(TablerIcons.ShieldLock, "سياسة الخصوصية") { onNavigate(ProfileRoute.Privacy) }
            ProfileRow(TablerIcons.FileText, "الشروط والأحكام") { onNavigate(ProfileRoute.Terms) }
            ProfileRow(TablerIcons.Code, "التراخيص والمصادر", isLast = true) { onNavigate(ProfileRoute.Licenses) }
        }
    }
    val account = @Composable {
        ProfileSection(title = "الحساب") {
            ProfileRow(
                icon = TablerIcons.Logout,
                title = "تسجيل الخروج",
                isLast = isAdmin,
                onClick = onSignOut
            )
            if (!isAdmin) {
                ProfileRow(
                    icon = TablerIcons.Trash,
                    title = "حذف الحساب نهائيًا",
                    tint = colors.danger,
                    textColor = colors.danger,
                    isLast = true,
                    onClick = onDeleteAccount
                )
            }
        }
    }
    val version = @Composable {
        Text(
            text = "الإصدار $appVersion",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textMuted,
        )
    }

    Scaffold(
        containerColor = colors.background,
        topBar = { AppTopBar(title = stringResource(R.string.my_account)) }
    ) { padding ->
        if (layoutTokens.isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(top = ProfileExpandedTopGap),
                horizontalArrangement = Arrangement.spacedBy(ProfileExpandedColumnGap),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(ProfileSectionGap)) {
                    header()
                    if (!isAdmin) {
                        appearance()
                        settings()
                        appSection()
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(ProfileSectionGap)) {
                    if (!isAdmin) support()
                    account()
                    version()
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(ProfileSectionGap)
            ) {
                item { header() }
                if (!isAdmin) {
                    item { appearance() }
                    item { settings() }
                    item { appSection() }
                    item { support() }
                }
                item { account() }
                item { version() }
            }
        }
    }
}

/** Between sections (phone and tablet). */
private val ProfileSectionGap = 20.dp
/** Expanded (Figma `59:1054`): top bar to the columns, and between the columns. */
private val ProfileExpandedTopGap = 24.dp
private val ProfileExpandedColumnGap = 24.dp

/**
 * "تقييم التطبيق": the Play Store listing directly (market:// in the store app, else the
 * web listing). No in-app review API: it would be a new dependency.
 */
private fun openStoreListing(context: Context) {
    val id = context.packageName
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$id")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: ActivityNotFoundException) {
            // No store and no browser: nothing to open.
        }
    }
}

/** 48dp avatar with an accent ring, the name, and the email (LTR isolate, muted). */
@Composable
private fun ProfileHeader(name: String, email: String, photoUrl: String) {
    val colors = Brand.colors
    ProfileCard {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(2.dp, colors.accent, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(colors.accentContainer),
                contentAlignment = Alignment.Center,
            ) {
                SubcomposeAsyncImage(
                    model = photoUrl,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = null,
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(TablerIcons.User),
                                contentDescription = null,
                                tint = colors.onAccentContainer,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    },
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (email.isNotBlank()) {
                    Text(
                        text = BidiText.ltr(email),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Brand.colors.surface,
        border = BorderStroke(0.5.dp, Brand.colors.divider),
    ) {
        Column { content() }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Brand.colors.textMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        ProfileCard(content)
    }
}

@Composable
private fun ProfileRow(
    @DrawableRes icon: Int,
    title: String,
    tint: Color = Brand.colors.accent,
    textColor: Color = Brand.colors.textPrimary,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    val colors = Brand.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = textColor,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(TablerIcons.ChevronLeft),
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp),
        )
    }
    if (!isLast) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = colors.divider)
    }
}

/**
 * تلقائي / فاتح / داكن on a surfaceMuted track. One selection pill slides (and resizes)
 * to the chosen segment instead of each segment toggling its own background.
 */
@Composable
private fun ThemeSegmentedControl(selected: ThemeChoice, onSelect: (ThemeChoice) -> Unit) {
    val colors = Brand.colors
    val segments = listOf(
        Triple(ThemeChoice.SYSTEM, R.string.theme_system, TablerIcons.DeviceMobile),
        Triple(ThemeChoice.LIGHT, R.string.theme_light, TablerIcons.Sun),
        Triple(ThemeChoice.DARK, R.string.theme_dark, TablerIcons.Moon),
    )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceMuted)
            .padding(4.dp)
    ) {
        // Segments share the track equally; offset is from the START edge (RTL-aware).
        val segmentWidth = maxWidth / segments.size
        val selectedIndex = segments.indexOfFirst { it.first == selected }.coerceAtLeast(0)
        val pillOffset by animateDpAsState(segmentWidth * selectedIndex, stateChangeSpec(), label = "pillOffset")
        val pillWidth by animateDpAsState(segmentWidth, stateChangeSpec(), label = "pillWidth")
        Box(
            Modifier
                .matchParentSize()
                .wrapContentWidth(Alignment.Start)
                .offset(x = pillOffset)
                .width(pillWidth)
                .clip(RoundedCornerShape(9.dp))
                .background(colors.surface)
                .border(0.5.dp, colors.divider, RoundedCornerShape(9.dp))
        )
        Row(Modifier.fillMaxWidth()) {
            segments.forEach { (choice, label, icon) ->
                val isSelected = choice == selected
                val contentColor by animateColorAsState(
                    if (isSelected) colors.textPrimary else colors.textMuted, stateChangeSpec(), label = "segmentText",
                )
                val iconColor by animateColorAsState(
                    if (isSelected) colors.accent else colors.textMuted, stateChangeSpec(), label = "segmentIcon",
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .clickable { onSelect(choice) }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(icon), contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/** The article reader's text size (the same preference as the reader's A−/A+). */
@Composable
private fun FontSizeRow(step: Int, onStepChange: (Int) -> Unit) {
    val colors = Brand.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(TablerIcons.TextSize), contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            text = stringResource(R.string.reader_font_setting),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        OutlinedButton(
            onClick = { onStepChange(step - 1) },
            enabled = step > 0,
            contentPadding = PaddingValues(horizontal = 10.dp),
            border = BorderStroke(0.5.dp, colors.divider),
        ) { Text("A−", color = colors.textPrimary) }
        val reduced = reducedMotion
        AnimatedContent(
            targetState = step,
            transitionSpec = { Motion.contentSwap(reduced) },
            modifier = Modifier.padding(horizontal = 10.dp),
            label = "fontStep",
        ) { shownStep ->
            Text(text = ArabicNumerals.digits(shownStep + 1), color = colors.textMuted)
        }
        OutlinedButton(
            onClick = { onStepChange(step + 1) },
            enabled = step < ProfileScreenViewModel.MAX_READER_FONT_STEP,
            contentPadding = PaddingValues(horizontal = 10.dp),
            border = BorderStroke(0.5.dp, colors.divider),
        ) { Text("A+", color = colors.textPrimary) }
    }
}

/** The account screen with sample data (the Figma frames' placeholders): previews and UI tests. */
@Composable
fun ProfilePreviewContent() {
    ProfileContent(
        userName = "Ali Hassan",
        userEmail = "alihassan17122002@gmail.com",
        photoUrl = "",
        isAdmin = false,
        themeChoice = ThemeChoice.SYSTEM,
        onThemeSelect = {},
        readerFontStep = 1,
        onReaderFontStepChange = {},
        appVersion = "1.0.7",
        onNavigate = {},
        onRateApp = {},
        onSignOut = {},
        onDeleteAccount = {},
    )
}

@Composable
private fun ProfilePreview(darkTheme: Boolean) {
    AdaptiveShellPreview(darkTheme = darkTheme, selectedTab = PreviewTab.Profile) { ProfilePreviewContent() }
}

@Preview(name = "Profile - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun ProfileCompactLightPreview() = ProfilePreview(darkTheme = false)

@Preview(name = "Profile - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun ProfileCompactDarkPreview() = ProfilePreview(darkTheme = true)

@Preview(name = "Profile - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun ProfileMediumLightPreview() = ProfilePreview(darkTheme = false)

@Preview(name = "Profile - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun ProfileMediumDarkPreview() = ProfilePreview(darkTheme = true)

@Preview(name = "Profile - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun ProfileExpandedLightPreview() = ProfilePreview(darkTheme = false)

@Preview(name = "Profile - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun ProfileExpandedDarkPreview() = ProfilePreview(darkTheme = true)
