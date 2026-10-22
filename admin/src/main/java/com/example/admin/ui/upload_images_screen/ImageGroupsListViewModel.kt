package app.netlify.devalihassan.admin.ui.upload_images_screen

import com.example.domain.use_cases.images.UpdateImageGroupSeriesUseCase
import com.example.domain.use_cases.series.SyncSeriesUseCase
import com.example.domain.use_cases.series.GetSeriesUseCase
import com.example.domain.module.SeriesCollection
import app.netlify.devalihassan.admin.ui.components.SeriesFormController
import androidx.lifecycle.viewModelScope
import app.netlify.devalihassan.admin.ui.components.AdminActionViewModel
import com.example.domain.module.ImageGroup
import com.example.domain.use_cases.images.DeleteImageGroupUseCase
import com.example.domain.use_cases.images.GetAllRemoteImageGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ImageGroupsListUiState {
    object Loading : ImageGroupsListUiState
    data class Success(val groups: List<ImageGroup>) : ImageGroupsListUiState
    data class Error(val message: String) : ImageGroupsListUiState
}

@HiltViewModel
class ImageGroupsListViewModel @Inject constructor(
    private val getAllRemoteImageGroupsUseCase: GetAllRemoteImageGroupsUseCase,
    private val deleteImageGroupUseCase: DeleteImageGroupUseCase,
    private val updateImageGroupSeriesUseCase: UpdateImageGroupSeriesUseCase,
    getSeriesUseCase: GetSeriesUseCase,
    syncSeriesUseCase: SyncSeriesUseCase,
) : AdminActionViewModel() {

    /** The series dialog's fields (there is no design edit screen). */
    val seriesForm = SeriesFormController(viewModelScope, getSeriesUseCase, syncSeriesUseCase, SeriesCollection.IMAGE_GROUPS)

    private val _uiState = MutableStateFlow<ImageGroupsListUiState>(ImageGroupsListUiState.Loading)
    val uiState: StateFlow<ImageGroupsListUiState> = _uiState.asStateFlow()

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { ImageGroupsListUiState.Loading }
            try {
                val groups = getAllRemoteImageGroupsUseCase()
                _uiState.update { ImageGroupsListUiState.Success(groups) }
            } catch (e: Exception) {
                _uiState.update { ImageGroupsListUiState.Error(e.message ?: "Failed to load image groups") }
            }
        }
    }

    fun deleteGroup(groupId: String) {
        runAction({ deleteImageGroupUseCase(groupId) }) { loadGroups() }
    }

    /** Opens [group]'s series in the dialog's fields. */
    fun editSeries(group: ImageGroup) {
        seriesForm.load(group.seriesId, group.episodeNumber)
    }

    /** Saves the dialog's choice for [groupId], if it changed. */
    fun saveSeries(groupId: String) {
        val placement = seriesForm.state.value.changedPlacement ?: return
        runAction({ updateImageGroupSeriesUseCase(groupId, placement) }) { loadGroups() }
    }
}
