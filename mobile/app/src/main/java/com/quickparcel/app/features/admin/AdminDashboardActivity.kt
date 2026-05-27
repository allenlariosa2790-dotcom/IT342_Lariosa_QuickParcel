package com.quickparcel.app.features.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityAdminDashboardBinding
import com.quickparcel.app.features.sender.DeliveryAdapter
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var adminViewModel: AdminViewModel
    private lateinit var deliveryAdapter: DeliveryAdapter

    private var currentTab = 0 // 0=Overview, 1=Users, 2=Deliveries

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        adminViewModel = AdminViewModel(retrofitClient)

        setupTabButtons()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        loadOverviewData()
    }

    private fun setupTabButtons() {
        binding.btnOverview.setOnClickListener {
            currentTab = 0
            updateTabUI()
            loadOverviewData()
        }

        binding.btnUsers.setOnClickListener {
            currentTab = 1
            updateTabUI()
            loadUsersData()
        }

        binding.btnDeliveries.setOnClickListener {
            currentTab = 2
            updateTabUI()
            loadDeliveriesData()
        }
    }

    private fun updateTabUI() {
        val selectedColor = getColor(R.color.quickparcel_blue)
        val defaultColor = getColor(R.color.quickparcel_gray)

        binding.btnOverview.setTextColor(if (currentTab == 0) selectedColor else defaultColor)
        binding.btnUsers.setTextColor(if (currentTab == 1) selectedColor else defaultColor)
        binding.btnDeliveries.setTextColor(if (currentTab == 2) selectedColor else defaultColor)

        binding.btnOverview.backgroundTintList = if (currentTab == 0) {
            androidx.core.content.ContextCompat.getColorStateList(this, R.color.quickparcel_blue)
        } else {
            androidx.core.content.ContextCompat.getColorStateList(this, R.color.quickparcel_gray_light)
        }

        binding.btnUsers.backgroundTintList = if (currentTab == 1) {
            androidx.core.content.ContextCompat.getColorStateList(this, R.color.quickparcel_blue)
        } else {
            androidx.core.content.ContextCompat.getColorStateList(this, R.color.quickparcel_gray_light)
        }

        binding.btnDeliveries.backgroundTintList = if (currentTab == 2) {
            androidx.core.content.ContextCompat.getColorStateList(this, R.color.quickparcel_blue)
        } else {
            androidx.core.content.ContextCompat.getColorStateList(this, R.color.quickparcel_gray_light)
        }

        binding.overviewContainer.visibility = if (currentTab == 0) android.view.View.VISIBLE else android.view.View.GONE
        binding.usersContainer.visibility = if (currentTab == 1) android.view.View.VISIBLE else android.view.View.GONE
        binding.deliveriesContainer.visibility = if (currentTab == 2) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun setupRecyclerView() {
        deliveryAdapter = DeliveryAdapter(emptyList()) { delivery ->
            showDeliveryDetailsDialog(delivery)
        }
        binding.rvDeliveries.layoutManager = LinearLayoutManager(this)
        binding.rvDeliveries.adapter = deliveryAdapter
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            when (currentTab) {
                0 -> loadOverviewData()
                1 -> loadUsersData()
                2 -> loadDeliveriesData()
            }
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadOverviewData() {
        adminViewModel.loadDashboardStats()
    }

    private fun loadUsersData() {
        adminViewModel.loadAllUsers()
    }

    private fun loadDeliveriesData() {
        adminViewModel.loadAllDeliveries()
    }

    private fun observeViewModel() {
        // Stats observer
        lifecycleScope.launch {
            adminViewModel.statsResult.collect { state ->
                when (state) {
                    is AdminStatsState.Loading -> showLoading(true)
                    is AdminStatsState.Success -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        updateStatsUI(state.stats)
                    }
                    is AdminStatsState.Error -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(this@AdminDashboardActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Users observer
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
                        Toast.makeText(this@AdminDashboardActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Deliveries observer
        lifecycleScope.launch {
            adminViewModel.deliveriesResult.collect { state ->
                when (state) {
                    is AdminDeliveriesState.Loading -> showLoading(true)
                    is AdminDeliveriesState.Success -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        updateDeliveriesUI(state.deliveries)
                    }
                    is AdminDeliveriesState.Error -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(this@AdminDashboardActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // User status update observer
        lifecycleScope.launch {
            adminViewModel.userStatusResult.collect { state ->
                when (state) {
                    is AdminUserStatusState.Loading -> showLoading(true)
                    is AdminUserStatusState.Success -> {
                        showLoading(false)
                        Toast.makeText(this@AdminDashboardActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    is AdminUserStatusState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@AdminDashboardActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Cancel delivery observer
        lifecycleScope.launch {
            adminViewModel.cancelDeliveryResult.collect { state ->
                when (state) {
                    is AdminCancelDeliveryState.Loading -> showLoading(true)
                    is AdminCancelDeliveryState.Success -> {
                        showLoading(false)
                        Toast.makeText(this@AdminDashboardActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    is AdminCancelDeliveryState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@AdminDashboardActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateStatsUI(stats: AdminModels.DashboardStats) {
        binding.tvTotalUsers.text = stats.totalUsers.toString()
        binding.tvTotalSenders.text = stats.totalSenders.toString()
        binding.tvTotalRiders.text = stats.totalRiders.toString()
        binding.tvActiveRiders.text = stats.activeRiders.toString()
        binding.tvTotalDeliveries.text = stats.totalDeliveries.toString()
        binding.tvPendingDeliveries.text = stats.pendingDeliveries.toString()
        binding.tvCompletedDeliveries.text = stats.completedDeliveries.toString()
        binding.tvTotalEarnings.text = "₱${String.format("%.2f", stats.totalEarnings)}"
    }

    private fun updateUsersUI(users: List<com.quickparcel.app.shared.models.User>) {
        val userAdapter = UserAdapter(users) { user, isActive ->
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to ${if (isActive) "suspend" else "activate"} ${user.firstName} ${user.lastName}?")
                .setPositiveButton("Yes") { _, _ ->
                    adminViewModel.updateUserStatus(user.id, !isActive)
                }
                .setNegativeButton("No", null)
                .show()
        }
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = userAdapter
    }

    private fun updateDeliveriesUI(deliveries: List<com.quickparcel.app.shared.models.Delivery>) {
        deliveryAdapter = DeliveryAdapter(deliveries) { delivery ->
            showDeliveryDetailsDialog(delivery)
        }
        binding.rvDeliveries.adapter = deliveryAdapter
    }

    private fun showDeliveryDetailsDialog(delivery: com.quickparcel.app.shared.models.Delivery) {
        val message = """
            Tracking: ${delivery.trackingNumber}
            Status: ${delivery.status}
            From: ${delivery.pickupAddress.take(50)}...
            To: ${delivery.dropoffAddress.take(50)}...
            Amount: ₱${String.format("%.2f", delivery.estimatedCost)}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delivery Details")
            .setMessage(message)
            .setPositiveButton("Cancel Delivery") { _, _ ->
                if (delivery.status !in listOf("DELIVERED", "CANCELLED")) {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Confirm Cancel")
                        .setMessage("Are you sure you want to cancel this delivery?")
                        .setPositiveButton("Yes") { _, _ ->
                            adminViewModel.cancelDelivery(delivery.id)
                        }
                        .setNegativeButton("No", null)
                        .show()
                } else {
                    Toast.makeText(this, "Cannot cancel ${delivery.status} delivery", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}