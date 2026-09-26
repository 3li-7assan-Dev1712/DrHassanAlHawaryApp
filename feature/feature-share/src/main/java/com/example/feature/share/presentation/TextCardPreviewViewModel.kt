package com.example.feature.share.presentation

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.SpannableString
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.R
import com.example.domain.repository.ArticlesRepository
import com.example.feature.share.domain.ArabicNumerals
import com.example.feature.share.domain.ArticleText
import com.example.feature.share.domain.ShareExportState
import com.example.feature.share.domain.TextSanitizer
import com.example.feature.share.engine.QuoteCardPainter
import com.example.feature.share.engine.QuoteCardRenderer
import com.example.feature.share.engine.QuotePage
import com.example.feature.share.engine.ShareFileStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

/**
 * Article excerpt -> one or more branded quote images, previewed and shared in order.
 *
 * Receives the article id + selection offsets (not the text itself - a whole
 * article URL-encoded into a nav route doesn't scale) and reloads the article,
 * rebuilding the exact display text the selection was made on via
 * [ArticleText.displayText]. The excerpt is then cleaned ([TextSanitizer]) and
 * split into pages by the same painter that draws them.
 */
@HiltViewModel
class TextCardPreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val shareFileStore: ShareFileStore,
    private val articlesRepository: ArticlesRepository,
    private val renderer: QuoteCardRenderer,
) : ViewModel() {

    /** Identifies this share session's files - stable across rotation since the ViewModel survives it. */
    private val cardId = UUID.randomUUID().toString()

    private val articleId = savedStateHandle.get<String>(ARG_ARTICLE_ID).orEmpty()
    private val selectionStart = savedStateHandle.get<Int>(ARG_SELECTION_START) ?: 0
    private val selectionEnd = savedStateHandle.get<Int>(ARG_SELECTION_END) ?: 0

    private val _uiState = MutableStateFlow(TextCardPreviewUiState())
    val uiState = _uiState.asStateFlow()

    /** One-shot: the screen collects this and fires the share intent with the URIs in page order. */
    private val _shareIntentEvent = MutableSharedFlow<List<Uri>>(extraBufferCapacity = 1)
    val shareIntentEvent = _shareIntentEvent.asSharedFlow()

    private var renderJob: Job? = null
    private var shareWhenReady = false
    private var renderedFiles: List<File> = emptyList()

    /** Once true, never delete the rendered files ourselves (the receiving app may still be reading them). */
    private var hasBeenShared = false

    init {
        shareFileStore.sweepStale()
        loadPages()
    }

    private fun loadPages() {
        viewModelScope.launch {
            val article = runCatching { articlesRepository.getArticleById(articleId).firstOrNull() }.getOrNull()
            if (article == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = context.getString(R.string.share_error_generic)) }
                return@launch
            }
            val pages = withContext(Dispatchers.Default) {
                val displayText = ArticleText.displayText(article.content)
                val quote = TextSanitizer.sanitize(displayText, selectionStart, selectionEnd)
                val title = ArticleText.cleanTitle(article.title).takeIf { it.isNotBlank() }
                val texts = QuoteCardPainter(context).paginate(SpannableString(ArabicNumerals.digits(quote.text)))
                texts.mapIndexed { i, text -> QuotePage(text, i, texts.size, title, R.drawable.admin_logo_app) }
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    pages = pages,
                    errorMessage = if (pages.isEmpty()) context.getString(R.string.share_text_no_selection) else null,
                )
            }
        }
    }

    /** Tapping Share is what renders the files - nothing is written before this. */
    fun onShareClicked() {
        when (_uiState.value.exportState) {
            is ShareExportState.Ready -> fireShareIntent()
            ShareExportState.Preparing, is ShareExportState.Encoding -> Unit // already rendering
            ShareExportState.Idle, is ShareExportState.Failed -> {
                shareWhenReady = true
                render()
            }
        }
    }

    fun onRetry() {
        _uiState.update { it.copy(errorMessage = null) }
        render()
    }

    private fun render() {
        val pages = _uiState.value.pages
        if (pages.isEmpty()) return
        renderJob?.cancel()
        // Set synchronously so the generating overlay appears on the very next frame after the tap.
        _uiState.update { it.copy(exportState = ShareExportState.Preparing, errorMessage = null) }
        renderJob = viewModelScope.launch {
            if (shareFileStore.usableSpaceBytes() < MIN_USABLE_SPACE_BYTES) {
                _uiState.update {
                    it.copy(exportState = ShareExportState.Idle, errorMessage = context.getString(R.string.share_error_low_space))
                }
                return@launch
            }

            val result = runCatching {
                withContext(Dispatchers.Default) {
                    val painter = QuoteCardPainter(context)
                    pages.map { page ->
                        val file = shareFileStore.textCardFile(cardId, page.index)
                        val bitmap = renderer.render(painter, page)
                        file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
                        bitmap.recycle()
                        file
                    }
                }
            }

            result.onSuccess { files ->
                renderedFiles = files
                _uiState.update { it.copy(exportState = ShareExportState.Ready(shareFileStore.uriForFile(files.first()))) }
                if (shareWhenReady) {
                    shareWhenReady = false
                    fireShareIntent()
                }
            }.onFailure {
                _uiState.update {
                    it.copy(exportState = ShareExportState.Idle, errorMessage = context.getString(R.string.share_error_generic))
                }
            }
        }
    }

    private fun fireShareIntent() {
        if (renderedFiles.isEmpty()) return
        hasBeenShared = true
        _shareIntentEvent.tryEmit(renderedFiles.map(shareFileStore::uriForFile))
    }

    override fun onCleared() {
        renderJob?.cancel()
        if (!hasBeenShared) shareFileStore.deleteTextCards(cardId)
        super.onCleared()
    }

    companion object {
        const val ARG_ARTICLE_ID = "articleId"
        const val ARG_SELECTION_START = "selectionStart"
        const val ARG_SELECTION_END = "selectionEnd"

        // Image renders are tiny compared to the video flow's 150MB guard.
        private const val MIN_USABLE_SPACE_BYTES = 20L * 1024 * 1024
    }
}
