package com.gulshid.socialsphere.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gulshid.socialsphere.data.model.Post
import com.gulshid.socialsphere.databinding.ItemGridPostBinding

class ProfilePostGridAdapter(
    private val onPostClicked: (Post) -> Unit
) : ListAdapter<Post, ProfilePostGridAdapter.GridViewHolder>(GridDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val binding = ItemGridPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GridViewHolder(private val binding: ItemGridPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            Glide.with(binding.ivGridImage).load(post.imageUrl).centerCrop().into(binding.ivGridImage)
            binding.root.setOnClickListener { onPostClicked(post) }
        }
    }

    class GridDiff : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.postId == newItem.postId
        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }
}
