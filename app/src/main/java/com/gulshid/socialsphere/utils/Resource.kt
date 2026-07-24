package com.example.socialsphere.utils

/**
 * A generic wrapper class used to represent the state of any async
 * operation (network/Firestore/Storage calls) as it flows to the UI.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
