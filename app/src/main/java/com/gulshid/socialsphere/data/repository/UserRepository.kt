package com.gulshid.socialsphere.data.repository

import android.net.Uri
import com.gulshid.socialsphere.data.model.NotificationType
import com.gulshid.socialsphere.data.model.User
import com.gulshid.socialsphere.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.gulshid.socialsphere.utils.CloudinaryUploader
import kotlinx.coroutines.tasks.await

/**
 * Handles user profile reads/updates and the follow/unfollow relationship graph.
 * Follow edges are stored as sub-collections: users/{uid}/following/{targetUid}
 * and users/{uid}/followers/{followerUid} for fast reverse lookups.
 */
class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) {
    private val usersCollection = firestore.collection("users")

    suspend fun getUser(uid: String): Resource<User> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            val user = snapshot.toObject(User::class.java) ?: return Resource.Error("User not found.")
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load user.")
        }
    }

    suspend fun updateProfile(
        fullName: String,
        bio: String,
        newImageUri: Uri?
    ): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Not authenticated.")
        return try {
            val updates = mutableMapOf<String, Any>(
                "fullName" to fullName,
                "bio" to bio
            )

            if (newImageUri != null) {
                val downloadUrl = CloudinaryUploader.uploadImage(newImageUri, "profile_images/$uid")
                updates["profileImageUrl"] = downloadUrl
            }

            usersCollection.document(uid).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update profile.")
        }
    }

    suspend fun toggleFollow(targetUid: String, isCurrentlyFollowing: Boolean): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Not authenticated.")
        return try {
            val batch = firestore.batch()
            val myFollowingRef = usersCollection.document(uid).collection("following").document(targetUid)
            val theirFollowersRef = usersCollection.document(targetUid).collection("followers").document(uid)
            val myDocRef = usersCollection.document(uid)
            val theirDocRef = usersCollection.document(targetUid)

            if (isCurrentlyFollowing) {
                batch.delete(myFollowingRef)
                batch.delete(theirFollowersRef)
                batch.update(myDocRef, "followingCount", FieldValue.increment(-1))
                batch.update(theirDocRef, "followersCount", FieldValue.increment(-1))
            } else {
                batch.set(myFollowingRef, mapOf("timestamp" to System.currentTimeMillis()))
                batch.set(theirFollowersRef, mapOf("timestamp" to System.currentTimeMillis()))
                batch.update(myDocRef, "followingCount", FieldValue.increment(1))
                batch.update(theirDocRef, "followersCount", FieldValue.increment(1))
            }
            batch.commit().await()
            if (!isCurrentlyFollowing) {
                notificationRepository.createNotification(
                    recipientId = targetUid,
                    type = NotificationType.FOLLOW
                )
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update follow status.")
        }
    }

    suspend fun isFollowing(targetUid: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val doc = usersCollection.document(uid).collection("following").document(targetUid).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    /** Users who follow [uid], newest edge first isn't guaranteed (no ordering field on the edge doc). */
    suspend fun getFollowers(uid: String): Resource<List<User>> {
        return try {
            val edges = usersCollection.document(uid).collection("followers").get().await()
            val users = edges.documents.mapNotNull { edge ->
                usersCollection.document(edge.id).get().await().toObject(User::class.java)
            }
            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load followers.")
        }
    }

    /** Users that [uid] follows. */
    suspend fun getFollowing(uid: String): Resource<List<User>> {
        return try {
            val edges = usersCollection.document(uid).collection("following").get().await()
            val users = edges.documents.mapNotNull { edge ->
                usersCollection.document(edge.id).get().await().toObject(User::class.java)
            }
            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load following.")
        }
    }

    suspend fun searchUsers(query: String): Resource<List<User>> {
        return try {
            val snapshot = usersCollection
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .await()
            Resource.Success(snapshot.toObjects(User::class.java))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Search failed.")
        }
    }
}