package com.quickparcel.app.features.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityLoginBinding
import com.quickparcel.app.features.admin.AdminDashboardActivity
import com.quickparcel.app.features.rider.RiderDashboardActivity
import com.quickparcel.app.features.sender.SenderDashboardActivity
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var tokenManager: TokenManager
    private var selectedRole = "SENDER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        val retrofitClient = RetrofitClient(tokenManager)
        authViewModel = AuthViewModel(retrofitClient, tokenManager)

        setupTabs()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupTabs() {
        binding.cardSender.setOnClickListener {
            selectedRole = "SENDER"
            updateTabUI()
        }
        binding.cardRider.setOnClickListener {
            selectedRole = "RIDER"
            updateTabUI()
        }
    }

    private fun updateTabUI() {
        binding.tabSender.setTextColor(if (selectedRole == "SENDER") android.graphics.Color.WHITE else android.graphics.Color.GRAY)
        binding.tabSender.background = if (selectedRole == "SENDER") {
            getDrawable(R.drawable.bg_tab_selected)
        } else {
            getDrawable(R.drawable.bg_tab_unselected)
        }
        binding.tabRider.setTextColor(if (selectedRole == "RIDER") android.graphics.Color.WHITE else android.graphics.Color.GRAY)
        binding.tabRider.background = if (selectedRole == "RIDER") {
            getDrawable(R.drawable.bg_tab_selected)
        } else {
            getDrawable(R.drawable.bg_tab_unselected)
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.login(email, password, selectedRole)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            authViewModel.loginResult.collect { result ->
                when (result) {
                    is AuthResult.Loading -> showLoading(true)
                    is AuthResult.Success -> {
                        showLoading(false)
                        val user = result.user

                        // Save user data
                        val userData = AuthModels.JwtResponse(
                            token = user.token,
                            id = user.id,
                            email = user.email,
                            firstName = user.firstName,
                            lastName = user.lastName,
                            userType = user.userType,
                            phone = user.phone,
                            createdAt = user.createdAt
                        )

                        lifecycleScope.launch {
                            tokenManager.saveToken(user.token)
                            tokenManager.saveUserData(Gson().toJson(userData))
                        }

                        val intent = when (user.userType) {
                            "SENDER" -> Intent(this@LoginActivity, SenderDashboardActivity::class.java)
                            "RIDER" -> Intent(this@LoginActivity, RiderDashboardActivity::class.java)
                            "ADMIN" -> Intent(this@LoginActivity, AdminDashboardActivity::class.java)
                            else -> Intent(this@LoginActivity, LoginActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    }
                    is AuthResult.Error -> {
                        showLoading(false)
                        Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnLogin.isEnabled = !show
    }
}