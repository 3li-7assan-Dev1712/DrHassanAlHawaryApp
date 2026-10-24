package app.netlify.devalihassan.admin.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.ContentCategory
import com.example.domain.module.ContentType
import com.example.domain.module.FixedCategories
import com.example.domain.use_cases.categories.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** The current audio and video categories for the admin's pickers and section headers. */
@HiltViewModel
class CategoryOptionsViewModel @Inject constructor(
    getCategoriesUseCase: GetCategoriesUseCase,
) : ViewModel() {

    private val audio = getCategoriesUseCase(ContentType.AUDIO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, FixedCategories.getCategories(ContentType.AUDIO))
    private val video = getCategoriesUseCase(ContentType.VIDEO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, FixedCategories.getCategories(ContentType.VIDEO))

    fun categories(type: ContentType): StateFlow<List<ContentCategory>> = when (type) {
        ContentType.VIDEO -> video
        else -> audio
    }
}
