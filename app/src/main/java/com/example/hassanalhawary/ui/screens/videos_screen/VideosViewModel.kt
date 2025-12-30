package com.example.hassanalhawary.ui.screens.videos_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.data.VideosRepositoryImpl
import com.example.domain.module.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class VideosViewModel @Inject constructor(
    private val videosRepository: VideosRepositoryImpl
) : ViewModel() {


    val videos: Flow<PagingData<Video>> = videosRepository.getVideos().cachedIn(viewModelScope)


}