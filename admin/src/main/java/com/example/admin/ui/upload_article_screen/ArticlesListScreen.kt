package app.netlify.devalihassan.admin.ui.upload_article_screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import app.netlify.devalihassan.admin.R
import app.netlify.devalihassan.admin.ui.content.AdminContentList

/** Every article: search, filter by series, change many at once. */
@Composable
fun ArticlesListScreen(
    viewModel: ArticlesListViewModel = hiltViewModel(),
    onAddArticle: () -> Unit,
    onEditArticle: (String) -> Unit
) {
    AdminContentList(
        viewModel = viewModel,
        addLabel = stringResource(R.string.upload_new_article),
        onAdd = onAddArticle,
        onOpen = { onEditArticle(it.id) },
        onDelete = viewModel::deleteArticle,
    )
}
