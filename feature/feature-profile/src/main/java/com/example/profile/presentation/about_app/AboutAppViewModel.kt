package com.example.profile.presentation.about_app


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    provider: AppInfoProvider
) : ViewModel() {

    private val _appInfo = MutableStateFlow(provider.getAppInfo())
    val appInfo = _appInfo.asStateFlow()
}
