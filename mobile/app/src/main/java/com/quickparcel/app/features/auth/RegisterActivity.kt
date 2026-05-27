package com.quickparcel.app.features.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityRegisterBinding
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var tokenManager: TokenManager
    private var selectedRole = "SENDER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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
        binding.btnRegister.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.register(email, password, firstName, lastName, phone, selectedRole)
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            authViewModel.registerResult.collect { result ->
                when (result) {
                    is RegisterResult.Loading -> showLoading(true)
                    is RegisterResult.Success -> {
                        showLoading(false)
                        Toast.makeText(this@RegisterActivity, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    }
                    is RegisterResult.Error -> {
                        showLoading(false)
                        Toast.makeText(this@RegisterActivity, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnRegister.isEnabled = !show
        if (show) {
            binding.btnRegister.text = ""
        } else {
            binding.btnRegister.text = "Sign Up"
        }
    }
}