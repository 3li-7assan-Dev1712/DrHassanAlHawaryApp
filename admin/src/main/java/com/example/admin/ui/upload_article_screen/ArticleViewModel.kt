package app.netlify.devalihassan.admin.ui.upload_article_screen

import com.example.domain.use_cases.series.SyncSeriesUseCase
import com.example.domain.use_cases.series.GetSeriesUseCase
import com.example.domain.module.SeriesCollection
import app.netlify.devalihassan.admin.ui.components.SeriesFormController
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.Article
import com.example.domain.use_cases.GetArticleByIdUseCase
import com.example.domain.use_cases.articles.UpdateArticleUseCase
import com.example.domain.use_cases.articles.UploadArticleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject


@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val uploadArticleUseCase: UploadArticleUseCase,
    private val updateArticleUseCase: UpdateArticleUseCase,
    private val getArticleByIdUseCase: GetArticleByIdUseCase,
    savedStateHandle: SavedStateHandle,
    getSeriesUseCase: GetSeriesUseCase,
    syncSeriesUseCase: SyncSeriesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    /** The series the article goes in, and its episode number. */
    val seriesForm = SeriesFormController(viewModelScope, getSeriesUseCase, syncSeriesUseCase, SeriesCollection.ARTICLES)

    private val articleId: String? = savedStateHandle["articleId"]

    init {
        if (articleId != null) {
            loadArticle(articleId)
        }
    }

    private fun loadArticle(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            var seriesLoaded = false
            getArticleByIdUseCase(id).collectLatest { article ->
                // Once: a later emission of the cache must not undo the admin's choice.
                if (!seriesLoaded && article != null) {
                    seriesForm.load(article.seriesId, article.episodeNumber)
                    seriesLoaded = true
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        articleId = article?.id ?: "",
                        title = article?.title ?: "",
                        content = article?.content ?: "",
                        publishDate = article?.publishDate?.time ?: 0
                    )
                }
            }
        }
    }

    fun onEvent(event: ArticleUserEvent) {
        when (event) {
            is ArticleUserEvent.OnTitleChanged -> {
                _uiState.update { it.copy(title = event.title) }
            }

            is ArticleUserEvent.OnContentChanged -> {
                _uiState.update { it.copy(content = event.content) }
            }

            is ArticleUserEvent.OnPublishDateChanged -> {
                _uiState.update { it.copy(publishDate = event.date) }
            }

            ArticleUserEvent.OnUploadClicked -> {
                saveArticle()
            }

            ArticleUserEvent.OnUserMessageShown -> {
                _uiState.update { it.copy(userMessage = null) }
            }
        }
    }

    private fun saveArticle() {
        val currentState = _uiState.value

        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(userMessage = "Title cannot be empty.") }
            return
        }

        if (currentState.content.isBlank()) {
            _uiState.update { it.copy(userMessage = "Content cannot be empty.") }
            return
        }

        val series = seriesForm.state.value
        if (!series.isValid) {
            _uiState.update { it.copy(userMessage = "Please enter the episode number (1 or more)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val article = Article(
                    id = currentState.articleId ?: "",
                    title = currentState.title,
                    content = currentState.content,
                    publishDate = Date(currentState.publishDate),
                )
                
                if (currentState.articleId == null) {
                    uploadArticleUseCase(article, series.placement)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isArticleUploaded = true,
                            userMessage = "Article uploaded successfully!"
                        )
                    }
                } else {
                    updateArticleUseCase(article, series.changedPlacement)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isArticleUploaded = true,
                            userMessage = "Article updated successfully!"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = "Operation failed: ${e.message}"
                    )
                }
            }
        }
    }
}
