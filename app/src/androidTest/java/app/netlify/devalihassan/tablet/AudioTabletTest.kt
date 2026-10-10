package app.netlify.devalihassan.tablet

import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.ui.components.AdaptivePanesDefaults
import com.example.core.ui.components.ContentCategories
import com.example.core.ui.components.EmptyDetailDefaults
import com.example.core.ui.theme.AdaptiveLayoutTokens
import com.example.feature.audio.presentation.category.AudioCategoriesAdaptiveContent
import com.example.feature.audio.presentation.detail.AudioDetailScreen
import com.example.feature.audio.presentation.detail.PlayerPreviewState
import com.example.feature.audio.presentation.list.AudioListAdaptiveContent
import com.example.feature.audio.presentation.list.AudioPreviewSelection
import com.example.feature.audio.presentation.list.AudiosPreviewData
import com.example.feature.audio.presentation.list.PlayerPanePreviewContent
import com.example.feature.audio.presentation.list.audiosPreviewPagingData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Audio categories (Figma `57:498` / `57:870`, Medium `63:3174` / `63:3310`), Fatwas with the
 * player (Expanded `57:646` / `57:1015`; Medium Fatwas `63:3447` / `63:3556`, Medium player
 * `63:3658` / `63:3719`).
 */
@RunWith(AndroidJUnit4::class)
class AudioTabletTest {

    @get:Rule
    val rule = createComposeRule()

    private val seekBar = SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)

    private fun setCategories(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        rule.setShellContent(window, tokens, darkTheme = darkTheme) {
            AudioCategoriesAdaptiveContent(
                isLoading = false,
                error = null,
                categories = ContentCategories.all,
                onBack = {},
                onClick = {},
            )
        }
    }

    private fun setFatwas(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        rule.setShellContent(window, tokens, darkTheme = darkTheme) {
            val audios = remember { audiosPreviewPagingData() }.collectAsLazyPagingItems()
            AudioListAdaptiveContent(
                audios = audios,
                categoryTitle = "فتاوى",
                selectedAudio = AudioPreviewSelection,
                playingAudioUrl = AudioPreviewSelection.audioUrl,
                onSelectAudio = {},
                onNavigateToAudioDetail = { _, _ -> },
                onNavigateBack = {},
                playerPane = { PlayerPanePreviewContent() },
            )
        }
    }

    private fun setPlayer(window: DpSize, tokens: AdaptiveLayoutTokens, darkTheme: Boolean) {
        rule.setShellContent(window, tokens, darkTheme = darkTheme) {
            AudioDetailScreen(
                uiState = PlayerPreviewState,
                onNavigateUp = {}, onPlayPauseToggle = {}, onSeek = {}, onRewind = {}, onForward = {},
                onCycleSpeed = {}, onDownload = {}, onCancelDownload = {}, onShare = {},
            )
        }
    }

    @Test
    fun categories_expanded_listAndEmptyDetail() {
        setCategories(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = false)
        rule.saveShellShot("audio-categories-expanded-light")
        val list = rule.boundsInShell(AdaptivePanesDefaults.ListPaneTestTag)
        val row = rule.boundsOfText("فتاوى")
        assertDp(list.right - 1.dp - 16.dp, row.right, "category card inset 16 in the list pane")
        assertDp(366.dp, row.widthDp, "rows fill the card: 398 - 2 × 16")
        val detail = rule.boundsInShell(AdaptivePanesDefaults.DetailPaneTestTag)
        val empty = rule.boundsInShell(EmptyDetailDefaults.TestTag)
        assertDp((detail.left + detail.right) / 2, (empty.left + empty.right) / 2, "empty detail centred")
    }

    @Test
    fun categories_expanded_dark() {
        setCategories(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = true)
        rule.saveShellShot("audio-categories-expanded-dark")
    }

    @Test
    fun categories_medium() {
        setCategories(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("audio-categories-medium-light")
        val row = rule.boundsOfText("فتاوى")
        assertDp(640.dp, row.widthDp, "the phone list in the 672 content: 672 - 2 × 16")
    }

    @Test
    fun fatwasAndPlayer_expanded() {
        setFatwas(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = false)
        rule.saveShellShot("player-expanded-light")
        val list = rule.boundsInShell(AdaptivePanesDefaults.ListPaneTestTag)
        val row = rule.boundsOfText(AudiosPreviewData[0].title)
        assertDp(366.dp, row.widthDp, "rows fill the list pane")
        assertDp(list.right - 17.dp, row.right, "16 padding inside the outline")

        val detail = rule.boundsInShell(AdaptivePanesDefaults.DetailPaneTestTag)
        val seek = rule.boundsInShell(rule.onNode(seekBar))
        assertDp(312.dp, seek.widthDp, "player controls keep their designed width")
        assertDp((detail.left + detail.right) / 2, (seek.left + seek.right) / 2, "centred in the detail pane")
    }

    @Test
    fun fatwasAndPlayer_expanded_dark() {
        setFatwas(ExpandedWindow, AdaptiveLayoutTokens.Expanded, darkTheme = true)
        rule.saveShellShot("player-expanded-dark")
    }

    @Test
    fun fatwas_medium() {
        setFatwas(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("fatwas-medium-light")
        val row = rule.boundsOfText(AudiosPreviewData[0].title)
        assertDp(640.dp, row.widthDp, "the phone list in the 672 content")
    }

    @Test
    fun player_medium_controlsFillTheWidth() {
        setPlayer(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = false)
        rule.saveShellShot("player-medium-light")
        val seek = rule.boundsInShell(rule.onNode(seekBar))
        assertDp(624.dp, seek.widthDp, "Figma 63:3658: the phone's 24 padding in the 672 content")
    }

    @Test
    fun player_medium_dark() {
        setPlayer(MediumWindow, AdaptiveLayoutTokens.Medium, darkTheme = true)
        rule.saveShellShot("player-medium-dark")
    }

    @Test
    fun player_compact_isThePhonePlayer() {
        setPlayer(CompactWindow, AdaptiveLayoutTokens.Compact, darkTheme = false)
        rule.saveShellShot("player-compact-light")
        val seek = rule.boundsInShell(rule.onNode(seekBar))
        assertDp(312.dp, seek.widthDp, "360 - 2 × 24")
    }
}
