package com.gulshid.socialsphere.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a single post stored in Firestore under "posts/{postId}".
 */
@Parcelize
data class Post(
    var postId: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorProfileImageUrl: String = "",
    val caption: String = "",
    val imageUrl: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    constructor() : this("", "", "", "", "", "", 0, 0, emptyList(), 0L)

    fun isLikedBy(uid: String): Boolean = likedBy.contains(uid)
}
