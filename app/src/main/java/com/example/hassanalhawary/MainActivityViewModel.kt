package com.example.hassanalhawary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class MainActivityViewModel @Inject constructor() :ViewModel()
    {

    private val _state = MutableStateFlow(MainActivityState())

    val state = _state.asStateFlow()




    fun hideProgressBar() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    showProgressBar = false
                )
            }
        }
    }
}