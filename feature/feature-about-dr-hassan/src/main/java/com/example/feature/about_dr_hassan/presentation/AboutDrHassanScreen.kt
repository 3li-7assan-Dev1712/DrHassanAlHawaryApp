package com.example.feature.about_dr_hassan.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.components.SheikhPhoto
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.module.DoctorProfile
import com.example.domain.module.doctorProfileData
import com.example.domain.text.BidiText
import com.example.domain.text.DatedEntry

/** Official links. Only ones that already existed in the codebase or were given by the spec. */
private const val WEBSITE_URL = "https://www.dr-alhawary.com"
private const val TELEGRAM_URL = "https://t.me/Dr_alhawary"

@Composable
fun AboutDrHassanScreen(
    onNavigateBack: () -> Unit
) {
    AboutContent(doctor = doctorProfileData, onNavigateBack = onNavigateBack)
}

@Composable
private fun AboutContent(doctor: DoctorProfile, onNavigateBack: () -> Unit) {
    val colors = Brand.colors
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        R.string.about_tab_bio,
        R.string.about_tab_qualifications,
        R.string.about_tab_research,
        R.string.about_tab_contributions,
    )
    val education = remember(doctor) { doctor.education.map(DatedEntry::parse) }
    // Newest first; undated entries last.
    val research = remember(doctor) { doctor.researches.map(DatedEntry::parse).sortedByDescending { it.year ?: Int.MIN_VALUE } }
    val papers = remember(doctor) { doctor.papers.map(DatedEntry::parse).sortedByDescending { it.year ?: Int.MIN_VALUE } }

    Scaffold(
        containerColor = colors.background,
        topBar = { AppTopBar(title = stringResource(R.string.about_dr_hassan), onBack = onNavigateBack) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { HeroSection(roleLine = doctor.title) }

            item {
                ScrollableTabRow(
                    selectedTabIndex = tab,
                    edgePadding = 0.dp,
                    containerColor = colors.background,
                    contentColor = colors.textPrimary,
                    indicator = { positions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(positions[tab]),
                            color = colors.accentStrong,
                        )
                    },
                    divider = { HorizontalDivider(thickness = 0.5.dp, color = colors.divider) },
                ) {
                    tabs.forEachIndexed { index, label ->
                        Tab(
                            selected = tab == index,
                            onClick = { tab = index },
                            selectedContentColor = colors.textPrimary,
                            unselectedContentColor = colors.textMuted,
                            text = {
                                Text(
                                    stringResource(label),
                                    fontWeight = if (tab == index) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                )
                            },
                        )
                    }
                }
            }

            when (tab) {
                0 -> {
                    item { SectionCard { BodyText(doctor.bio) } }
                    item { BulletSection(stringResource(R.string.about_teachers), doctor.teachers) }
                    item { BulletSection(stringResource(R.string.about_positions), doctor.positions) }
                }
                1 -> item { SectionCard { Timeline(education) } }
                2 -> {
                    item { ResearchSection(stringResource(R.string.about_research_published), research) }
                    item { ResearchSection(stringResource(R.string.about_research_papers), papers) }
                }
                else -> {
                    item { BulletSection(stringResource(R.string.about_media_contributions), doctor.mediaResponsibilities) }
                    item { BulletSection(stringResource(R.string.about_teaching_contributions), doctor.studyingResponsibilities) }
                }
            }

            item { OfficialChannels() }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroSection(roleLine: String) {
    val colors = Brand.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val name = stringResource(R.string.sheikh_name)
        SheikhPhoto(size = 112.dp, ringWidth = 2.dp, ringColor = colors.accent, contentDescription = name)
        Spacer(Modifier.height(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        // "أستاذ مشارك بجامعة القرآن الكريم - عضو بمجمع الفقه الإسلامي" -> two chips.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            roleLine.split(" - ").map(String::trim).filter(String::isNotEmpty).forEach { Chip(it) }
        }
    }
}

@Composable
private fun Chip(text: String) {
    val colors = Brand.colors
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = colors.onAccentContainer,
        modifier = Modifier
            .background(colors.accentContainer, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Brand.colors.surface,
        border = BorderStroke(0.5.dp, Brand.colors.divider),
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 1.8.em),
        color = Brand.colors.textSecondary,
    )
}

/** An accentStrong dot and connecting line per entry, a year chip, title and institution. */
@Composable
private fun Timeline(entries: List<DatedEntry>) {
    val colors = Brand.colors
    entries.forEachIndexed { index, entry ->
        Row(Modifier.height(IntrinsicSize.Min)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(16.dp)
                    .fillMaxHeight(),
            ) {
                Box(
                    Modifier
                        .padding(top = 6.dp)
                        .size(10.dp)
                        .background(colors.accentStrong, CircleShape),
                )
                if (index < entries.lastIndex) {
                    Box(
                        Modifier
                            .width(2.dp)
                            .weight(1f)
                            .background(colors.accentStrong.copy(alpha = 0.35f)),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(
                Modifier
                    .weight(1f)
                    .padding(bottom = if (index < entries.lastIndex) 18.dp else 0.dp),
            ) {
                entry.dateLabel?.let {
                    Chip(it)
                    Spacer(Modifier.height(6.dp))
                }
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, lineHeight = 1.5.em),
                    color = colors.textPrimary,
                )
                entry.detail?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 1.6.em),
                        color = colors.textMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResearchSection(title: String, entries: List<DatedEntry>) {
    SectionCard {
        SectionTitle(title)
        entries.forEachIndexed { index, entry ->
            ResearchItem(entry)
            if (index < entries.lastIndex) {
                HorizontalDivider(Modifier.padding(vertical = 10.dp), thickness = 0.5.dp, color = Brand.colors.divider)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResearchItem(entry: DatedEntry) {
    val colors = Brand.colors
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        entry.dateLabel?.let { Chip(it) }
        if (entry.unpublished) Chip(stringResource(R.string.about_unpublished))
    }
    Spacer(Modifier.height(6.dp))
    Text(
        text = entry.title,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, lineHeight = 1.6.em),
        color = colors.textPrimary,
    )
    entry.detail?.let {
        Text(text = it, style = MaterialTheme.typography.bodySmall.copy(lineHeight = 1.6.em), color = colors.textMuted)
    }
}

@Composable
private fun BulletSection(title: String, items: List<String>) {
    val colors = Brand.colors
    SectionCard {
        SectionTitle(title)
        items.forEach { item ->
            Row(Modifier.padding(bottom = 8.dp)) {
                Box(
                    Modifier
                        .padding(top = 9.dp)
                        .size(5.dp)
                        .background(colors.accentStrong, CircleShape),
                )
                Spacer(Modifier.width(10.dp))
                Text(item, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 1.7.em), color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = Brand.colors.textPrimary,
    )
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun OfficialChannels() {
    val colors = Brand.colors
    val context = LocalContext.current
    var showQr by rememberSaveable { mutableStateOf(false) }
    fun open(url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
            // No browser/Telegram: nothing sensible to open.
        }
    }
    Column {
        Text(
            text = stringResource(R.string.about_official_channels),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.textMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = colors.surface,
            border = BorderStroke(0.5.dp, colors.divider),
        ) {
            Column {
                ChannelRow(TablerIcons.World, stringResource(R.string.about_website), "dr-alhawary.com") { open(WEBSITE_URL) }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = colors.divider)
                ChannelRow(
                    TablerIcons.BrandTelegram,
                    stringResource(R.string.about_telegram_channel),
                    "t.me/Dr_alhawary",
                    onQrClick = { showQr = true },
                ) { open(TELEGRAM_URL) }
            }
        }
    }

    if (showQr) TelegramQrSheet(onDismiss = { showQr = false })
}

/** The row opens [url]; when [onQrClick] is set, a divided QR button sits at the end. */
@Composable
private fun ChannelRow(
    @DrawableRes icon: Int,
    title: String,
    url: String,
    onQrClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val colors = Brand.colors
    Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = colors.textPrimary)
                Text(BidiText.ltr(url), style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
            }
            Icon(painterResource(TablerIcons.ExternalLink), contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
        }
        if (onQrClick != null) {
            VerticalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = colors.divider)
            IconButton(onClick = onQrClick, modifier = Modifier.padding(horizontal = 4.dp)) {
                Icon(
                    painterResource(TablerIcons.QrCode),
                    contentDescription = stringResource(R.string.about_telegram_qr),
                    tint = colors.accent,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TelegramQrSheet(onDismiss: () -> Unit) {
    val colors = Brand.colors
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.about_telegram_qr_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(16.dp))
            // White behind the code in both themes, so any scanner reads it.
            Box(
                Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(8.dp),
            ) {
                Image(
                    painterResource(R.drawable.qr_telegram_channel),
                    contentDescription = TELEGRAM_URL,
                    modifier = Modifier
                        .width(240.dp)
                        .aspectRatio(863f / 1000f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.about_telegram_qr_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(name = "About - light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun AboutLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) { AboutContent(doctorProfileData, onNavigateBack = {}) }
}

@Preview(name = "About - dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun AboutDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { AboutContent(doctorProfileData, onNavigateBack = {}) }
}
