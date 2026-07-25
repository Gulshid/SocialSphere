package com.gulshid.socialsphere.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.gulshid.socialsphere.R
import com.gulshid.socialsphere.auth.LoginActivity
import com.gulshid.socialsphere.data.model.Post
import com.gulshid.socialsphere.data.model.User
import com.gulshid.socialsphere.databinding.FragmentProfileBinding
import com.gulshid.socialsphere.post.PostDetailActivity
import com.gulshid.socialsphere.utils.Resource
import com.gulshid.socialsphere.utils.gone
import com.gulshid.socialsphere.utils.toCompactCount
import com.gulshid.socialsphere.utils.toast
import com.gulshid.socialsphere.utils.visible

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var gridAdapter: ProfilePostGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gridAdapter = ProfilePostGridAdapter { post -> openPostDetail(post) }
        binding.rvPostsGrid.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvPostsGrid.adapter = gridAdapter

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.followersColumn.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                FollowListActivity.start(requireContext(), uid, FollowListType.FOLLOWERS)
            }
        }
        binding.followingColumn.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                FollowListActivity.start(requireContext(), uid, FollowListType.FOLLOWING)
            }
        }

        binding.ivLogout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finishAffinity()
        }

        viewModel.userState.observe(viewLifecycleOwner) { state -> renderUser(state) }
        viewModel.postsState.observe(viewLifecycleOwner) { state -> renderPosts(state) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProfile()
    }

    private fun renderUser(state: Resource<User>) {
        when (state) {
            is Resource.Loading -> binding.progressBar.visible()
            is Resource.Success -> {
                binding.progressBar.gone()
                val user = state.data
                binding.tvFullName.text = user.fullName
                binding.tvBio.text = user.bio.ifBlank { "No bio yet" }
                binding.tvPostsCount.text = user.postsCount.toCompactCount()
                binding.tvFollowersCount.text = user.followersCount.toCompactCount()
                binding.tvFollowingCount.text = user.followingCount.toCompactCount()

                Glide.with(binding.ivAvatar)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.ic_placeholder_avatar)
                    .into(binding.ivAvatar)
            }
            is Resource.Error -> {
                binding.progressBar.gone()
                requireContext().toast(state.message)
            }
        }
    }

    private fun renderPosts(state: Resource<List<Post>>) {
        if (state is Resource.Success) {
            gridAdapter.submitList(state.data)
            binding.emptyState.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun openPostDetail(post: Post) {
        val intent = Intent(requireContext(), PostDetailActivity::class.java)
        intent.putExtra(PostDetailActivity.EXTRA_POST, post)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}