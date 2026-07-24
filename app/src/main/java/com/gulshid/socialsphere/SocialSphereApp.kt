package com.gulshid.socialsphere

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.gulshid.socialsphere.utils.FcmService

class SocialSphereApp : Application() {

    companion object {
        /** App-wide context used by utilities (e.g. CloudinaryUploader) that need
         *  a ContentResolver but sit below the Activity/ViewModel layer. */
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        FirebaseApp.initializeApp(this)
        createNotificationChannel()

        // Enable offline persistence for a smoother, faster feed experience
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        Firebase.firestore.firestoreSettings = settings
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FcmService.CHANNEL_ID,
                "General",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Likes, comments, and new followers"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
