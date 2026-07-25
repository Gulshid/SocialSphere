package com.gulshid.socialsphere.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gulshid.socialsphere.R
import com.gulshid.socialsphere.data.model.Post
import com.gulshid.socialsphere.data.repository.UserRepository
import com.gulshid.socialsphere.databinding.ItemPostBinding
import com.gulshid.socialsphere.utils.Resource
import com.gulshid.socialsphere.utils.toCompactCount
import com.gulshid.socialsphere.utils.toRelativeTimeString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Renders the home feed. Each post exposes tap targets for like, comment,
 * share and opening the post detail screen, wired via [PostActionListener].
 *
 * Posts store a *snapshot* of the author's username/avatar taken at the
 * moment the post was created (see PostRepository.createPost). That snapshot
 * goes stale the instant the author edits their profile, and is simply blank
 * for posts made before the author ever set an avatar. To avoid showing
 * missing/outdated avatars, this adapter treats the snapshot only as an
 * instant placeholder and then fetches the author's *current* profile via
 * [UserRepository], caching the result per author so a feed with many posts
 * from the same person only fetches once.
 */
class PostAdapter(
    private val currentUserId: String?,
    private val listener: PostActionListener,
    private val adapterScope: CoroutineScope,
    private val userRepository: UserRepository = UserRepository()
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    interface PostActionListener {
        fun onLikeClicked(post: Post, position: Int)
        fun onCommentClicked(post: Post)
        fun onShareClicked(post: Post)
        fun onPostClicked(post: Post)
        fun onAuthorClicked(post: Post)
        fun onMoreClicked(post: Post, anchor: android.view.View, position: Int)
    }

    /** In-memory cache of author id -> (username, profileImageUrl), shared across binds. */
    private data class AuthorInfo(val username: String, val profileImageUrl: String)
    private val authorCache = mutableMapOf<String, AuthorInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.tvTimestamp.text = post.timestamp.toRelativeTimeString()
            binding.tvCaption.text = post.caption
            binding.tvLikeCount.text = itemView.context.getString(
                R.string.like
            ).let { "${post.likeCount.toCompactCount()} likes" }
            binding.tvViewComments.text = if (post.commentCount > 0) {
                "View all ${post.commentCount} comments"
            } else {
                "Be the first to comment"
            }

            Glide.with(binding.ivPostImage)
                .load(post.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.bg_card_rounded)
                .error(R.drawable.bg_card_rounded)
                .into(binding.ivPostImage)

            // Show the snapshot stored on the post immediately so there's no
            // blank flash while the live lookup below resolves.
            bindAuthorInfo(post.authorUsername, post.authorProfileImageUrl)

            // Then refresh with the author's current username/avatar, using
            // the cache to avoid a Firestore read for every post by the same
            // author in the feed.
            val cached = authorCache[post.authorId]
            if (cached != null) {
                bindAuthorInfo(cached.username, cached.profileImageUrl)
            } else {
                adapterScope.launch {
                    val result = userRepository.getUser(post.authorId)
                    if (result is Resource.Success) {
                        val info = AuthorInfo(
                            username = result.data.username.ifBlank { post.authorUsername },
                            profileImageUrl = result.data.profileImageUrl
                        )
                        authorCache[post.authorId] = info
                        // Guard against the ViewHolder having been recycled
                        // for a different item while the fetch was in flight.
                        if (bindingAdapterPosition != RecyclerView.NO_POSITION &&
                            getItem(bindingAdapterPosition).authorId == post.authorId
                        ) {
                            bindAuthorInfo(info.username, info.profileImageUrl)
                        }
                    }
                }
            }

            val isLiked = currentUserId != null && post.isLikedBy(currentUserId)
            binding.ivLike.setImageResource(
                if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )

            binding.ivLike.setOnClickListener { listener.onLikeClicked(post, bindingAdapterPosition) }
            binding.ivComment.setOnClickListener { listener.onCommentClicked(post) }
            binding.ivShare.setOnClickListener { listener.onShareClicked(post) }
            binding.ivPostImage.setOnClickListener { listener.onPostClicked(post) }
            binding.tvViewComments.setOnClickListener { listener.onCommentClicked(post) }
            binding.ivAvatar.setOnClickListener { listener.onAuthorClicked(post) }
            binding.tvUsername.setOnClickListener { listener.onAuthorClicked(post) }
            binding.ivMore.setOnClickListener { listener.onMoreClicked(post, binding.ivMore, bindingAdapterPosition) }
        }

        private fun bindAuthorInfo(username: String, profileImageUrl: String) {
            binding.tvUsername.text = username.ifBlank { "Unknown user" }

            Glide.with(binding.ivAvatar)
                .load(profileImageUrl)
                .placeholder(R.drawable.ic_placeholder_avatar)
                .error(R.drawable.ic_placeholder_avatar)
                .circleCrop()
                .into(binding.ivAvatar)
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.postId == newItem.postId
        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }
}