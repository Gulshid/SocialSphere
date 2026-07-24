package com.gulshid.socialsphere.post

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.gulshid.socialsphere.databinding.ActivityCreatePostBinding
import com.gulshid.socialsphere.utils.Resource
import com.gulshid.socialsphere.utils.gone
import com.gulshid.socialsphere.utils.toast
import com.gulshid.socialsphere.utils.visible

/**
 * Lets the user pick an image from the gallery, add a caption, and publish
 * it as a new post. Image cropping (e.g. via uCrop) can be layered on top
 * of the picked URI before it's passed to the ViewModel if a fixed aspect
 * ratio is desired.
 */
class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private val viewModel: CreatePostViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivSelectedImage.visible()
            binding.imagePlaceholder.gone()
            Glide.with(this).load(it).centerCrop().into(binding.ivSelectedImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnPickImage.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.ivSelectedImage.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.imagePlaceholder.setOnClickListener { pickImageLauncher.launch("image/*") }

        binding.btnShare.setOnClickListener { sharePost() }

        viewModel.uploadState.observe(this) { state -> render(state) }
    }

    private fun sharePost() {
        val uri = selectedImageUri
        if (uri == null) {
            toast("Please select an image first")
            return
        }
        val caption = binding.etCaption.text.toString().trim()
        viewModel.sharePost(uri, caption)
    }

    private fun render(state: Resource<Unit>) {
        when (state) {
            is Resource.Loading -> {
                binding.progressBar.visible()
                binding.btnShare.isEnabled = false
            }
            is Resource.Success -> {
                binding.progressBar.gone()
                toast("Post shared successfully!")
                finish()
            }
            is Resource.Error -> {
                binding.progressBar.gone()
                binding.btnShare.isEnabled = true
                toast(state.message)
            }
        }
    }
}
