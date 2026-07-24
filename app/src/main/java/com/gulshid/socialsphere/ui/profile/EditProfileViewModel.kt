package com.gulshid.socialsphere.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gulshid.socialsphere.data.model.User
import com.gulshid.socialsphere.data.repository.AuthRepository
import com.gulshid.socialsphere.data.repository.UserRepository
import com.gulshid.socialsphere.utils.Resource
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _userState = MutableLiveData<Resource<User>>()
    val userState: LiveData<Resource<User>> = _userState

    private val _saveState = MutableLiveData<Resource<Unit>>()
    val saveState: LiveData<Resource<Unit>> = _saveState

    fun loadCurrentUser() {
        _userState.value = Resource.Loading
        viewModelScope.launch {
            _userState.value = authRepository.getCurrentUser()
        }
    }

    fun save(fullName: String, bio: String, newImageUri: Uri?) {
        _saveState.value = Resource.Loading
        viewModelScope.launch {
            _saveState.value = userRepository.updateProfile(fullName, bio, newImageUri)
        }
    }
}
