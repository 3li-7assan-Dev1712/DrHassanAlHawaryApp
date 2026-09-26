package com.example.feature.home.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data_local.NotificationDao
import com.example.data_local.model.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
) : ViewModel() {

    /** null while the first read from the database is in flight. */
    val notifications: StateFlow<List<NotificationEntity>?> = notificationDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Opening the screen counts as reading everything on it. */
    fun markAllRead() {
        viewModelScope.launch { notificationDao.markAllRead() }
    }

    fun delete(id: Long) {
        viewModelScope.launch { notificationDao.deleteById(id) }
    }

    fun clearAll() {
        viewModelScope.launch { notificationDao.clearAll() }
    }
}
