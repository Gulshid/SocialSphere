package com.gulshid.socialsphere.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gulshid.socialsphere.data.model.Post
import com.gulshid.socialsphere.data.model.User
import com.gulshid.socialsphere.data.repository.AuthRepository
import com.gulshid.socialsphere.data.repository.PostRepository
import com.gulshid.socialsphere.utils.Resource
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val postRepository: PostRepository = PostRepository()
) : ViewModel() {

    private val _userState = MutableLiveData<Resource<User>>()
    val userState: LiveData<Resource<User>> = _userState

    private val _postsState = MutableLiveData<Resource<List<Post>>>()
    val postsState: LiveData<Resource<List<Post>>> = _postsState

    fun loadProfile() {
        _userState.value = Resource.Loading
        viewModelScope.launch {
            val result = authRepository.getCurrentUser()
            _userState.value = result
            if (result is Resource.Success) {
                loadPosts(result.data.uid)
            }
        }
    }

    private fun loadPosts(uid: String) {
        _postsState.value = Resource.Loading
        viewModelScope.launch {
            _postsState.value = postRepository.getPostsByUser(uid)
        }
    }

    fun logout() = authRepository.logout()
}
