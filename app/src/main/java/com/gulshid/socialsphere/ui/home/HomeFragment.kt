package com.gulshid.socialsphere.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.gulshid.socialsphere.data.model.Post
import com.gulshid.socialsphere.databinding.FragmentHomeBinding
import com.gulshid.socialsphere.post.PostDetailActivity
import com.gulshid.socialsphere.ui.profile.UserProfileActivity
import com.gulshid.socialsphere.utils.Resource
import com.gulshid.socialsphere.utils.gone
import com.gulshid.socialsphere.utils.toast
import com.gulshid.socialsphere.utils.visible
import kotlinx.coroutines.launch

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

        adapter = PostAdapter(viewModel.currentUserId, this, viewLifecycleOwner.lifecycleScope)
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadFeed() }

        binding.rvFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                if (visibleItemCount + firstVisiblePosition >= totalItemCount - 3) {
                    viewModel.loadMore()
                }
            }
        })

        viewModel.isLoadingMore.observe(viewLifecycleOwner) { loading ->
            binding.progressBarLoadMore.visibility = if (loading) View.VISIBLE else View.GONE
        }

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
        val intent = Intent(requireContext(), UserProfileActivity::class.java)
        intent.putExtra(UserProfileActivity.EXTRA_UID, post.authorId)
        startActivity(intent)
    }

    override fun onMoreClicked(post: Post, anchor: View, position: Int) {
        val isOwnPost = post.authorId == FirebaseAuth.getInstance().currentUser?.uid
        val popup = PopupMenu(requireContext(), anchor)
        if (isOwnPost) {
            popup.menu.add("Delete post")
        } else {
            popup.menu.add("Report post")
        }
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Delete post" -> confirmDeletePost(post)
                "Report post" -> requireContext().toast("Post reported. Thanks for letting us know.")
            }
            true
        }
        popup.show()
    }

    private fun confirmDeletePost(post: Post) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete post?")
            .setMessage("This can't be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val (updatedList, result) = viewModel.deletePost(post, adapter.currentList)
                    if (result is Resource.Success) {
                        adapter.submitList(updatedList)
                        requireContext().toast("Post deleted.")
                    } else if (result is Resource.Error) {
                        requireContext().toast(result.message)
                    }
                }
            }
            .show()
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