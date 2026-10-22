package app.netlify.devalihassan.admin.ui.upload_video_screen

import com.example.domain.use_cases.series.SyncSeriesUseCase
import com.example.domain.use_cases.series.GetSeriesUseCase
import com.example.domain.module.SeriesCollection
import app.netlify.devalihassan.admin.ui.components.SeriesFormController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_cases.audios.UploadResult
import com.example.domain.use_cases.videos.GetVideoByIdUseCase
import com.example.domain.use_cases.videos.UpdateVideoUseCase
import com.example.domain.use_cases.videos.UploadVideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UploadVideoViewModel @Inject constructor(
    private val uploadVideoUseCase: UploadVideoUseCase,
    private val updateVideoUseCase: UpdateVideoUseCase,
    private val getVideoByIdUseCase: GetVideoByIdUseCase,
    savedStateHandle: SavedStateHandle,
    getSeriesUseCase: GetSeriesUseCase,
    syncSeriesUseCase: SyncSeriesUseCase,
) : ViewModel() {

    /** The series the video goes in, and its episode number. */
    val seriesForm = SeriesFormController(viewModelScope, getSeriesUseCase, syncSeriesUseCase, SeriesCollection.VIDEOS)

    var title by mutableStateOf("")
    var videoUrl by mutableStateOf("")
    var categoryId by mutableStateOf("")
    var videoId: String? by mutableStateOf(savedStateHandle["videoId"])

    private val _uploadState = MutableStateFlow<UploadResult?>(null)
    val uploadState: StateFlow<UploadResult?> = _uploadState

    init {
        videoId?.let { id ->
            loadVideo(id)
        }
    }

    private fun loadVideo(id: String) {
        viewModelScope.launch {
            _uploadState.value = UploadResult.Progress(0)
            val video = getVideoByIdUseCase(id)
            if (video != null) {
                title = video.title
                videoUrl = video.videoUrl
                categoryId = video.categoryId ?: ""
                seriesForm.load(video.seriesId, video.episodeNumber)
                _uploadState.value = null
            } else {
                _uploadState.value = UploadResult.Error("Failed to load video data")
            }
        }
    }

    fun onCategoryChange(newCategoryId: String) {
        categoryId = newCategoryId
        seriesForm.onCategoryChange(newCategoryId)
    }

    fun uploadVideo() {
        if (title.isBlank() || videoUrl.isBlank()) {
            _uploadState.value = UploadResult.Error("Please fill all fields")
            return
        }
        if (categoryId.isBlank()) {
            _uploadState.value = UploadResult.Error("Please select a category")
            return
        }
        val series = seriesForm.state.value
        if (!series.isValid) {
            _uploadState.value = UploadResult.Error("Please enter the episode number (1 or more)")
            return
        }

        viewModelScope.launch {
            val flow = if (videoId == null) {
                uploadVideoUseCase(title, videoUrl, categoryId, series.placement)
            } else {
                updateVideoUseCase(videoId!!, title, videoUrl, categoryId, series.changedPlacement)
            }

            flow.collect { result ->
                _uploadState.value = result
                if (result is UploadResult.Success && videoId == null) {
                    title = ""
                    videoUrl = ""
                    categoryId = ""
                    seriesForm.reset()
                }
            }
        }
    }

    fun resetState() {
        _uploadState.value = null
    }
}
