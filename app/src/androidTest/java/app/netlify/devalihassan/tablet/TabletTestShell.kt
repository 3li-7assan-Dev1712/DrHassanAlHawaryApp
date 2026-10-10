package app.netlify.devalihassan.tablet

import android.graphics.Bitmap
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.LayoutDirection
import androidx.compose.ui.test.Locales
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.then
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.test.platform.app.InstrumentationRegistry
import com.example.core.ui.components.AdaptiveShellPreview
import com.example.core.ui.components.PreviewTab
import com.example.core.ui.theme.AdaptiveLayoutTokens
import java.io.File
import org.junit.Assert.assertEquals

const val SHELL_TAG = "testShell"

val ExpandedWindow = DpSize(1280.dp, 800.dp)
val MediumWindow = DpSize(800.dp, 1280.dp)
val CompactWindow = DpSize(360.dp, 800.dp)

/**
 * Lays [content] out in the app shell at a Figma reference size (Expanded 1280×800, Medium
 * 800×1280, Compact 360×800), right to left as the app always runs.
 */
fun ComposeContentTestRule.setShellContent(
    window: DpSize,
    tokens: AdaptiveLayoutTokens,
    darkTheme: Boolean = false,
    selectedTab: PreviewTab = PreviewTab.Home,
    showRail: Boolean = true,
    margin: Boolean = true,
    mediumMargin: Dp? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    setContent {
        DeviceConfigurationOverride(
            DeviceConfigurationOverride.ForcedSize(window) then
                DeviceConfigurationOverride.Locales(LocaleList("ar")) then
                DeviceConfigurationOverride.LayoutDirection(LayoutDirection.Rtl),
        ) {
            AdaptiveShellPreview(
                darkTheme = darkTheme,
                modifier = Modifier.testTag(SHELL_TAG),
                selectedTab = selectedTab,
                showRail = showRail,
                margin = margin,
                mediumMargin = mediumMargin,
                layoutTokens = tokens,
                content = content,
            )
        }
    }
}

/**
 * [actual] equals the Figma number [expected] within one pixel: a reference window larger
 * than the test device's is laid out at a scaled-down density (ForcedSize), where a pixel
 * is about 1dp and edges round to whole pixels.
 */
fun assertDp(expected: Dp, actual: Dp, what: String) {
    assertEquals(what, expected.value, actual.value, 1f)
}

/** Bounds of the node tagged [tag], relative to the shell's top-left corner. */
fun ComposeContentTestRule.boundsInShell(tag: String): DpRect =
    boundsInShell(onNodeWithTag(tag, useUnmergedTree = true))

/** Bounds of [node], relative to the shell's top-left corner. */
fun ComposeContentTestRule.boundsInShell(node: SemanticsNodeInteraction): DpRect {
    val shell = onNodeWithTag(SHELL_TAG).getUnclippedBoundsInRoot()
    val bounds = node.getUnclippedBoundsInRoot()
    return DpRect(bounds.left - shell.left, bounds.top - shell.top, bounds.right - shell.left, bounds.bottom - shell.top)
}

/** Bounds, relative to the shell, of the (merged) node showing [text]: a whole card for a card's title. */
fun ComposeContentTestRule.boundsOfText(text: String): DpRect = boundsInShell(onNodeWithText(text))

/** Bounds, relative to the shell, of every (merged) node showing [text], top to bottom. */
fun ComposeContentTestRule.allBoundsOfText(text: String): List<DpRect> {
    val shell = onNodeWithTag(SHELL_TAG).getUnclippedBoundsInRoot()
    return onAllNodesWithText(text).fetchSemanticsNodes().map { node ->
        val b = node.boundsInRoot
        with(node.layoutInfo.density) {
            DpRect(b.left.toDp() - shell.left, b.top.toDp() - shell.top, b.right.toDp() - shell.left, b.bottom.toDp() - shell.top)
        }
    }.sortedBy { it.top.value }
}

val DpRect.widthDp: Dp get() = right - left
val DpRect.heightDp: Dp get() = bottom - top

/**
 * Saves the shell as a PNG to the test app's external files (`tablet-shots/<name>.png`),
 * to be pulled with adb and compared with the Figma frame.
 */
fun ComposeContentTestRule.saveShellShot(name: String) {
    val bitmap = onNodeWithTag(SHELL_TAG).captureToImage().asAndroidBitmap()
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val dir = File(context.getExternalFilesDir(null), "tablet-shots").apply { mkdirs() }
    File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
}
