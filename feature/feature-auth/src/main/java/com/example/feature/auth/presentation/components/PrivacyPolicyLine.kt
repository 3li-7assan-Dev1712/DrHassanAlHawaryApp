package com.example.feature.auth.presentation.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.config.AppLinks
import com.example.core.ui.theme.BrandTokens
import com.example.core.ui.theme.Cairo
import com.example.feature.auth.R

/**
 * "بالمتابعة فإنك توافق على سياسة الخصوصية", with the policy name as a real link
 * (LinkAnnotation, so TalkBack announces it as one) opening in a Custom Tab.
 */
@Composable
fun PrivacyPolicyLine(
    modifier: Modifier = Modifier,
    url: String = AppLinks.PRIVACY_POLICY_URL,
) {
    val context = LocalContext.current
    val policy = stringResource(R.string.auth_privacy_policy)
    val sentence = stringResource(R.string.auth_privacy_agreement, policy)
    val policyStart = sentence.indexOf(policy)

    val text = buildAnnotatedString {
        append(sentence.substring(0, policyStart))
        if (url.isNotBlank()) {
            val link = LinkAnnotation.Url(
                url = url,
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = BrandTokens.textSecondary,
                        textDecoration = TextDecoration.Underline,
                    ),
                ),
                linkInteractionListener = { context.openInCustomTab(url) },
            )
            withLink(link) { append(policy) }
        } else {
            append(policy)
        }
        append(sentence.substring(policyStart + policy.length))
    }

    Text(
        text = text,
        color = BrandTokens.textMuted,
        fontFamily = Cairo,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        // The line is the link's touch target; keep it at least 48dp tall.
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 12.dp),
    )
}

/** Custom Tab when a browser supports it, otherwise any app that can view the URL. */
private fun Context.openInCustomTab(url: String) {
    val uri = Uri.parse(url)
    try {
        CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(this, uri)
    } catch (e: ActivityNotFoundException) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: ActivityNotFoundException) {
            // No browser at all; nothing sensible to do.
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1512, widthDp = 360)
@Composable
private fun PrivacyPolicyLinePreview() {
    PrivacyPolicyLine(url = "https://example.com/privacy")
}
