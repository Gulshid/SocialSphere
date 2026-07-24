package com.gulshid.socialsphere.ui.profile

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.gulshid.socialsphere.R
import com.gulshid.socialsphere.data.model.Post
import com.gulshid.socialsphere.data.model.User
import com.gulshid.socialsphere.databinding.ActivityUserProfileBinding
import com.gulshid.socialsphere.post.PostDetailActivity
import com.gulshid.socialsphere.utils.Resource
import com.gulshid.socialsphere.utils.gone
import com.gulshid.socialsphere.utils.toCompactCount
import com.gulshid.socialsphere.utils.toast
import com.gulshid.socialsphere.utils.visible

/**
 * Public profile screen for viewing any user (reached from the feed, search,
 * or notifications). Shows their posts grid and a working Follow/Following
 * button that reflects real follow state.
 */
class UserProfileActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_UID = "extra_uid"
    }

    private lateinit var binding: ActivityUserProfileBinding
    private val viewModel: UserProfileViewModel by viewModels()
    private lateinit var gridAdapter: ProfilePostGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = intent.getStringExtra(EXTRA_UID)
        if (uid.isNullOrBlank()) {
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        gridAdapter = ProfilePostGridAdapter { post -> openPostDetail(post) }
        binding.rvPostsGrid.layoutManager = GridLayoutManager(this, 3)
        binding.rvPostsGrid.adapter = gridAdapter

        binding.btnFollow.setOnClickListener { viewModel.toggleFollow() }

        viewModel.userState.observe(this) { state -> renderUser(state) }
        viewModel.postsState.observe(this) { state -> renderPosts(state) }
        viewModel.isFollowing.observe(this) { following -> renderFollowButton(following) }

        viewModel.load(uid)
    }

    private fun renderUser(state: Resource<User>) {
        when (state) {
            is Resource.Loading -> binding.progressBar.visible()
            is Resource.Success -> {
                binding.progressBar.gone()
                val user = state.data
                binding.toolbar.title = user.username
                binding.tvFullName.text = user.fullName
                binding.tvBio.text = user.bio.ifBlank { "No bio yet" }
                binding.tvPostsCount.text = user.postsCount.toCompactCount()
                binding.tvFollowersCount.text = user.followersCount.toCompactCount()
                binding.tvFollowingCount.text = user.followingCount.toCompactCount()

                Glide.with(binding.ivAvatar)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.ic_placeholder_avatar)
                    .into(binding.ivAvatar)

                binding.btnFollow.visibility = if (viewModel.isOwnProfile) View.GONE else View.VISIBLE
            }
            is Resource.Error -> {
                binding.progressBar.gone()
                toast(state.message)
            }
        }
    }

    private fun renderPosts(state: Resource<List<Post>>) {
        if (state is Resource.Success) {
            gridAdapter.submitList(state.data)
        }
    }

    private fun renderFollowButton(isFollowing: Boolean) {
        if (isFollowing) {
            binding.btnFollow.text = getString(R.string.following)
            val mutedColor = ContextCompat.getColor(this, R.color.on_surface_variant)
            val dividerColor = ContextCompat.getColor(this, R.color.divider)
            binding.btnFollow.setTextColor(mutedColor)
            binding.btnFollow.strokeColor = ColorStateList.valueOf(dividerColor)
        } else {
            binding.btnFollow.text = getString(R.string.follow)
            val brandColor = ContextCompat.getColor(this, R.color.brand_primary)
            binding.btnFollow.setTextColor(brandColor)
            binding.btnFollow.strokeColor = ColorStateList.valueOf(brandColor)
        }
    }

    private fun openPostDetail(post: Post) {
        val intent = Intent(this, PostDetailActivity::class.java)
        intent.putExtra(PostDetailActivity.EXTRA_POST, post)
        startActivity(intent)
    }
}
