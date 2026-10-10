package com.example.feature.article.presentation.detail

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.DataStoreRepository
import com.example.domain.text.ArticleTextCleaner
import com.example.feature.article.domain.use_case.GetArticleByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DetailArticleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getArticleByIdUseCase: GetArticleByIdUseCase,
    private val dataStoreRepository: DataStoreRepository,
) : ViewModel() {
    private val textCleaner = ArticleTextCleaner()

    /** Reader text-size step, 0..[MAX_FONT_STEP]; 1 is the original size. */
    val fontStep: StateFlow<Int> = dataStoreRepository.readerFontStep()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)

    fun setFontStep(step: Int) {
        viewModelScope.launch { dataStoreRepository.setReaderFontStep(step.coerceIn(0, MAX_FONT_STEP)) }
    }

//    private val articleId: String? = savedStateHandle["articleId"]

    private val _uiState = MutableStateFlow<DetailArticleUiState>(DetailArticleUiState.Loading)
    val uiState: StateFlow<DetailArticleUiState> = _uiState.asStateFlow()


    private var shownArticleId: String? = null
    private var loadJob: Job? = null

    init {
        val articleId = savedStateHandle.get<String>("articleId")
        Log.d(TAG, "articleId = : $articleId ")
        if (articleId != null) {
            showArticle(articleId)
        }
    }

    /**
     * Shows [articleId] (nothing if it is already shown). The reader beside the article list
     * on tablets has no route argument and changes article with the selection.
     */
    fun showArticle(articleId: String) {
        if (articleId == shownArticleId) return
        shownArticleId = articleId
        loadJob?.cancel()
        _uiState.value = DetailArticleUiState.Loading
        loadJob = fetchArticleDetailsById(articleId)
    }

    private fun fetchArticleDetailsById(articleId: String): Job {
        return viewModelScope.launch {

            try {
                getArticleByIdUseCase(articleId).collect { art ->
                    if (art != null) {
                        _uiState.value = DetailArticleUiState.Success(
                            article = art,
                            paragraphs = textCleaner.readerParagraphs(art.content, art.title),
                            readingMinutes = textCleaner.readingMinutes(art.content),
                        )
                    } else {
                        _uiState.value = DetailArticleUiState.Error(
                            message = "Article not found"
                        )
                    }
                }

            } catch (e: CancellationException) {
                // Another article was chosen: its load owns the state now.
                throw e
            } catch (e: Exception) {
                _uiState.value = DetailArticleUiState.Error(
                    message = e.message ?: "Unknown error"
                )
            }

        }

    }

    companion object {
        const val MAX_FONT_STEP = 3
    }
}
