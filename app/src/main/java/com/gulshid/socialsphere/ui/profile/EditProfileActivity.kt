package com.gulshid.socialsphere.ui.profile

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.gulshid.socialsphere.R
import com.gulshid.socialsphere.data.model.User
import com.gulshid.socialsphere.databinding.ActivityEditProfileBinding
import com.gulshid.socialsphere.utils.Resource
import com.gulshid.socialsphere.utils.gone
import com.gulshid.socialsphere.utils.toast
import com.gulshid.socialsphere.utils.visible

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModels()
    private var newImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            newImageUri = it
            Glide.with(this).load(it).circleCrop().into(binding.ivAvatar)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.ivEditIcon.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.ivAvatar.setOnClickListener { pickImageLauncher.launch("image/*") }

        binding.btnSave.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val bio = binding.etBio.text.toString().trim()
            viewModel.save(fullName, bio, newImageUri)
        }

        viewModel.userState.observe(this) { state -> renderUser(state) }
        viewModel.saveState.observe(this) { state -> renderSave(state) }
        viewModel.loadCurrentUser()
    }

    private fun renderUser(state: Resource<User>) {
        if (state is Resource.Success) {
            binding.etFullName.setText(state.data.fullName)
            binding.etBio.setText(state.data.bio)
            Glide.with(this)
                .load(state.data.profileImageUrl)
                .placeholder(R.drawable.ic_placeholder_avatar)
                .circleCrop()
                .into(binding.ivAvatar)
        } else if (state is Resource.Error) {
            toast(state.message)
        }
    }

    private fun renderSave(state: Resource<Unit>) {
        when (state) {
            is Resource.Loading -> {
                binding.progressBar.visible()
                binding.btnSave.isEnabled = false
            }
            is Resource.Success -> {
                binding.progressBar.gone()
                toast("Profile updated")
                finish()
            }
            is Resource.Error -> {
                binding.progressBar.gone()
                binding.btnSave.isEnabled = true
                toast(state.message)
            }
        }
    }
}
