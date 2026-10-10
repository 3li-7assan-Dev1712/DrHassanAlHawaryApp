package com.example.core.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.HassanAlHawaryTheme
import com.example.core.ui.theme.layoutTokens

/**
 * The update screen (optional or required). The phone: everything centred. A tablet (Figma
 * Update, Medium and Expanded): a centred 520dp column with flexible space above and below
 * the message, so the actions stay at the bottom.
 */
@Composable
fun UpdateScreen(
    modifier: Modifier = Modifier,
    updateUrl: String,
    isForceUpdate: Boolean,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val onUpdate = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
        context.startActivity(intent)
    }

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        if (!layoutTokens.isCompact) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = AdaptivePanesDefaults.FirstRunColumnWidth)
                        .fillMaxHeight()
                        .padding(horizontal = AdaptivePanesDefaults.FirstRunPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.weight(1f))
                    UpdateMessage(isForceUpdate)
                    Spacer(Modifier.weight(1f))
                    UpdateActions(isForceUpdate, onUpdate, onDismiss)
                    Spacer(Modifier.height(24.dp))
                }
            }
            return@Surface
        }
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center
        ) {
            UpdateMessage(isForceUpdate)

            Spacer(modifier = Modifier.height(32.dp))

            UpdateActions(isForceUpdate, onUpdate, onDismiss)
        }
    }
}

@Composable
private fun ColumnScope.UpdateMessage(isForceUpdate: Boolean) {
    Icon(
        imageVector = Icons.Default.Update,
        contentDescription = null,
        modifier = Modifier.size(100.dp),
        tint = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = if (isForceUpdate) "تحديث مطلوب" else "تحديث جديد متاح",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = if (isForceUpdate)
            "يوجد إصدار جديد من التطبيق متاح حاليًا. يرجى تحديث التطبيق للاستمرار في استخدام المنصة."
        else
            "يوجد إصدار جديد من التطبيق متاح حاليًا مع ميزات وتحسينات جديدة. هل ترغب في التحديث الآن؟",
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ColumnScope.UpdateActions(isForceUpdate: Boolean, onUpdate: () -> Unit, onDismiss: () -> Unit) {
    Button(
        onClick = onUpdate,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "تحديث الآن")
    }

    if (!isForceUpdate) {
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "ليس الآن")
        }
    }
}

@Preview(name = "Update - compact, light", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun UpdateCompactPreview() {
    HassanAlHawaryTheme(darkTheme = false) { UpdateScreen(updateUrl = "", isForceUpdate = false) }
}

@Preview(name = "Update - compact, dark", locale = "ar", widthDp = 360, heightDp = 800)
@Composable
private fun UpdateCompactDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { UpdateScreen(updateUrl = "", isForceUpdate = true) }
}

@Preview(name = "Update - medium, light", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun UpdateMediumPreview() {
    HassanAlHawaryTheme(darkTheme = false) { UpdateScreen(updateUrl = "", isForceUpdate = false) }
}

@Preview(name = "Update - medium, dark", locale = "ar", device = "spec:width=800dp,height=1280dp,dpi=320")
@Composable
private fun UpdateMediumDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { UpdateScreen(updateUrl = "", isForceUpdate = true) }
}

@Preview(name = "Update - expanded, light", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun UpdateExpandedPreview() {
    HassanAlHawaryTheme(darkTheme = false) { UpdateScreen(updateUrl = "", isForceUpdate = false) }
}

@Preview(name = "Update - expanded, dark", locale = "ar", device = "spec:width=1280dp,height=800dp,dpi=320")
@Composable
private fun UpdateExpandedDarkPreview() {
    HassanAlHawaryTheme(darkTheme = true) { UpdateScreen(updateUrl = "", isForceUpdate = true) }
}
