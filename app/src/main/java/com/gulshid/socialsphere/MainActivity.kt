package com.gulshid.socialsphere

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.gulshid.socialsphere.databinding.ActivityMainBinding
import com.gulshid.socialsphere.post.CreatePostActivity

/**
 * Shell activity: hosts the bottom navigation bar and the NavHostFragment
 * containing Home, Search, Notifications and Profile. The "Create" tab is
 * intercepted to launch CreatePostActivity instead of being a graph destination.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
}
