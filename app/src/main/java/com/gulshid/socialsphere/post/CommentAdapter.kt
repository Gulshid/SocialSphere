package com.example.socialsphere.post

import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialsphere.R
import com.example.socialsphere.data.model.Comment
import com.example.socialsphere.databinding.ItemCommentBinding
import com.example.socialsphere.utils.toRelativeTimeString

class CommentAdapter : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            val text = SpannableStringBuilder()
                .append(comment.authorUsername)
                .append("   ")
                .append(comment.text)
            text.setSpan(
                StyleSpan(Typeface.BOLD), 0, comment.authorUsername.length, 0
            )
            binding.tvCommentText.text = text
            binding.tvCommentTime.text = comment.timestamp.toRelativeTimeString()

            Glide.with(binding.ivAvatar)
                .load(comment.authorProfileImageUrl)
                .placeholder(R.drawable.ic_placeholder_avatar)
                .circleCrop()
                .into(binding.ivAvatar)
        }
    }

    class CommentDiff : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment) = oldItem.commentId == newItem.commentId
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment) = oldItem == newItem
    }
}
