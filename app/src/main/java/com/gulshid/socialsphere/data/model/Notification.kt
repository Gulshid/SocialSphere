package com.gulshid.socialsphere.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a single notification event (like, comment, or new follower),
 * stored under "notifications/{recipientId}/items/{notificationId}".
 */
@Parcelize
data class Notification(
    var notificationId: String = "",
    val recipientId: String = "",
    val actorId: String = "",
    val actorUsername: String = "",
    val actorProfileImageUrl: String = "",
    val type: String = "",
    val postId: String = "",
    val postImageUrl: String = "",
    val commentText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) : Parcelable {
    // No-arg constructor required by Firestore deserialization
    constructor() : this("", "", "", "", "", "", "", "", "", 0L, false)
}

/** Notification.type values. */
object NotificationType {
    const val LIKE = "like"
    const val COMMENT = "comment"
    const val FOLLOW = "follow"
}
