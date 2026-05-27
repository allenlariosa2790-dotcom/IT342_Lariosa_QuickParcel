package com.quickparcel.app.features.sender

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivitySenderDashboardBinding
import com.quickparcel.app.features.auth.AuthModels
import com.quickparcel.app.features.auth.LoginActivity
import com.quickparcel.app.features.delivery.CreateDeliveryActivity
import com.quickparcel.app.features.profile.ProfileActivity
import com.quickparcel.app.features.tracking.MyDeliveriesActivity
import com.quickparcel.app.features.tracking.TrackingActivity
import com.quickparcel.app.features.payment.PaymentHistoryActivity
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import com.quickparcel.app.shared.utils.Constants
import com.quickparcel.app.shared.utils.CurrencyFormatter
import kotlinx.coroutines.launch

class SenderDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySenderDashboardBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var senderViewModel: SenderViewModel
    private lateinit var deliveryAdapter: DeliveryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySenderDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        senderViewModel = SenderViewModel(retrofitClient)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        senderViewModel.loadDashboard()
        loadProfileData()
    }

    private fun loadProfileData() {
        lifecycleScope.launch {
            try {
                val userData = tokenManager.getUserData()
                if (userData != null) {
                    val gson = Gson()
                    val user = gson.fromJson(userData, AuthModels.JwtResponse::class.java)
                    binding.tvEmail.text = user.email
                    binding.tvWelcome.text = "Welcome back, ${user.firstName}!"

                    val baseIp = Constants.BASE_URL.replace("http://", "").replace("/", "")
                    val apiService = retrofitClient.create(com.quickparcel.app.features.profile.ProfileApiService::class.java)
                    val response = apiService.getProfilePicture()
                    if (response.isSuccessful && response.body()?.hasPicture == true) {
                        val imageUrl = "http://$baseIp${response.body()?.url}?t=${System.currentTimeMillis()}"
                        Glide.with(this@SenderDashboardActivity)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(binding.ivProfilePicture)
                    } else {
                        Glide.with(this@SenderDashboardActivity)
                            .load(R.drawable.ic_profile_placeholder)
                            .into(binding.ivProfilePicture)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Glide.with(this@SenderDashboardActivity)
                    .load(R.drawable.ic_profile_placeholder)
                    .into(binding.ivProfilePicture)
            }
        }
    }

    private fun setupRecyclerView() {
        deliveryAdapter = DeliveryAdapter(
            deliveries = emptyList(),
            onItemClick = { delivery ->
                val intent = Intent(this, TrackingActivity::class.java)
                intent.putExtra("delivery_id", delivery.id)
                intent.putExtra("tracking_number", delivery.trackingNumber)
                startActivity(intent)
            },
            isRiderMode = false,
            onStatusUpdate = null
        )
        binding.rvRecentDeliveries.layoutManager = LinearLayoutManager(this)
        binding.rvRecentDeliveries.adapter = deliveryAdapter
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            senderViewModel.loadDashboard()
            loadProfileData()
        }

        binding.btnCreateDelivery.setOnClickListener {
            startActivity(Intent(this, CreateDeliveryActivity::class.java))
        }

        binding.btnViewAllDeliveries.setOnClickListener {
            startActivity(Intent(this, MyDeliveriesActivity::class.java))
        }

        binding.btnPayments.setOnClickListener {
            startActivity(Intent(this, PaymentHistoryActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch {
                        tokenManager.clear()
                        startActivity(Intent(this@SenderDashboardActivity, LoginActivity::class.java))
                        finish()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            senderViewModel.dashboardResult.collect { state ->
                when (state) {
                    is DashboardState.Loading -> showLoading(true)
                    is DashboardState.Success -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        updateUI(state.stats, state.recentDeliveries)
                    }
                    is DashboardState.Error -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(this@SenderDashboardActivity, state.message, Toast.LENGTH_SHORT).show()

                        if (state.message.contains("401") || state.message.contains("unauthorized")) {
                            lifecycleScope.launch {
                                tokenManager.clear()
                            }
                            startActivity(Intent(this@SenderDashboardActivity, LoginActivity::class.java))
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(stats: SenderModels.DashboardStats, recentDeliveries: List<com.quickparcel.app.shared.models.Delivery>) {
        binding.tvTotalDeliveries.text = stats.totalDeliveries.toString()
        binding.tvActiveDeliveries.text = stats.activeDeliveries.toString()
        binding.tvCompletedDeliveries.text = stats.completedDeliveries.toString()
        binding.tvTotalSpent.text = CurrencyFormatter.format(stats.totalSpent)

        val limitedDeliveries = recentDeliveries.take(3)

        deliveryAdapter = DeliveryAdapter(
            deliveries = limitedDeliveries,
            onItemClick = { delivery ->
                val intent = Intent(this, TrackingActivity::class.java)
                intent.putExtra("delivery_id", delivery.id)
                intent.putExtra("tracking_number", delivery.trackingNumber)
                startActivity(intent)
            },
            isRiderMode = false,
            onStatusUpdate = null
        )
        binding.rvRecentDeliveries.adapter = deliveryAdapter

        if (limitedDeliveries.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvRecentDeliveries.visibility = android.view.View.GONE
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.rvRecentDeliveries.visibility = android.view.View.VISIBLE
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnCreateDelivery.isEnabled = !show
        binding.btnViewAllDeliveries.isEnabled = !show
        binding.btnProfile.isEnabled = !show
    }
}