package app.netlify.devalihassan.admin.ui.upload_audio_screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import app.netlify.devalihassan.admin.R
import app.netlify.devalihassan.admin.ui.content.AdminContentList

/** Every audio: search, filter by category and series, change many at once. */
@Composable
fun AudiosListScreen(
    viewModel: AudiosListViewModel = hiltViewModel(),
    onAddAudio: () -> Unit,
    onEditAudio: (String) -> Unit
) {
    AdminContentList(
        viewModel = viewModel,
        addLabel = stringResource(R.string.upload_new_audio),
        onAdd = onAddAudio,
        onOpen = { onEditAudio(it.id) },
        onDelete = viewModel::deleteAudio,
    )
}
