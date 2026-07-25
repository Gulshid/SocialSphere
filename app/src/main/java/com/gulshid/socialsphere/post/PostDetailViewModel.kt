package com.gulshid.socialsphere.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.gulshid.socialsphere.data.model.Comment
import com.gulshid.socialsphere.data.model.Post
import com.gulshid.socialsphere.data.repository.PostRepository
import com.gulshid.socialsphere.utils.Resource
import kotlinx.coroutines.launch

class PostDetailViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _commentsState = MutableLiveData<Resource<List<Comment>>>()
    val commentsState: LiveData<Resource<List<Comment>>> = _commentsState

    private val _postCommentState = MutableLiveData<Resource<Unit>>()
    val postCommentState: LiveData<Resource<Unit>> = _postCommentState

    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun setInitialPost(post: Post) {
        _post.value = post
    }

    fun loadComments(postId: String) {
        _commentsState.value = Resource.Loading
        viewModelScope.launch {
            _commentsState.value = postRepository.getComments(postId)
        }
    }

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            _postCommentState.value = postRepository.addComment(postId, text)
            loadComments(postId)
        }
    }

    /** Toggles the like on the currently displayed post, updating [post] optimistically. */
    fun toggleLike() {
        val current = _post.value ?: return
        val uid = currentUserId ?: return
        val isLiked = current.isLikedBy(uid)
        val updated = current.copy(
            likedBy = if (isLiked) current.likedBy - uid else current.likedBy + uid,
            likeCount = if (isLiked) current.likeCount - 1 else current.likeCount + 1
        )
        _post.value = updated
        viewModelScope.launch {
            postRepository.toggleLike(current, isLiked)
        }
    }
}
