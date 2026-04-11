package com.example.feature.image.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.ImageGroupWithImages
import com.example.feature.image.domain.use_case.GetGroupImagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject



data class ImageDetailUiState(
    val isLoading: Boolean = true,
    val imageGroup: ImageGroupWithImages? = null,
    val error: String? = null
)


@HiltViewModel
class ImageDetailViewModel @Inject constructor(
    private val getImageGroupDetailsUseCase: GetGroupImagesUseCase,
    savedStateHandle: SavedStateHandle // To get the navigation argument
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId")!!

    val uiState: StateFlow<ImageDetailUiState> =
        getImageGroupDetailsUseCase(groupId)
            .map { imageGroup ->
                // When data arrives, update the state to show it
                ImageDetailUiState(isLoading = false, imageGroup = imageGroup)
            }
            .catch { throwable ->
                // If an error occurs in the flow, you can represent it
                emit(ImageDetailUiState(isLoading = false, error = "Failed to load images."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                // The initial state is explicitly loading
                initialValue = ImageDetailUiState(isLoading = true)
            )

   /* val imageGroupDetails: StateFlow<ImageGroupWithImages?> =
        getImageGroupDetailsUseCase(groupId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )*/

    /*

    private fun loadImageGroupDetails() {
        viewModelScope.launch {
            getImageGroupDetailsUseCase(groupId)
                .map { imageGroup ->
                    // When data arrives, update the state to show it
                    _state.update {
                        it.copy(isLoading = false, imageGroup = imageGroup)
                    }
                }
                .catch { throwable ->
                    // If an error occurs in the flow, you can represent it
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unknown error"
                        )
                    }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = ImageDetailUiState(isLoading = true)
                )
        }
    }
     */
}