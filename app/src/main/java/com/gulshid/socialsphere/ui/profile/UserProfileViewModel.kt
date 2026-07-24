package com.gulshid.socialsphere.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.gulshid.socialsphere.data.model.Post
import com.gulshid.socialsphere.data.model.User
import com.gulshid.socialsphere.data.repository.PostRepository
import com.gulshid.socialsphere.data.repository.UserRepository
import com.gulshid.socialsphere.utils.Resource
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val postRepository: PostRepository = PostRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _userState = MutableLiveData<Resource<User>>()
    val userState: LiveData<Resource<User>> = _userState

    private val _postsState = MutableLiveData<Resource<List<Post>>>()
    val postsState: LiveData<Resource<List<Post>>> = _postsState

    private val _isFollowing = MutableLiveData<Boolean>()
    val isFollowing: LiveData<Boolean> = _isFollowing

    private var targetUid: String = ""

    val isOwnProfile: Boolean
        get() = targetUid.isNotBlank() && targetUid == auth.currentUser?.uid

    fun load(uid: String) {
        targetUid = uid
        _userState.value = Resource.Loading
        viewModelScope.launch {
            _userState.value = userRepository.getUser(uid)
            _postsState.value = postRepository.getPostsByUser(uid)
            if (!isOwnProfile) {
                _isFollowing.value = userRepository.isFollowing(uid)
            }
        }
    }

    /** Toggles follow state optimistically, reverting if the write fails. */
    fun toggleFollow() {
        val wasFollowing = _isFollowing.value ?: false
        _isFollowing.value = !wasFollowing
        viewModelScope.launch {
            val result = userRepository.toggleFollow(targetUid, wasFollowing)
            if (result is Resource.Error) {
                _isFollowing.value = wasFollowing
            }
        }
    }
}
