package app.netlify.devalihassan.admin.ui.upload_images_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.netlify.devalihassan.admin.ui.components.SeriesFormController
import app.netlify.devalihassan.admin.ui.content.AdminContentListViewModel
import app.netlify.devalihassan.admin.ui.content.ContentListDependencies
import app.netlify.devalihassan.admin.ui.content.toAdminItem
import com.example.domain.module.ImageGroup
import com.example.domain.module.SeriesCollection
import com.example.domain.use_cases.images.DeleteImageGroupUseCase
import com.example.domain.use_cases.images.GetAllRemoteImageGroupsUseCase
import com.example.domain.use_cases.images.UpdateImageGroupSeriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Every design group on the server, searchable and filterable by series. There is no design
 * edit screen: a tap opens the series dialog ([seriesForm]).
 */
@HiltViewModel
class ImageGroupsListViewModel @Inject constructor(
    private val getAllRemoteImageGroupsUseCase: GetAllRemoteImageGroupsUseCase,
    private val deleteImageGroupUseCase: DeleteImageGroupUseCase,
    private val updateImageGroupSeriesUseCase: UpdateImageGroupSeriesUseCase,
    savedStateHandle: SavedStateHandle,
    dependencies: ContentListDependencies,
) : AdminContentListViewModel<ImageGroup>(SeriesCollection.IMAGE_GROUPS, savedStateHandle, dependencies) {

    /** The series dialog's fields. */
    val seriesForm = SeriesFormController(viewModelScope, dependencies.getSeries, dependencies.syncSeries, SeriesCollection.IMAGE_GROUPS)

    override suspend fun fetchAll(): List<ImageGroup> = getAllRemoteImageGroupsUseCase()
    override fun itemOf(value: ImageGroup) = value.toAdminItem()

    fun deleteGroup(group: ImageGroup) = runAndReload { deleteImageGroupUseCase(group.id) }

    /** Opens [group]'s series in the dialog's fields. */
    fun editSeries(group: ImageGroup) {
        seriesForm.load(group.seriesId, group.episodeNumber)
    }

    /** Saves the dialog's choice for [groupId], if it changed. */
    fun saveSeries(groupId: String) {
        val placement = seriesForm.state.value.changedPlacement ?: return
        runAndReload { updateImageGroupSeriesUseCase(groupId, placement) }
    }
}
