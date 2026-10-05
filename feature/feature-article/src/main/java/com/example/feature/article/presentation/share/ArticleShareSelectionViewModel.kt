package com.example.feature.article.presentation.share

import android.content.Context
import android.text.SpannableString
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.R
import com.example.domain.text.ArabicNumerals
import com.example.domain.text.ArticleText
import com.example.domain.text.SelectableArticle
import com.example.domain.text.TextSanitizer
import com.example.feature.article.domain.use_case.GetArticleByIdUseCase
import com.example.feature.share.engine.QuoteCardPainter
import com.example.feature.share.engine.QuotePage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Free long-press-and-drag selection of article text for the quote images. The article
 * is shown cleaned (as in the reader) as one text ([SelectableArticle]). "متابعة" still
 * hands the next step what it has always taken: the article id + a range in
 * [ArticleText.displayText] - it rebuilds that text, sanitizes the range and paginates
 * it, exactly as measured here.
 */
@HiltViewModel
class ArticleShareSelectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getArticleByIdUseCase: GetArticleByIdUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleShareSelectionUiState())
    val uiState: StateFlow<ArticleShareSelectionUiState> = _uiState.asStateFlow()

    /** Fonts and paints for the page count; created once, off the main thread. */
    private var painter: QuoteCardPainter? = null
    private var measureJob: Job? = null
    private var loadedContent: String? = null

    init {
        val articleId = savedStateHandle.get<String>(ARG_ARTICLE_ID)
        if (articleId != null) {
            loadArticle(articleId)
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Article not found") }
        }
    }

    private fun loadArticle(articleId: String) {
        viewModelScope.launch {
            try {
                getArticleByIdUseCase(articleId).collect { article ->
                    if (article == null) {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Article not found") }
                        return@collect
                    }
                    // The flow can re-emit the same article: keep the user's selection then.
                    if (article.content == loadedContent) return@collect
                    loadedContent = article.content
                    val selectable = withContext(Dispatchers.Default) { SelectableArticle.build(article.content, article.title) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            articleId = articleId,
                            articleTitle = ArticleText.cleanTitle(article.title),
                            article = selectable,
                            selectionStart = 0,
                            selectionEnd = 0,
                            errorMessage = if (selectable.text.isBlank()) "Article not found" else null,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }

    /** The drag selection, in [SelectableArticle.text] offsets. */
    fun onSelectionChanged(start: Int, end: Int) {
        val state = _uiState.value
        if (start == state.selectionStart && end == state.selectionEnd) return
        _uiState.update { it.copy(selectionStart = start, selectionEnd = end) }
        measure()
    }

    fun onClearSelection() = onSelectionChanged(0, 0)

    /**
     * Characters and images for the current selection, measured with the quote-image
     * step's own sanitizer and pagination, debounced ~200ms so quick taps don't each render.
     */
    private fun measure() {
        measureJob?.cancel()
        val state = _uiState.value
        val article = state.article
        val range = state.selectionRange
        if (article == null || range == null) {
            _uiState.update { it.copy(characterCount = 0, pageCount = 0, firstPage = null) }
            return
        }
        measureJob = viewModelScope.launch {
            delay(MEASURE_DEBOUNCE_MS)
            val (characters, pages) = withContext(Dispatchers.Default) {
                val quote = TextSanitizer.sanitize(article.displayText, range.first, range.last + 1).text
                val painter = painter ?: QuoteCardPainter(context).also { painter = it }
                quote.length to painter.paginate(SpannableString(ArabicNumerals.digits(quote)))
            }
            val title = _uiState.value.articleTitle.takeIf { it.isNotBlank() }
            _uiState.update {
                it.copy(
                    characterCount = characters,
                    pageCount = pages.size,
                    firstPage = pages.firstOrNull()?.let { text -> QuotePage(text, 0, pages.size, title, R.drawable.admin_logo_app) },
                )
            }
        }
    }

    companion object {
        const val ARG_ARTICLE_ID = "articleId"
        private const val MEASURE_DEBOUNCE_MS = 200L
    }
}
