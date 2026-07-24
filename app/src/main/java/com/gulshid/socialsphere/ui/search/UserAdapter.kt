package com.example.socialsphere.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialsphere.R
import com.example.socialsphere.data.model.User
import com.example.socialsphere.databinding.ItemUserBinding

class UserAdapter(
    private val onUserClicked: (User) -> Unit,
    private val onFollowClicked: (User) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvUsername.text = user.username
            binding.tvFullName.text = user.fullName

            Glide.with(binding.ivAvatar)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.ic_placeholder_avatar)
                .circleCrop()
                .into(binding.ivAvatar)

            binding.root.setOnClickListener { onUserClicked(user) }
            binding.btnFollow.setOnClickListener { onFollowClicked(user) }
        }
    }

    class UserDiff : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.uid == newItem.uid
        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
    }
}
