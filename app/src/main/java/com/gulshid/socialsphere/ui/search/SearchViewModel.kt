package com.gulshid.socialsphere.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gulshid.socialsphere.data.model.User
import com.gulshid.socialsphere.data.repository.UserRepository
import com.gulshid.socialsphere.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** A search result paired with whether the current user already follows them. */
data class FollowableUser(val user: User, val isFollowing: Boolean)

class SearchViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _searchResults = MutableLiveData<Resource<List<FollowableUser>>>()
    val searchResults: LiveData<Resource<List<FollowableUser>>> = _searchResults

    private var searchJob: Job? = null
    private var latestResults: List<FollowableUser> = emptyList()

    /** Debounces user input by 350ms to avoid firing a query per keystroke. */
    fun search(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            latestResults = emptyList()
            _searchResults.value = Resource.Success(emptyList())
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            _searchResults.value = Resource.Loading
            when (val result = userRepository.searchUsers(query)) {
                is Resource.Success -> {
                    val withFollowStatus = result.data.map { user ->
                        FollowableUser(user, userRepository.isFollowing(user.uid))
                    }
                    latestResults = withFollowStatus
                    _searchResults.value = Resource.Success(withFollowStatus)
                }
                is Resource.Error -> _searchResults.value = Resource.Error(result.message)
                Resource.Loading -> Unit
            }
        }
    }

    /** Toggles follow state, updating the list optimistically and reverting on failure. */
    fun toggleFollow(item: FollowableUser) {
        latestResults = latestResults.map {
            if (it.user.uid == item.user.uid) it.copy(isFollowing = !it.isFollowing) else it
        }
        _searchResults.value = Resource.Success(latestResults)

        viewModelScope.launch {
            val result = userRepository.toggleFollow(item.user.uid, item.isFollowing)
            if (result is Resource.Error) {
                latestResults = latestResults.map {
                    if (it.user.uid == item.user.uid) it.copy(isFollowing = item.isFollowing) else it
                }
                _searchResults.value = Resource.Success(latestResults)
            }
        }
    }
}
