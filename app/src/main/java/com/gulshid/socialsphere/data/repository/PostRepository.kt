package com.gulshid.socialsphere.data.repository

import android.net.Uri
import com.gulshid.socialsphere.data.model.Comment
import com.gulshid.socialsphere.data.model.NotificationType
import com.gulshid.socialsphere.data.model.Post
import com.gulshid.socialsphere.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gulshid.socialsphere.utils.CloudinaryUploader
import kotlinx.coroutines.tasks.await

/**
 * Handles all post-related data operations: creating posts, uploading
 * images to Cloudinary, paginated feed retrieval, likes and comments.
 */
class PostRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) {
    private val postsCollection = firestore.collection("posts")
    private val usersCollection = firestore.collection("users")

    /** Uploads an image to Cloudinary and returns its public download URL. */
    private suspend fun uploadImage(imageUri: Uri, folder: String): String {
        return CloudinaryUploader.uploadImage(imageUri, folder)
    }

    suspend fun createPost(imageUri: Uri, caption: String): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Not authenticated.")
        return try {
            val userSnapshot = usersCollection.document(uid).get().await()
            val username = userSnapshot.getString("username") ?: "user"
            val profileImageUrl = userSnapshot.getString("profileImageUrl") ?: ""

            val imageUrl = uploadImage(imageUri, "posts/$uid")

            val postId = postsCollection.document().id
            val post = Post(
                postId = postId,
                authorId = uid,
                authorUsername = username,
                authorProfileImageUrl = profileImageUrl,
                caption = caption,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis()
            )

            postsCollection.document(postId).set(post).await()
            usersCollection.document(uid).update("postsCount", FieldValue.increment(1)).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create post.")
        }
    }

    /**
     * Fetches a page of the most recent posts for the home feed, newest first.
     * Pass [startAfterTimestamp] (the `timestamp` of the last post already
     * loaded) to fetch the next page for infinite-scroll pagination.
     */
    suspend fun getFeed(limit: Long = 10, startAfterTimestamp: Long? = null): Resource<List<Post>> {
        return try {
            var query = postsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
            if (startAfterTimestamp != null) {
                query = query.startAfter(startAfterTimestamp)
            }
            val snapshot = query.get().await()
            val posts = snapshot.toObjects(Post::class.java)
            Resource.Success(posts)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load feed.")
        }
    }

    suspend fun getPostsByUser(uid: String): Resource<List<Post>> {
        return try {
            val snapshot = postsCollection
                .whereEqualTo("authorId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            Resource.Success(snapshot.toObjects(Post::class.java))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load posts.")
        }
    }

    /**
     * Toggles a like for the current user on [post] (optimistic-friendly).
     * Sends the post's author a "like" notification, unless they're liking
     * their own post or un-liking.
     */
    suspend fun toggleLike(post: Post, isCurrentlyLiked: Boolean): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Not authenticated.")
        return try {
            val postRef = postsCollection.document(post.postId)
            if (isCurrentlyLiked) {
                postRef.update(
                    "likedBy", FieldValue.arrayRemove(uid),
                    "likeCount", FieldValue.increment(-1)
                ).await()
            } else {
                postRef.update(
                    "likedBy", FieldValue.arrayUnion(uid),
                    "likeCount", FieldValue.increment(1)
                ).await()
                notificationRepository.createNotification(
                    recipientId = post.authorId,
                    type = NotificationType.LIKE,
                    postId = post.postId,
                    postImageUrl = post.imageUrl
                )
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update like.")
        }
    }

    /** Adds a comment and notifies the post's author (unless they're commenting on their own post). */
    suspend fun addComment(postId: String, text: String): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Not authenticated.")
        return try {
            val userSnapshot = usersCollection.document(uid).get().await()
            val username = userSnapshot.getString("username") ?: "user"
            val profileImageUrl = userSnapshot.getString("profileImageUrl") ?: ""

            val postSnapshot = postsCollection.document(postId).get().await()
            val postAuthorId = postSnapshot.getString("authorId") ?: ""
            val postImageUrl = postSnapshot.getString("imageUrl") ?: ""

            val commentRef = postsCollection.document(postId).collection("comments").document()
            val comment = Comment(
                commentId = commentRef.id,
                authorId = uid,
                authorUsername = username,
                authorProfileImageUrl = profileImageUrl,
                text = text
            )
            commentRef.set(comment).await()
            postsCollection.document(postId).update("commentCount", FieldValue.increment(1)).await()

            if (postAuthorId.isNotBlank()) {
                notificationRepository.createNotification(
                    recipientId = postAuthorId,
                    type = NotificationType.COMMENT,
                    postId = postId,
                    postImageUrl = postImageUrl,
                    commentText = text
                )
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add comment.")
        }
    }

    suspend fun getComments(postId: String): Resource<List<Comment>> {
        return try {
            val snapshot = postsCollection.document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
            Resource.Success(snapshot.toObjects(Comment::class.java))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load comments.")
        }
    }

    /** Deletes a post (only the author is allowed to, enforced by Firestore rules too). */
    suspend fun deletePost(postId: String, authorId: String): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Not authenticated.")
        if (uid != authorId) return Resource.Error("You can only delete your own posts.")
        return try {
            postsCollection.document(postId).delete().await()
            usersCollection.document(uid).update("postsCount", FieldValue.increment(-1)).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete post.")
        }
    }
}
