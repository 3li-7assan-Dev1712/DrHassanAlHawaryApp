package com.example.feature.share.presentation

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.R
import com.example.feature.share.domain.ShareBackgroundSource
import com.example.feature.share.domain.ShareCardContent
import com.example.feature.share.domain.ShareExportState
import com.example.feature.share.engine.ShareFileStore
import com.example.feature.share.engine.TextCardBitmapRenderer
import com.example.feature.share.engine.TextCardSpec
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TextCardPreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val shareFileStore: ShareFileStore,
    private val textCardBitmapRenderer: TextCardBitmapRenderer,
) : ViewModel() {

    /** Identifies this share session's temp file - stable across rotation since the ViewModel survives it. */
    private val cardId = UUID.randomUUID().toString()
    private val spec = TextCardSpec.default()

    private val quoteText = savedStateHandle.get<String>(ARG_QUOTE_TEXT).orEmpty()
    private val articleTitle = savedStateHandle.get<String>(ARG_ARTICLE_TITLE)?.takeIf { it.isNotBlank() }
    private val instituteName = context.getString(R.string.app_name)

    private val content = ShareCardContent(
        title = quoteText,
        category = articleTitle?.let { context.getString(R.string.share_text_attribution, it) },
        instituteName = instituteName,
        background = ShareBackgroundSource.Gradient,
        logoResId = R.drawable.dr_hassan_image,
    )

    private val _uiState = MutableStateFlow(TextCardPreviewUiState(content = content))
    val uiState = _uiState.asStateFlow()

    /** One-shot: the screen collects this and fires the actual share/chooser intent. */
    private val _shareIntentEvent = MutableSharedFlow<Uri>(extraBufferCapacity = 1)
    val shareIntentEvent = _shareIntentEvent.asSharedFlow()

    private var renderJob: Job? = null

    /** Set once the user taps Share; the instant the render lands, we fire the intent. */
    private var shareWhenReady = false

    /** Rapid double-tap guard, and once true, never delete the rendered file ourselves. */
    private var hasBeenShared = false

    init {
        shareFileStore.sweepStale()
    }

    /** Tapping Share is what starts rendering - nothing runs eagerly before this. */
    fun onShareClicked() {
        if (hasBeenShared) return
        when (_uiState.value.exportState) {
            is ShareExportState.Ready -> fireShareIntent(shareFileStore.textCardFile(cardId))
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
        renderJob?.cancel()
        // Set synchronously (not inside the coroutine below) so the generating overlay
        // appears on the very next frame after the tap, with zero dispatch lag.
        _uiState.update { it.copy(exportState = ShareExportState.Preparing, errorMessage = null) }
        renderJob = viewModelScope.launch {
            if (shareFileStore.usableSpaceBytes() < MIN_USABLE_SPACE_BYTES) {
                _uiState.update {
                    it.copy(exportState = ShareExportState.Idle, errorMessage = context.getString(R.string.share_error_low_space))
                }
                return@launch
            }

            val outputFile = shareFileStore.textCardFile(cardId)
            val result = runCatching {
                withContext(Dispatchers.Default) {
                    val bitmap = textCardBitmapRenderer.render(context, content, spec)
                    outputFile.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
                    bitmap.recycle()
                }
            }

            result.onSuccess {
                _uiState.update { it.copy(exportState = ShareExportState.Ready(shareFileStore.uriForFile(outputFile))) }
                if (shareWhenReady) {
                    shareWhenReady = false
                    fireShareIntent(outputFile)
                }
            }.onFailure {
                _uiState.update {
                    it.copy(exportState = ShareExportState.Idle, errorMessage = context.getString(R.string.share_error_generic))
                }
            }
        }
    }

    private fun fireShareIntent(file: File) {
        hasBeenShared = true
        _shareIntentEvent.tryEmit(shareFileStore.uriForFile(file))
    }

    override fun onCleared() {
        renderJob?.cancel()
        if (!hasBeenShared) {
            shareFileStore.deleteTextCard(cardId)
        }
        super.onCleared()
    }

    companion object {
        const val ARG_QUOTE_TEXT = "quoteText"
        const val ARG_ARTICLE_TITLE = "articleTitle"

        // An image render is tiny compared to the video flow's 150MB guard.
        private const val MIN_USABLE_SPACE_BYTES = 20L * 1024 * 1024
    }
}
