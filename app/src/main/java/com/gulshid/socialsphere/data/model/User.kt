package com.example.socialsphere.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a user profile stored in Firestore under "users/{uid}".
 */
@Parcelize
data class User(
    val uid: String = "",
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    // No-arg constructor required by Firestore deserialization
    constructor() : this("", "", "", "", "", "", 0, 0, 0, 0L)
}
