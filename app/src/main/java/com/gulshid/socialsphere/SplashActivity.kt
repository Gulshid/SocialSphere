package com.example.socialsphere

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.socialsphere.auth.LoginActivity
import com.example.socialsphere.data.repository.AuthRepository

/**
 * Transient entry point: checks whether a user session already exists
 * and routes to either the main feed or the login screen.
 */
class SplashActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val destination = if (authRepository.isLoggedIn) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        startActivity(destination)
        finish()
    }
}
