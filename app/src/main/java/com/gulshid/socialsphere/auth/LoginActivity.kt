package com.gulshid.socialsphere.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gulshid.socialsphere.MainActivity
import com.gulshid.socialsphere.R
import com.gulshid.socialsphere.data.repository.AuthRepository
import com.gulshid.socialsphere.databinding.ActivityLoginBinding
import com.gulshid.socialsphere.utils.Resource
import com.gulshid.socialsphere.utils.gone
import com.gulshid.socialsphere.utils.isValidEmail
import com.gulshid.socialsphere.utils.toast
import com.gulshid.socialsphere.utils.visible
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvForgotPassword.setOnClickListener {
            toast("Password reset coming soon")
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        var isValid = true
        if (!email.isValidEmail()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        }
        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_short_password)
            isValid = false
        }
        if (!isValid) return

        setLoading(true)
        lifecycleScope.launch {
            when (val result = authRepository.login(email, password)) {
                is Resource.Success -> {
                    setLoading(false)
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finishAffinity()
                }
                is Resource.Error -> {
                    setLoading(false)
                    toast(result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.let { if (loading) it.visible() else it.gone() }
        binding.btnLogin.isEnabled = !loading
    }
}
