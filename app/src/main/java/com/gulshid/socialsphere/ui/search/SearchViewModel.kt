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

class SearchViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _searchResults = MutableLiveData<Resource<List<User>>>()
    val searchResults: LiveData<Resource<List<User>>> = _searchResults

    private var searchJob: Job? = null

    /** Debounces user input by 350ms to avoid firing a query per keystroke. */
    fun search(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = Resource.Success(emptyList())
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            _searchResults.value = Resource.Loading
            _searchResults.value = userRepository.searchUsers(query)
        }
    }

    fun toggleFollow(user: User, isFollowing: Boolean) {
        viewModelScope.launch {
            userRepository.toggleFollow(user.uid, isFollowing)
        }
    }
}
