package app.netlify.devalihassan.tablet

import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.components.ContentCategories
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.feature.video.presentation.list.VideosScreenContent
import com.example.feature.video.presentation.list.videosPreviewPagingData
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/** Videos (Figma `60:1403` / `60:1809` Expanded, `64:3744` / `64:3936` Medium). */
@RunWith(AndroidJUnit4::class)
class VideosTabletTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setVideos(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        rule.setShellContent(
            window, tokens, darkTheme = darkTheme,
            mediumMargin = AdaptivePanesDefaults.GridScreenMediumMargin,
        ) {
            val videos = remember { videosPreviewPagingData() }.collectAsLazyPagingItems()
            VideosScreenContent(
                videos = videos,
                onNavigateBack = {},
                onNavigateToVideo = { _, _ -> },
                categories = ContentCategories.all,
            )
        }
    }

    /** The cards of the first grid row (every card whose top is the first card's). */
    private fun firstRow(): List<DpRect> {
        val cards = rule.allBoundsOfText(VIDEO_TITLE) + rule.allBoundsOfText(OTHER_TITLE)
        val top = cards.minOf { it.top.value }
        return cards.filter { abs(it.top.value - top) < 1f }
    }

    @Test
    fun expanded_threeAcross_centred() {
        setVideos(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = false)
        rule.saveShellShot("videos-expanded-light")
        // Content 32..1168 (1136): three 328 cards + 2 × 24 = 1032, centred: 84..1116.
        val row = firstRow()
        assertEquals("three across", 3, row.size)
        row.forEach { assertDp(328.dp, it.widthDp, "cards keep their designed width") }
        assertDp(1116.dp, row.maxOf { it.right.value }.dp, "grid centred: (1136 - 1032) / 2 from the content's edge")
        assertDp(84.dp, row.minOf { it.left.value }.dp, "grid centred")
        assertDp(1168.dp - 16.dp, rule.boundsOfText("الكل").right, "pills start 16 in")
    }

    @Test
    fun expanded_dark() {
        setVideos(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = true)
        rule.saveShellShot("videos-expanded-dark")
    }

    @Test
    fun medium_twoAcross_20Margin() {
        setVideos(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("videos-medium-light")
        // Content 20..700 (680): two 328 cards + 24 fill it exactly.
        val row = firstRow()
        assertEquals("two across", 2, row.size)
        row.forEach { assertDp(328.dp, it.widthDp, "cards keep their designed width") }
        assertDp(700.dp, row.maxOf { it.right.value }.dp, "the 20 Medium margin of the grid screens")
        assertDp(20.dp, row.minOf { it.left.value }.dp, "the 20 Medium margin of the grid screens")
    }

    @Test
    fun medium_dark() {
        setVideos(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true)
        rule.saveShellShot("videos-medium-dark")
    }

    @Test
    fun compact_isThePhoneList() {
        setVideos(CompactWindow, AdaptiveLayoutTokens.Compact, darkTheme = false)
        rule.saveShellShot("videos-compact-light")
        assertDp(328.dp, firstRow().single().widthDp, "the phone list: 360 - 2 × 16")
        assertEquals("no pills on the phone", 0, rule.onAllNodesWithText("الكل").fetchSemanticsNodes().size)
    }

    private companion object {
        const val VIDEO_TITLE = "ما تيسر من سورة يس"
        const val OTHER_TITLE = "هل من يموت في المظاهرات شهيد؟"
    }
}
