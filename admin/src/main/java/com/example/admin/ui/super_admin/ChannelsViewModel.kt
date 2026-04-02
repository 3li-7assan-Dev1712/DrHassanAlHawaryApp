package com.example.admin.ui.super_admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.module.Channel
import com.example.domain.use_cases.channel.AddChannelUseCase
import com.example.domain.use_cases.channel.DeleteChannelUseCase
import com.example.domain.use_cases.channel.GetChannelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChannelsUiState(
    val channels: List<Channel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ChannelsViewModel @Inject constructor(
    private val getChannelsUseCase: GetChannelsUseCase,
    private val addChannelUseCase: AddChannelUseCase,
    private val deleteChannelUseCase: DeleteChannelUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChannelsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadChannels()
    }

    private fun loadChannels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getChannelsUseCase()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { channelsList ->
                    _uiState.update { it.copy(channels = channelsList, isLoading = false) }
                }
        }
    }

    fun addChannel(channelId: String, batch: String, orderStr: String) {
        val order = orderStr.toIntOrNull()
        if (order == null || order < 0) {
            _uiState.update { it.copy(error = "Order must be a positive number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            
            val channel = Channel(channelId = channelId.trim(), batch = batch.trim(), order = order)
            
            addChannelUseCase(channel)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Channel added successfully") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun deleteChannel(channelId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            deleteChannelUseCase(channelId)
                .onSuccess {
                    // Update UI immediately (Optimistic Update)
                    _uiState.update { state -> 
                        state.copy(
                            isLoading = false, 
                            successMessage = "Channel deleted successfully",
                            channels = state.channels.filter { it.channelId != channelId }
                        ) 
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
