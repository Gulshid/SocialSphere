package com.gulshid.socialsphere.data.repository

import android.net.Uri
import com.gulshid.socialsphere.data.model.Comment
import com.gulshid.socialsphere.data.model.Post
import com.gulshid.socialsphere.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Handles all post-related data operations: creating posts, uploading
 * images to Firebase Storage, paginated feed retrieval, likes and comments.
 */
class PostRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val postsCollection = firestore.collection("posts")
    private val usersCollection = firestore.collection("users")

    /** Uploads an image to Storage and returns its public download URL. */
    private suspend fun uploadImage(imageUri: Uri, path: String): String {
        val ref = storage.reference.child(path)
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun createPost(imageUri: Uri, caption: String): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Not authenticated.")
        return try {
            val userSnapshot = usersCollection.document(uid).get().await()
            val username = userSnapshot.getString("username") ?: "user"
            val profileImageUrl = userSnapshot.getString("profileImageUrl") ?: ""

            val fileName = "posts/$uid/${UUID.randomUUID()}.jpg"
            val imageUrl = uploadImage(imageUri, fileName)

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

    /** Fetches the most recent posts for the home feed, newest first. */
    suspend fun getFeed(limit: Long = 20): Resource<List<Post>> {
        return try {
            val snapshot = postsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
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

    /** Toggles a like for the current user on the given post (optimistic-friendly). */
    suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Not authenticated.")
        return try {
            val postRef = postsCollection.document(postId)
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
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update like.")
        }
    }

    suspend fun addComment(postId: String, text: String): Resource<Unit> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Not authenticated.")
        return try {
            val userSnapshot = usersCollection.document(uid).get().await()
            val username = userSnapshot.getString("username") ?: "user"
            val profileImageUrl = userSnapshot.getString("profileImageUrl") ?: ""

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
}
