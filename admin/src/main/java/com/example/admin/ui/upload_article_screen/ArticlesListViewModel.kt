package app.netlify.devalihassan.admin.ui.upload_article_screen

import androidx.lifecycle.viewModelScope
import app.netlify.devalihassan.admin.ui.components.AdminActionViewModel
import com.example.domain.module.Article
import com.example.domain.use_cases.articles.DeleteArticleUseCase
import com.example.domain.use_cases.articles.GetAllRemoteArticlesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ArticlesListUiState {
    object Loading : ArticlesListUiState
    data class Success(val articles: List<Article>) : ArticlesListUiState
    data class Error(val message: String) : ArticlesListUiState
}

@HiltViewModel
class ArticlesListViewModel @Inject constructor(
    private val getAllRemoteArticlesUseCase: GetAllRemoteArticlesUseCase,
    private val deleteArticleUseCase: DeleteArticleUseCase
) : AdminActionViewModel() {

    private val _uiState = MutableStateFlow<ArticlesListUiState>(ArticlesListUiState.Loading)
    val uiState: StateFlow<ArticlesListUiState> = _uiState.asStateFlow()

    fun loadArticles() {
        viewModelScope.launch {
            _uiState.update { ArticlesListUiState.Loading }
            try {
                val articles = getAllRemoteArticlesUseCase()
                _uiState.update { ArticlesListUiState.Success(articles) }
            } catch (e: Exception) {
                _uiState.update { ArticlesListUiState.Error(e.message ?: "Failed to load articles") }
            }
        }
    }

    fun deleteArticle(articleId: String) {
        runAction({ runCatching { deleteArticleUseCase(articleId) } }) { loadArticles() }
    }
}
