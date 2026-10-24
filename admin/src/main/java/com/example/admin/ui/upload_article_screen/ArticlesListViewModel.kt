package app.netlify.devalihassan.admin.ui.upload_article_screen

import androidx.lifecycle.SavedStateHandle
import app.netlify.devalihassan.admin.ui.content.AdminContentListViewModel
import app.netlify.devalihassan.admin.ui.content.ContentListDependencies
import app.netlify.devalihassan.admin.ui.content.toAdminItem
import com.example.domain.module.Article
import com.example.domain.module.SeriesCollection
import com.example.domain.use_cases.articles.DeleteArticleUseCase
import com.example.domain.use_cases.articles.GetAllRemoteArticlesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Every article on the server, searchable and filterable by series. */
@HiltViewModel
class ArticlesListViewModel @Inject constructor(
    private val getAllRemoteArticlesUseCase: GetAllRemoteArticlesUseCase,
    private val deleteArticleUseCase: DeleteArticleUseCase,
    savedStateHandle: SavedStateHandle,
    dependencies: ContentListDependencies,
) : AdminContentListViewModel<Article>(SeriesCollection.ARTICLES, savedStateHandle, dependencies) {

    override suspend fun fetchAll(): List<Article> = getAllRemoteArticlesUseCase()
    override fun itemOf(value: Article) = value.toAdminItem()

    fun deleteArticle(article: Article) = runAndReload { runCatching { deleteArticleUseCase(article.id) } }
}
