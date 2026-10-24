package app.netlify.devalihassan.admin.ui.upload_video_screen

import androidx.lifecycle.SavedStateHandle
import app.netlify.devalihassan.admin.ui.content.AdminContentListViewModel
import app.netlify.devalihassan.admin.ui.content.ContentListDependencies
import app.netlify.devalihassan.admin.ui.content.toAdminItem
import com.example.domain.module.SeriesCollection
import com.example.domain.module.Video
import com.example.domain.use_cases.videos.DeleteVideoUseCase
import com.example.domain.use_cases.videos.GetAllRemoteVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Every video on the server, searchable and filterable (see [AdminContentListViewModel]). */
@HiltViewModel
class VideosListViewModel @Inject constructor(
    private val getAllRemoteVideosUseCase: GetAllRemoteVideosUseCase,
    private val deleteVideoUseCase: DeleteVideoUseCase,
    savedStateHandle: SavedStateHandle,
    dependencies: ContentListDependencies,
) : AdminContentListViewModel<Video>(SeriesCollection.VIDEOS, savedStateHandle, dependencies) {

    override suspend fun fetchAll(): List<Video> = getAllRemoteVideosUseCase()
    override fun itemOf(value: Video) = value.toAdminItem()

    fun deleteVideo(video: Video) = runAndReload { deleteVideoUseCase(video.id) }
}
