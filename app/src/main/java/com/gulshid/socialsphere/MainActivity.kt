package com.gulshid.socialsphere

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.gulshid.socialsphere.databinding.ActivityMainBinding
import com.gulshid.socialsphere.post.CreatePostActivity
import com.gulshid.socialsphere.utils.FcmService

/**
 * Shell activity: hosts the bottom navigation bar and the NavHostFragment
 * containing Home, Search, Notifications and Profile. The "Create" tab is
 * intercepted to launch CreatePostActivity instead of being a graph destination.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* either way, proceed */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermissionIfNeeded()
        FcmService.syncToken()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.createPostAction) {
                startActivity(Intent(this, CreatePostActivity::class.java))
                false // don't mark this tab as selected
            } else {
                navController.navigate(item.itemId)
                true
            }
        }
    }

    /** Android 13+ requires runtime consent to show notifications (likes, comments, followers, etc). */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
