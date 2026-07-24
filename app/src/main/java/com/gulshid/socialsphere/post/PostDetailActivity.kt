package com.example.socialsphere.post

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialsphere.data.model.Post
import com.example.socialsphere.databinding.ActivityPostDetailBinding
import com.example.socialsphere.utils.Resource
import com.example.socialsphere.utils.gone
import com.example.socialsphere.utils.toast
import com.example.socialsphere.utils.visible

/**
 * Shows a post's comment thread and lets the user add new comments.
 * Expects a [Post] Parcelable passed via [EXTRA_POST].
 */
class PostDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST = "extra_post"
    }

    private lateinit var binding: ActivityPostDetailBinding
    private val viewModel: PostDetailViewModel by viewModels()
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var post: Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        post = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_POST, Post::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_POST)
        } ?: run {
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        commentAdapter = CommentAdapter()
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentAdapter

        binding.ivSendComment.setOnClickListener { submitComment() }

        viewModel.commentsState.observe(this) { state -> renderComments(state) }
        viewModel.loadComments(post.postId)
    }

    private fun submitComment() {
        val text = binding.etComment.text.toString().trim()
        if (text.isBlank()) return
        binding.etComment.text?.clear()
        viewModel.addComment(post.postId, text)
    }

    private fun renderComments(state: Resource<List<com.example.socialsphere.data.model.Comment>>) {
        when (state) {
            is Resource.Loading -> Unit
            is Resource.Success -> {
                commentAdapter.submitList(state.data)
                binding.tvEmptyComments.visibility =
                    if (state.data.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
            is Resource.Error -> toast(state.message)
        }
    }
}
