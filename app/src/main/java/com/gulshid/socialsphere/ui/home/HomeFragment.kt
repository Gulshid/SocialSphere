package com.example.socialsphere.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialsphere.data.model.Post
import com.example.socialsphere.databinding.FragmentHomeBinding
import com.example.socialsphere.post.PostDetailActivity
import com.example.socialsphere.utils.Resource
import com.example.socialsphere.utils.gone
import com.example.socialsphere.utils.toast
import com.example.socialsphere.utils.visible

class HomeFragment : Fragment(), PostAdapter.PostActionListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PostAdapter(viewModel.currentUserId, this)
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadFeed() }

        viewModel.feedState.observe(viewLifecycleOwner) { state -> render(state) }
        viewModel.loadFeed()
    }

    private fun render(state: Resource<List<Post>>) {
        when (state) {
            is Resource.Loading -> {
                binding.progressBar.visible()
                binding.emptyState.gone()
            }
            is Resource.Success -> {
                binding.progressBar.gone()
                binding.swipeRefresh.isRefreshing = false
                adapter.submitList(state.data)
                binding.emptyState.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
            }
            is Resource.Error -> {
                binding.progressBar.gone()
                binding.swipeRefresh.isRefreshing = false
                requireContext().toast(state.message)
            }
        }
    }

    override fun onLikeClicked(post: Post, position: Int) {
        val currentList = adapter.currentList
        val updated = viewModel.toggleLike(post, position, currentList)
        adapter.submitList(updated)
    }

    override fun onCommentClicked(post: Post) {
        openPostDetail(post)
    }

    override fun onShareClicked(post: Post) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this post on SocialSphere: ${post.imageUrl}")
        }
        startActivity(Intent.createChooser(shareIntent, "Share post via"))
    }

    override fun onPostClicked(post: Post) {
        openPostDetail(post)
    }

    override fun onAuthorClicked(post: Post) {
        // TODO: Navigate to the author's public profile screen
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
