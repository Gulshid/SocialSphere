package com.gulshid.socialsphere.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gulshid.socialsphere.data.model.Comment
import com.gulshid.socialsphere.data.repository.PostRepository
import com.gulshid.socialsphere.utils.Resource
import kotlinx.coroutines.launch

class PostDetailViewModel(
    private val postRepository: PostRepository = PostRepository()
) : ViewModel() {

    private val _commentsState = MutableLiveData<Resource<List<Comment>>>()
    val commentsState: LiveData<Resource<List<Comment>>> = _commentsState

    private val _postCommentState = MutableLiveData<Resource<Unit>>()
    val postCommentState: LiveData<Resource<Unit>> = _postCommentState

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
}
