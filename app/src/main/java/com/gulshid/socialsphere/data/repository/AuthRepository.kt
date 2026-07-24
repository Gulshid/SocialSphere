package com.example.socialsphere.data.repository

import com.example.socialsphere.data.model.User
import com.example.socialsphere.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Single source of truth for authentication and the current user's profile.
 * Wraps FirebaseAuth + Firestore "users" collection.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUserId: String?
        get() = auth.currentUser?.uid

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    suspend fun login(email: String, password: String): Resource<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed. Please check your credentials.")
        }
    }

    suspend fun register(
        email: String,
        password: String,
        username: String,
        fullName: String
    ): Resource<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("Registration failed.")

            val newUser = User(
                uid = uid,
                username = username,
                fullName = fullName,
                email = email
            )
            firestore.collection("users").document(uid).set(newUser).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed. Please try again.")
        }
    }

    suspend fun getCurrentUser(): Resource<User> {
        val uid = currentUserId ?: return Resource.Error("Not authenticated.")
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java) ?: return Resource.Error("User not found.")
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load profile.")
        }
    }

    fun logout() {
        auth.signOut()
    }
}
