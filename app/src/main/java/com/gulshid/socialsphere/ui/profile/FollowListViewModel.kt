package com.gulshid.socialsphere.ui.profile


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gulshid.socialsphere.data.repository.UserRepository
import com.gulshid.socialsphere.ui.search.FollowableUser
import com.gulshid.socialsphere.utils.Resource
import kotlinx.coroutines.launch

enum class FollowListType { FOLLOWERS, FOLLOWING }

/**
 * Backs [FollowListActivity]. Loads either the followers or following list
 * for a given profile, pairing each result with whether the *current* signed
 * in user follows them (same FollowableUser/UserAdapter plumbing as search),
 * so the Follow/Following button works live from this screen too.
 */
class FollowListViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _usersState = MutableLiveData<Resource<List<FollowableUser>>>()
    val usersState: LiveData<Resource<List<FollowableUser>>> = _usersState

    private var latestResults: List<FollowableUser> = emptyList()

    fun load(uid: String, type: FollowListType) {
        _usersState.value = Resource.Loading
        viewModelScope.launch {
            val result = when (type) {
                FollowListType.FOLLOWERS -> userRepository.getFollowers(uid)
                FollowListType.FOLLOWING -> userRepository.getFollowing(uid)
            }
            when (result) {
                is Resource.Success -> {
                    val withFollowStatus = result.data.map { user ->
                        FollowableUser(user, userRepository.isFollowing(user.uid))
                    }
                    latestResults = withFollowStatus
                    _usersState.value = Resource.Success(withFollowStatus)
                }
                is Resource.Error -> _usersState.value = Resource.Error(result.message)
                Resource.Loading -> Unit
            }
        }
    }

    /** Toggles follow state optimistically for a listed user, reverting if the write fails. */
    fun toggleFollow(item: FollowableUser) {
        latestResults = latestResults.map {
            if (it.user.uid == item.user.uid) it.copy(isFollowing = !it.isFollowing) else it
        }
        _usersState.value = Resource.Success(latestResults)

        viewModelScope.launch {
            val result = userRepository.toggleFollow(item.user.uid, item.isFollowing)
            if (result is Resource.Error) {
                latestResults = latestResults.map {
                    if (it.user.uid == item.user.uid) it.copy(isFollowing = item.isFollowing) else it
                }
                _usersState.value = Resource.Success(latestResults)
            }
        }
    }
}