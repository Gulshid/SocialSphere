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

    companion object {
        private const val PAGE_SIZE = 10L
    }

    private val _feedState = MutableLiveData<Resource<List<Post>>>()
    val feedState: LiveData<Resource<List<Post>>> = _feedState

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private var currentPosts: List<Post> = emptyList()
    private var isLastPage = false
    private var isBusy = false

    val currentUserId: String?
        get() = auth.currentUser?.uid

    /** Loads the first page of the feed, replacing whatever was previously loaded. */
    fun loadFeed() {
        if (isBusy) return
        isBusy = true
        isLastPage = false
        _feedState.value = Resource.Loading
        viewModelScope.launch {
            val result = postRepository.getFeed(limit = PAGE_SIZE)
            if (result is Resource.Success) {
                currentPosts = result.data
                isLastPage = result.data.size < PAGE_SIZE
            }
            _feedState.value = result
            isBusy = false
        }
    }

    /** Fetches the next page and appends it, for infinite-scroll. No-ops if already loading or at the end. */
    fun loadMore() {
        if (isBusy || isLastPage) return
        val lastTimestamp = currentPosts.lastOrNull()?.timestamp ?: return
        isBusy = true
        _isLoadingMore.value = true
        viewModelScope.launch {
            val result = postRepository.getFeed(limit = PAGE_SIZE, startAfterTimestamp = lastTimestamp)
            if (result is Resource.Success) {
                val combined = currentPosts + result.data
                currentPosts = combined
                isLastPage = result.data.size < PAGE_SIZE
                _feedState.value = Resource.Success(combined)
            }
            isBusy = false
            _isLoadingMore.value = false
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
        currentPosts = updatedList

        viewModelScope.launch {
            postRepository.toggleLike(post, isLiked)
        }

        return updatedList
    }

    /** Deletes [post] and returns the updated list with it removed, for optimistic UI update. */
    suspend fun deletePost(post: Post, currentList: List<Post>): Pair<List<Post>, Resource<Unit>> {
        val result = postRepository.deletePost(post.postId, post.authorId)
        if (result is Resource.Success) {
            val updated = currentList.filterNot { it.postId == post.postId }
            currentPosts = updated
            return updated to result
        }
        return currentList to result
    }
}