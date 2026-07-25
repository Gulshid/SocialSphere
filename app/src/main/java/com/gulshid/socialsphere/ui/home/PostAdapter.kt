package com.gulshid.socialsphere.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gulshid.socialsphere.R
import com.gulshid.socialsphere.data.model.Post
import com.gulshid.socialsphere.databinding.ItemPostBinding
import com.gulshid.socialsphere.utils.toCompactCount
import com.gulshid.socialsphere.utils.toRelativeTimeString

/**
 * Renders the home feed. Each post exposes tap targets for like, comment,
 * share and opening the post detail screen, wired via [PostActionListener].
 */
class PostAdapter(
    private val currentUserId: String?,
    private val listener: PostActionListener
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    interface PostActionListener {
        fun onLikeClicked(post: Post, position: Int)
        fun onCommentClicked(post: Post)
        fun onShareClicked(post: Post)
        fun onPostClicked(post: Post)
        fun onAuthorClicked(post: Post)
        fun onMoreClicked(post: Post, anchor: android.view.View, position: Int)
    }

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
            binding.tvUsername.text = post.authorUsername
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
                .into(binding.ivPostImage)

            Glide.with(binding.ivAvatar)
                .load(post.authorProfileImageUrl)
                .placeholder(R.drawable.ic_placeholder_avatar)
                .circleCrop()
                .into(binding.ivAvatar)

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
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.postId == newItem.postId
        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }
}
