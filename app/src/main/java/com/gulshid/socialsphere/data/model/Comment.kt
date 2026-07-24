package com.example.socialsphere.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a comment on a post, stored under "posts/{postId}/comments/{commentId}".
 */
@Parcelize
data class Comment(
    var commentId: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorProfileImageUrl: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    constructor() : this("", "", "", "", "", 0L)
}
