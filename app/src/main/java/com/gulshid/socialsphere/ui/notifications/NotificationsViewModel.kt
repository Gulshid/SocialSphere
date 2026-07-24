package com.gulshid.socialsphere.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gulshid.socialsphere.data.model.Notification
import com.gulshid.socialsphere.data.repository.NotificationRepository
import com.gulshid.socialsphere.utils.Resource
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val _notificationsState = MutableLiveData<Resource<List<Notification>>>()
    val notificationsState: LiveData<Resource<List<Notification>>> = _notificationsState

    fun loadNotifications() {
        _notificationsState.value = Resource.Loading
        viewModelScope.launch {
            _notificationsState.value = notificationRepository.getNotifications()
        }
    }
}
