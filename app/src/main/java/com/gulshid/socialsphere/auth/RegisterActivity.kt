package com.example.socialsphere.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.socialsphere.MainActivity
import com.example.socialsphere.R
import com.example.socialsphere.data.repository.AuthRepository
import com.example.socialsphere.databinding.ActivityRegisterBinding
import com.example.socialsphere.utils.Resource
import com.example.socialsphere.utils.gone
import com.example.socialsphere.utils.isValidEmail
import com.example.socialsphere.utils.toast
import com.example.socialsphere.utils.visible
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        binding.tvGoToLogin.setOnClickListener { finish() }
        binding.btnRegister.setOnClickListener { attemptRegister() }
    }

    private fun attemptRegister() {
        val fullName = binding.etFullName.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.tilFullName.error = null
        binding.tilUsername.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        var isValid = true
        if (fullName.isBlank()) {
            binding.tilFullName.error = getString(R.string.error_required_field)
            isValid = false
        }
        if (username.isBlank()) {
            binding.tilUsername.error = getString(R.string.error_required_field)
            isValid = false
        }
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
            when (val result = authRepository.register(email, password, username, fullName)) {
                is Resource.Success -> {
                    setLoading(false)
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
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
        binding.btnRegister.isEnabled = !loading
    }
}
