package com.quickparcel.app.features.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityProfileBinding
import com.quickparcel.app.features.auth.AuthModels
import com.quickparcel.app.features.auth.LoginActivity
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var profileViewModel: ProfileViewModel

    private var isEditing = false
    private var currentUser: AuthModels.JwtResponse? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadProfilePicture(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        profileViewModel = ProfileViewModel(retrofitClient)

        loadUserData()
        setupListeners()
        observeViewModel()

        profileViewModel.loadProfilePicture()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val userData = tokenManager.getUserData()
                if (userData != null) {
                    val gson = Gson()
                    currentUser = gson.fromJson(userData, AuthModels.JwtResponse::class.java)
                    displayUserInfo()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayUserInfo() {
        currentUser?.let { user ->
            binding.tvName.text = "${user.firstName} ${user.lastName}"
            binding.tvEmail.text = user.email

            // Handle phone - show "Not provided" if null or empty
            val phoneText = if (user.phone.isNullOrEmpty()) "Not provided" else user.phone
            binding.tvPhone.text = phoneText

            // Handle createdAt - show default if null
            val createdDate = user.createdAt?.take(10) ?: "2024"
            binding.tvMemberSince.text = "Member since $createdDate"

            // Set form values when editing
            binding.etFirstName.setText(user.firstName)
            binding.etLastName.setText(user.lastName)
            binding.etPhone.setText(user.phone ?: "")
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnEditProfile.setOnClickListener {
            isEditing = true
            updateEditMode()
        }

        binding.btnCancelEdit.setOnClickListener {
            isEditing = false
            updateEditMode()
            displayUserInfo() // Reset form values
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfileChanges()
        }

        binding.btnChangePassword.setOnClickListener {
            Toast.makeText(this, "Change Password - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.ivProfilePicture.setOnClickListener {
            showImagePicker()
        }

        binding.btnChangePhoto.setOnClickListener {
            showImagePicker()
        }
    }

    private fun updateEditMode() {
        if (isEditing) {
            binding.tvName.visibility = android.view.View.GONE
            binding.tvEmail.visibility = android.view.View.GONE
            binding.tvPhone.visibility = android.view.View.GONE
            binding.linearEditForm.visibility = android.view.View.VISIBLE
            binding.btnEditProfile.visibility = android.view.View.GONE
            binding.btnChangePassword.visibility = android.view.View.GONE
            binding.btnLogout.visibility = android.view.View.GONE
            binding.linearButtons.visibility = android.view.View.VISIBLE
        } else {
            binding.tvName.visibility = android.view.View.VISIBLE
            binding.tvEmail.visibility = android.view.View.VISIBLE
            binding.tvPhone.visibility = android.view.View.VISIBLE
            binding.linearEditForm.visibility = android.view.View.GONE
            binding.btnEditProfile.visibility = android.view.View.VISIBLE
            binding.btnChangePassword.visibility = android.view.View.VISIBLE
            binding.btnLogout.visibility = android.view.View.VISIBLE
            binding.linearButtons.visibility = android.view.View.GONE
        }
    }

    private fun saveProfileChanges() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "First name and last name are required", Toast.LENGTH_SHORT).show()
            return
        }

        profileViewModel.updateProfile(firstName, lastName, if (phone.isNotEmpty()) phone else null)
    }

    private fun showImagePicker() {
        pickImageLauncher.launch("image/*")
    }

    private fun uploadProfilePicture(uri: Uri) {
        profileViewModel.uploadProfilePicture(uri, this)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            profileViewModel.updateResult.collect { state ->
                when (state) {
                    is ProfileState.Loading -> showLoading(true)
                    is ProfileState.Success -> {
                        showLoading(false)
                        Toast.makeText(this@ProfileActivity, state.message, Toast.LENGTH_SHORT).show()
                        isEditing = false
                        updateEditMode()
                        loadUserData() // Reload user data
                        // Update stored user data with new phone
                        updateStoredUserData()
                    }
                    is ProfileState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@ProfileActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            profileViewModel.pictureResult.collect { state ->
                when (state) {
                    is PictureState.Loading -> {
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                    is PictureState.Success -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        if (state.url.isNotEmpty()) {
                            val fullUrl = "http://10.0.2.2:8080${state.url}?t=${System.currentTimeMillis()}"
                            Glide.with(this@ProfileActivity)
                                .load(fullUrl)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .circleCrop()
                                .into(binding.ivProfilePicture)
                        }
                    }
                    is PictureState.NoPicture -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        Glide.with(this@ProfileActivity)
                            .load(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(binding.ivProfilePicture)
                    }
                    is PictureState.Error -> {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@ProfileActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateStoredUserData() {
        lifecycleScope.launch {
            try {
                val userData = tokenManager.getUserData()
                if (userData != null) {
                    val gson = Gson()
                    val user = gson.fromJson(userData, AuthModels.JwtResponse::class.java)
                    val updatedUser = user.copy(
                        firstName = binding.etFirstName.text.toString().trim(),
                        lastName = binding.etLastName.text.toString().trim(),
                        phone = binding.etPhone.text.toString().trim()
                    )
                    tokenManager.saveUserData(gson.toJson(updatedUser))
                    currentUser = updatedUser
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun logout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    tokenManager.clear()
                    startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                    finish()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnSaveProfile.isEnabled = !show
    }
}