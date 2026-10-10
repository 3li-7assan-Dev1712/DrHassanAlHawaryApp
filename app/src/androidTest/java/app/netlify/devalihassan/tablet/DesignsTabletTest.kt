package app.netlify.devalihassan.tablet

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.domain.text.DesignTitle
import com.example.feature.image.presentation.list.DesignsPreviewData
import com.example.feature.image.presentation.list.DesignsScreenContent
import com.example.feature.image.presentation.list.designsPreviewPagingData
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/** Designs (Figma `60:1597` / `60:1960` Expanded, `64:4089` / `64:4306` Medium). */
@RunWith(AndroidJUnit4::class)
class DesignsTabletTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setDesigns(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        rule.setShellContent(
            window, tokens, darkTheme = darkTheme,
            mediumMargin = AdaptivePanesDefaults.GridScreenMediumMargin,
        ) {
            val groups = remember { designsPreviewPagingData() }.collectAsLazyPagingItems()
            DesignsScreenContent(groups = groups, imageCount = { 1 }, onGroupClick = {}, onNavigateBack = {})
        }
    }

    /** The tiles of the first row. */
    private fun firstRow(): List<DpRect> {
        val tiles = DesignsPreviewData.flatMap { rule.allBoundsOfText(DesignTitle.clean(it.title)) }
        val top = tiles.minOf { it.top.value }
        return tiles.filter { abs(it.top.value - top) < 1f }
    }

    @Test
    fun expanded_sixAcross_centred() {
        setDesigns(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = false)
        rule.saveShellShot("designs-expanded-light")
        // Content 32..1168 (1136): six 156 tiles + 5 × 24 = 1056, centred: 72..1128.
        val row = firstRow()
        assertEquals("six across", 6, row.size)
        row.forEach { assertDp(156.dp, it.widthDp, "tiles keep their designed width") }
        assertDp(1128.dp, row.maxOf { it.right.value }.dp, "grid centred")
        assertDp(72.dp, row.minOf { it.left.value }.dp, "grid centred")
    }

    @Test
    fun expanded_dark() {
        setDesigns(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = true)
        rule.saveShellShot("designs-expanded-dark")
    }

    @Test
    fun medium_fourAcross() {
        setDesigns(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("designs-medium-light")
        // Content 20..700 (680): four 156 tiles + 3 × 16 = 672, centred: 24..696.
        val row = firstRow()
        assertEquals("four across", 4, row.size)
        row.forEach { assertDp(156.dp, it.widthDp, "tiles keep their designed width") }
        assertDp(696.dp, row.maxOf { it.right.value }.dp, "grid centred")
        assertDp(24.dp, row.minOf { it.left.value }.dp, "grid centred")
    }

    @Test
    fun medium_dark() {
        setDesigns(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true)
        rule.saveShellShot("designs-medium-dark")
    }

    @Test
    fun compact_isThePhoneGrid() {
        setDesigns(CompactWindow, AdaptiveLayoutTokens.Compact, darkTheme = false)
        rule.saveShellShot("designs-compact-light")
        val row = firstRow()
        assertEquals("two across", 2, row.size)
        row.forEach { assertDp(158.dp, it.widthDp, "(360 - 2 × 16 - 12) / 2") }
    }
}
