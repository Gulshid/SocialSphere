package com.gulshid.socialsphere.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gulshid.socialsphere.data.model.Post
import com.gulshid.socialsphere.data.repository.PostRepository
import com.gulshid.socialsphere.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomeViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _feedState = MutableLiveData<Resource<List<Post>>>()
    val feedState: LiveData<Resource<List<Post>>> = _feedState

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun loadFeed() {
        _feedState.value = Resource.Loading
        viewModelScope.launch {
            _feedState.value = postRepository.getFeed()
        }
    }

    /**
     * Toggles a like optimistically in the UI list and persists the change.
     * Returns the updated list immediately so the adapter can refresh without
     * waiting on the network round trip.
     */
    fun toggleLike(post: Post, position: Int, currentList: List<Post>): List<Post> {
        val uid = currentUserId ?: return currentList
        val isLiked = post.isLikedBy(uid)

        val updatedPost = post.copy(
            likedBy = if (isLiked) post.likedBy - uid else post.likedBy + uid,
            likeCount = if (isLiked) post.likeCount - 1 else post.likeCount + 1
        )

        val updatedList = currentList.toMutableList().apply {
            if (position in indices) this[position] = updatedPost
        }

        viewModelScope.launch {
            postRepository.toggleLike(post.postId, isLiked)
        }

        return updatedList
    }
}
