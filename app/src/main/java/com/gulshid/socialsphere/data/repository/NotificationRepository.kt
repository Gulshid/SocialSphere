package com.gulshid.socialsphere.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gulshid.socialsphere.data.model.Notification
import com.gulshid.socialsphere.utils.Resource
import kotlinx.coroutines.tasks.await

/**
 * Handles reading and writing notification events. Notifications are stored
 * per-recipient at "notifications/{recipientId}/items/{notificationId}" so a
 * user's own notification list is a cheap, directly-queryable subcollection.
 */
class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun itemsCollection(recipientId: String) =
        firestore.collection("notifications").document(recipientId).collection("items")

    /**
     * Writes a notification for [recipientId]. Silently no-ops if the actor
     * would be notifying themselves, and swallows failures (e.g. missing
     * Firestore rules for this path) so a failed notification write never
     * blocks the like/comment/follow action that triggered it.
     */
    suspend fun createNotification(
        recipientId: String,
        type: String,
        postId: String = "",
        postImageUrl: String = "",
        commentText: String = ""
    ) {
        val actor = auth.currentUser ?: return
        if (recipientId.isBlank() || recipientId == actor.uid) return
        try {
            val actorSnapshot = firestore.collection("users").document(actor.uid).get().await()
            val actorUsername = actorSnapshot.getString("username") ?: "Someone"
            val actorProfileImageUrl = actorSnapshot.getString("profileImageUrl") ?: ""

            val docRef = itemsCollection(recipientId).document()
            val notification = Notification(
                notificationId = docRef.id,
                recipientId = recipientId,
                actorId = actor.uid,
                actorUsername = actorUsername,
                actorProfileImageUrl = actorProfileImageUrl,
                type = type,
                postId = postId,
                postImageUrl = postImageUrl,
                commentText = commentText
            )
            docRef.set(notification).await()
        } catch (e: Exception) {
            // Best-effort — see kdoc above.
        }
    }

    suspend fun getNotifications(limit: Long = 50): Resource<List<Notification>> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Not authenticated.")
        return try {
            val snapshot = itemsCollection(uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            Resource.Success(snapshot.toObjects(Notification::class.java))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load notifications.")
        }
    }
}
