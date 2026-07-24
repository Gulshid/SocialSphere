package com.example.socialsphere.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles incoming Firebase Cloud Messaging push notifications
 * (e.g. new followers, likes, comments). Extend onMessageReceived
 * to build and display a NotificationCompat notification.
 */
class FcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Persist token to the current user's Firestore document
        // so the backend / Cloud Functions can target this device.
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: return
        val body = message.notification?.body ?: ""
        // TODO: Build and show a NotificationCompat.Builder notification
        // using title/body and message.data for deep-linking.
    }
}
