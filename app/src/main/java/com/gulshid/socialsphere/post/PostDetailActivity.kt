package com.gulshid.socialsphere.post

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gulshid.socialsphere.R
import com.gulshid.socialsphere.data.model.Post
import com.gulshid.socialsphere.databinding.ActivityPostDetailBinding
import com.gulshid.socialsphere.utils.Resource
import com.gulshid.socialsphere.utils.gone
import com.gulshid.socialsphere.utils.toCompactCount
import com.gulshid.socialsphere.utils.toRelativeTimeString
import com.gulshid.socialsphere.utils.toast
import com.gulshid.socialsphere.utils.visible

/**
 * Shows a single post — image, caption and like button — with its full
 * comment thread below, and lets the user add new comments.
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
        viewModel.setInitialPost(post)

        commentAdapter = CommentAdapter()
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentAdapter

        binding.ivSendComment.setOnClickListener { submitComment() }
        binding.ivLike.setOnClickListener { viewModel.toggleLike() }
        binding.ivShare.setOnClickListener { sharePost() }

        viewModel.post.observe(this) { renderPost(it) }
        viewModel.commentsState.observe(this) { state -> renderComments(state) }
        viewModel.loadComments(post.postId)
    }

    private fun renderPost(post: Post) {
        binding.tvUsername.text = post.authorUsername
        binding.tvTimestamp.text = post.timestamp.toRelativeTimeString()
        binding.tvCaption.text = post.caption
        binding.tvLikeCount.text = "${post.likeCount.toCompactCount()} likes"

        val isLiked = viewModel.currentUserId != null && post.isLikedBy(viewModel.currentUserId!!)
        binding.ivLike.setImageResource(
            if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )

        Glide.with(binding.ivPostImage).load(post.imageUrl).centerCrop().into(binding.ivPostImage)
        Glide.with(binding.ivAvatar)
            .load(post.authorProfileImageUrl)
            .placeholder(R.drawable.ic_placeholder_avatar)
            .circleCrop()
            .into(binding.ivAvatar)
    }

    private fun sharePost() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this post on SocialSphere: ${post.imageUrl}")
        }
        startActivity(Intent.createChooser(shareIntent, "Share post via"))
    }

    private fun submitComment() {
        val text = binding.etComment.text.toString().trim()
        if (text.isBlank()) return
        binding.etComment.text?.clear()
        viewModel.addComment(post.postId, text)
    }

    private fun renderComments(state: Resource<List<com.gulshid.socialsphere.data.model.Comment>>) {
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
