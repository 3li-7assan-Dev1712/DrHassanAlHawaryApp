package com.example.profile.presentation.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.core.ui.R
import com.example.core.ui.components.AppTopBar
import com.example.core.ui.icons.TablerIcons
import com.example.core.ui.theme.Brand
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.domain.text.ArabicNumerals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader

private suspend fun readAsset(context: Context, fileName: String): String =
    withContext(Dispatchers.IO) {
        context.assets.open(fileName).bufferedReader().use(BufferedReader::readText)
    }

/** Privacy policy, terms and licenses: one of the Markdown files in app/src/main/assets. */
@Composable
fun LegalTextScreen(
    title: String,
    assetFileName: String,
    contactLabel: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var document by remember { mutableStateOf<LegalDocument?>(null) }

    LaunchedEffect(assetFileName) {
        document = parseLegalDocument(readAsset(context, assetFileName))
    }

    val email = stringResource(R.string.support_email_address)
    LegalContent(
        title = title,
        document = document,
        contactLabel = contactLabel,
        onContact = {
            try {
                context.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:$email".toUri()))
            } catch (_: ActivityNotFoundException) {
                // No mail app: nothing sensible to open.
            }
        },
        onBack = onBack,
    )
}

@Composable
private fun LegalContent(
    title: String,
    document: LegalDocument?,
    contactLabel: String,
    onContact: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = Brand.colors
    Scaffold(
        containerColor = colors.background,
        topBar = { AppTopBar(title = title, onBack = onBack) },
    ) { padding ->
        if (document == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.accent)
            }
            return@Scaffold
        }

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        // Items before the first section: header, contents card.
        val firstSectionIndex = 2

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    document.updated?.let {
                        Text(
                            text = ArabicNumerals.digits(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted,
                        )
                    }
                    Blocks(document.intro)
                }
            }

            item {
                if (document.sections.isNotEmpty()) {
                    ContentsCard(document.sections) { index ->
                        scope.launch { listState.animateScrollToItem(firstSectionIndex + index) }
                    }
                }
            }

            itemsIndexed(document.sections) { index, section ->
                Section(number = index + 1, section = section)
            }

            if (document.outro.isNotEmpty()) {
                item {
                    HorizontalDivider(thickness = 0.5.dp, color = colors.divider)
                    Spacer(Modifier.height(16.dp))
                    Blocks(document.outro, textAlign = TextAlign.Center)
                }
            }

            item { ContactRow(contactLabel, onContact) }
        }
    }
}

@Composable
private fun ContentsCard(sections: List<LegalSection>, onClick: (Int) -> Unit) {
    val colors = Brand.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(0.5.dp, colors.divider),
    ) {
        Column(Modifier.padding(vertical = 12.dp)) {
            Text(
                text = "المحتويات",
                style = MaterialTheme.typography.labelLarge,
                color = colors.textMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            sections.forEachIndexed { index, section ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick(index) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = ArabicNumerals.digits(index + 1),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                        modifier = Modifier.width(28.dp),
                    )
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        painterResource(TablerIcons.ChevronLeft),
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
                if (index < sections.lastIndex) {
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = colors.divider)
                }
            }
        }
    }
}

@Composable
private fun Section(number: Int, section: LegalSection) {
    val colors = Brand.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(22.dp)
                    .background(colors.accentStrong, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "${ArabicNumerals.digits(number)}. ${section.title}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
            )
        }
        Blocks(section.blocks)
    }
}

@Composable
private fun Blocks(blocks: List<LegalBlock>, textAlign: TextAlign? = null) {
    val colors = Brand.colors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        blocks.forEach { block ->
            when (block) {
                is LegalBlock.Paragraph -> Text(
                    text = inline(block.text, colors.textPrimary),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 1.8.em),
                    color = colors.textSecondary,
                    textAlign = textAlign,
                    modifier = Modifier.fillMaxWidth(),
                )

                is LegalBlock.Bullets -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    block.items.forEach { Bullet(it) }
                }

                is LegalBlock.Code -> CodeBlock(block.text)
            }
        }
    }
}

@Composable
private fun Bullet(text: String) {
    val colors = Brand.colors
    Row {
        Box(
            Modifier
                .padding(top = 9.dp)
                .size(5.dp)
                .background(colors.accentStrong, CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = inline(text, colors.textPrimary),
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 1.8.em),
            color = colors.textSecondary,
        )
    }
}

/** License texts are English: shown left-to-right in a mono card. */
@Composable
private fun CodeBlock(text: String) {
    val colors = Brand.colors
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = colors.surface,
            border = BorderStroke(0.5.dp, colors.divider),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 1.5.em),
                color = colors.textSecondary,
                modifier = Modifier.padding(14.dp),
            )
        }
    }
}

@Composable
private fun ContactRow(label: String, onClick: () -> Unit) {
    val colors = Brand.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(TablerIcons.Mail), contentDescription = null, tint = colors.accentStrong, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.accentStrong,
        )
    }
}

/** `**bold**` spans, drawn in [boldColor]; everything else plain. */
private fun inline(text: String, boldColor: Color): AnnotatedString = buildAnnotatedString {
    val parts = text.split("**")
    parts.forEachIndexed { index, part ->
        // Odd parts sit between a pair of ** markers; an unpaired trailing ** stays plain.
        if (index % 2 == 1 && index < parts.lastIndex) {
            val start = length
            append(part)
            addStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor), start, length)
        } else {
            append(part)
        }
    }
}

private val previewDocument = parseLegalDocument(
    """
    # سياسة الخصوصية

    آخر تحديث: 28 سبتمبر 2026

    نحرص في تطبيق الشيخ د. حسن الهواري على حماية بياناتك.

    ---

    ## أولًا: البيانات التي نجمعها

    * اسمك وبريدك الإلكتروني من حساب Google
    * بيانات تقنية عن الأعطال والأداء

    ---

    ## ثانيًا: كيف نستخدمها

    **لا يتم** بيع بياناتك.

    ---

    باستخدامك للتطبيق، فإنك توافق على هذه السياسة.
    """.trimIndent()
)

@Preview(name = "Legal - dark", locale = "ar", widthDp = 360, heightDp = 900)
@Composable
private fun LegalDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) {
        LegalContent("سياسة الخصوصية", previewDocument, "للأسئلة عن خصوصيتك: تواصل معنا", {}, {})
    }
}

@Preview(name = "Legal - light", locale = "ar", widthDp = 360, heightDp = 900)
@Composable
private fun LegalLightPreview() {
    HassanAlHawaryTheme(darkTheme = false) {
        LegalContent("سياسة الخصوصية", previewDocument, "للأسئلة عن خصوصيتك: تواصل معنا", {}, {})
    }
}
