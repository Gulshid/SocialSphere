package com.gulshid.socialsphere.ui.notifications

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gulshid.socialsphere.R
import com.gulshid.socialsphere.data.model.Notification
import com.gulshid.socialsphere.data.model.NotificationType
import com.gulshid.socialsphere.databinding.ItemNotificationBinding
import com.gulshid.socialsphere.utils.gone
import com.gulshid.socialsphere.utils.toRelativeTimeString
import com.gulshid.socialsphere.utils.visible

class NotificationAdapter(
    private val onItemClicked: (Notification) -> Unit
) : ListAdapter<Notification, NotificationAdapter.NotificationViewHolder>(NotificationDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            val actionText = when (notification.type) {
                NotificationType.LIKE -> "liked your post"
                NotificationType.COMMENT -> "commented: ${notification.commentText}"
                NotificationType.FOLLOW -> "started following you"
                else -> ""
            }
            val text = SpannableStringBuilder()
                .append(notification.actorUsername)
                .append(" ")
                .append(actionText)
            text.setSpan(StyleSpan(Typeface.BOLD), 0, notification.actorUsername.length, 0)
            binding.tvNotificationText.text = text
            binding.tvNotificationTime.text = notification.timestamp.toRelativeTimeString()

            Glide.with(binding.ivAvatar)
                .load(notification.actorProfileImageUrl)
                .placeholder(R.drawable.ic_placeholder_avatar)
                .circleCrop()
                .into(binding.ivAvatar)

            if (notification.postImageUrl.isNotBlank()) {
                binding.ivPostThumbnail.visible()
                Glide.with(binding.ivPostThumbnail)
                    .load(notification.postImageUrl)
                    .centerCrop()
                    .into(binding.ivPostThumbnail)
            } else {
                binding.ivPostThumbnail.gone()
            }

            val icon = when (notification.type) {
                NotificationType.LIKE -> R.drawable.ic_heart_filled
                NotificationType.COMMENT -> R.drawable.ic_comment
                NotificationType.FOLLOW -> R.drawable.ic_person
                else -> R.drawable.ic_notifications
            }
            binding.ivTypeIcon.setImageResource(icon)

            binding.root.setOnClickListener { onItemClicked(notification) }
        }
    }

    class NotificationDiff : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification) =
            oldItem.notificationId == newItem.notificationId
        override fun areContentsTheSame(oldItem: Notification, newItem: Notification) =
            oldItem == newItem
    }
}
