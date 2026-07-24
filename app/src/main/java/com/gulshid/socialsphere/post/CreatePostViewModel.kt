package com.gulshid.socialsphere.post

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gulshid.socialsphere.data.repository.PostRepository
import com.gulshid.socialsphere.utils.Resource
import kotlinx.coroutines.launch

class CreatePostViewModel(
    private val postRepository: PostRepository = PostRepository()
) : ViewModel() {

    private val _uploadState = MutableLiveData<Resource<Unit>>()
    val uploadState: LiveData<Resource<Unit>> = _uploadState

    fun sharePost(imageUri: Uri, caption: String) {
        _uploadState.value = Resource.Loading
        viewModelScope.launch {
            _uploadState.value = postRepository.createPost(imageUri, caption)
        }
    }
}
