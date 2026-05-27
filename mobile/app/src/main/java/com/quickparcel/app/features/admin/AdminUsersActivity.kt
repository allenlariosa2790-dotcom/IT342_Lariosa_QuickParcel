package com.quickparcel.app.features.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityAdminUsersBinding
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class AdminUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUsersBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var adminViewModel: AdminViewModel
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        adminViewModel = AdminViewModel(retrofitClient)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        adminViewModel.loadAllUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(emptyList()) { user, isActive ->
            showStatusToggleDialog(user, isActive)
        }
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = userAdapter
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            adminViewModel.loadAllUsers()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showStatusToggleDialog(user: com.quickparcel.app.shared.models.User, isActive: Boolean) {
        val action = if (isActive) "suspend" else "activate"
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm $action")
            .setMessage("Are you sure you want to $action ${user.firstName} ${user.lastName}?")
            .setPositiveButton("Yes") { _, _ ->
                adminViewModel.updateUserStatus(user.id, !isActive)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            adminViewModel.usersResult.collect { state ->
                when (state) {
                    is AdminUsersState.Loading -> showLoading(true)
                    is AdminUsersState.Success -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        updateUsersUI(state.users)
                    }
                    is AdminUsersState.Error -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(this@AdminUsersActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            adminViewModel.userStatusResult.collect { state ->
                when (state) {
                    is AdminUserStatusState.Loading -> showLoading(true)
                    is AdminUserStatusState.Success -> {
                        showLoading(false)
                        Toast.makeText(this@AdminUsersActivity, state.message, Toast.LENGTH_SHORT).show()
                        adminViewModel.loadAllUsers()
                    }
                    is AdminUserStatusState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@AdminUsersActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateUsersUI(users: List<com.quickparcel.app.shared.models.User>) {
        userAdapter.updateUsers(users)
        binding.tvTotalUsers.text = "Total: ${users.size} users"

        if (users.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvUsers.visibility = android.view.View.GONE
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.rvUsers.visibility = android.view.View.VISIBLE
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}