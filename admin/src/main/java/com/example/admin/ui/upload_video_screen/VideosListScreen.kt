package app.netlify.devalihassan.admin.ui.upload_video_screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import app.netlify.devalihassan.admin.R
import app.netlify.devalihassan.admin.ui.content.AdminContentList

/** Every video: search, filter by category and series, change many at once. */
@Composable
fun VideosListScreen(
    viewModel: VideosListViewModel = hiltViewModel(),
    onAddVideo: () -> Unit,
    onEditVideo: (String) -> Unit
) {
    AdminContentList(
        viewModel = viewModel,
        addLabel = stringResource(R.string.upload_new_video),
        onAdd = onAddVideo,
        onOpen = { onEditVideo(it.id) },
        onDelete = viewModel::deleteVideo,
    )
}
