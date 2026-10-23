package app.netlify.devalihassan.admin.ui.upload_audio_screen

import androidx.lifecycle.SavedStateHandle
import app.netlify.devalihassan.admin.ui.content.AdminContentListViewModel
import app.netlify.devalihassan.admin.ui.content.ContentListDependencies
import app.netlify.devalihassan.admin.ui.content.toAdminItem
import com.example.domain.module.Audio
import com.example.domain.module.SeriesCollection
import com.example.domain.use_cases.audios.DeleteAudioUseCase
import com.example.domain.use_cases.audios.GetAllRemoteAudiosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Every audio on the server, searchable and filterable (see [AdminContentListViewModel]). */
@HiltViewModel
class AudiosListViewModel @Inject constructor(
    private val getAllRemoteAudiosUseCase: GetAllRemoteAudiosUseCase,
    private val deleteAudioUseCase: DeleteAudioUseCase,
    savedStateHandle: SavedStateHandle,
    dependencies: ContentListDependencies,
) : AdminContentListViewModel<Audio>(SeriesCollection.AUDIOS, savedStateHandle, dependencies) {

    override suspend fun fetchAll(): List<Audio> = getAllRemoteAudiosUseCase()
    override fun itemOf(value: Audio) = value.toAdminItem()

    fun deleteAudio(audio: Audio) = runAndReload { deleteAudioUseCase(audio.id, audio.audioUrl) }
}
