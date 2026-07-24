package com.gulshid.socialsphere.ui.search

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gulshid.socialsphere.R
import com.gulshid.socialsphere.data.model.User
import com.gulshid.socialsphere.databinding.ItemUserBinding

class UserAdapter(
    private val onUserClicked: (User) -> Unit,
    private val onFollowClicked: (FollowableUser) -> Unit
) : ListAdapter<FollowableUser, UserAdapter.UserViewHolder>(UserDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FollowableUser) {
            val user = item.user
            val context = binding.root.context

            binding.tvUsername.text = user.username
            binding.tvFullName.text = user.fullName

            Glide.with(binding.ivAvatar)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.ic_placeholder_avatar)
                .circleCrop()
                .into(binding.ivAvatar)

            if (item.isFollowing) {
                binding.btnFollow.text = context.getString(R.string.following)
                val mutedColor = ContextCompat.getColor(context, R.color.on_surface_variant)
                val dividerColor = ContextCompat.getColor(context, R.color.divider)
                binding.btnFollow.setTextColor(mutedColor)
                binding.btnFollow.strokeColor = ColorStateList.valueOf(dividerColor)
            } else {
                binding.btnFollow.text = context.getString(R.string.follow)
                val brandColor = ContextCompat.getColor(context, R.color.brand_primary)
                binding.btnFollow.setTextColor(brandColor)
                binding.btnFollow.strokeColor = ColorStateList.valueOf(brandColor)
            }

            binding.root.setOnClickListener { onUserClicked(user) }
            binding.btnFollow.setOnClickListener { onFollowClicked(item) }
        }
    }

    class UserDiff : DiffUtil.ItemCallback<FollowableUser>() {
        override fun areItemsTheSame(oldItem: FollowableUser, newItem: FollowableUser) =
            oldItem.user.uid == newItem.user.uid
        override fun areContentsTheSame(oldItem: FollowableUser, newItem: FollowableUser) =
            oldItem == newItem
    }
}
