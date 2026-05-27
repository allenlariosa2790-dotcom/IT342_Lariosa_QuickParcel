package com.quickparcel.app.features.rider

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityRiderDashboardBinding
import com.quickparcel.app.features.auth.AuthModels
import com.quickparcel.app.features.sender.DeliveryAdapter
import com.quickparcel.app.features.tracking.TrackingActivity
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class RiderDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiderDashboardBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var riderViewModel: RiderViewModel
    private lateinit var deliveryAdapter: DeliveryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiderDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        riderViewModel = RiderViewModel(retrofitClient)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        riderViewModel.loadDashboardData()
    }

    private fun setupRecyclerView() {
        deliveryAdapter = DeliveryAdapter(emptyList()) { delivery ->
            val intent = Intent(this, TrackingActivity::class.java)
            intent.putExtra("delivery_id", delivery.id)
            intent.putExtra("tracking_number", delivery.trackingNumber)
            startActivity(intent)
        }
        binding.rvRecentDeliveries.layoutManager = LinearLayoutManager(this)
        binding.rvRecentDeliveries.adapter = deliveryAdapter
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            riderViewModel.loadDashboardData()
        }

        binding.btnBrowseDeliveries.setOnClickListener {
            startActivity(Intent(this, AvailableDeliveriesActivity::class.java))
        }

        binding.btnAvailableDeliveries.setOnClickListener {
            startActivity(Intent(this, AvailableDeliveriesActivity::class.java))
        }

        binding.btnEarnings.setOnClickListener {
            startActivity(Intent(this, EarningsActivity::class.java))
        }

        binding.btnViewTracking.setOnClickListener {
            val intent = Intent(this, TrackingActivity::class.java)
            intent.putExtra("delivery_id", it.tag as? Int ?: 0)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            riderViewModel.dashboardResult.collect { state ->
                when (state) {
                    is RiderDashboardState.Loading -> showLoading(true)
                    is RiderDashboardState.Success -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        updateUI(state.stats, state.activeDelivery, state.recentDeliveries)
                    }
                    is RiderDashboardState.Error -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(this@RiderDashboardActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateUI(stats: RiderModels.EarningsStats, activeDelivery: com.quickparcel.app.shared.models.Delivery?, recentDeliveries: List<com.quickparcel.app.shared.models.Delivery>) {
        // Load user name
        lifecycleScope.launch {
            var firstName = "Rider"
            try {
                val userData = tokenManager.getUserData()
                if (userData != null) {
                    val gson = Gson()
                    val user = gson.fromJson(userData, AuthModels.JwtResponse::class.java)
                    firstName = user.firstName
                }
            } catch (e: Exception) {
                // Use default
            }
            binding.tvWelcome.text = "Welcome back, $firstName!"
        }

        binding.tvTodayEarnings.text = "₱${String.format("%.2f", stats.today)}"
        binding.tvCompletedCount.text = stats.completedCount.toString()
        binding.tvWeekEarnings.text = "₱${String.format("%.2f", stats.thisWeek)}"
        binding.tvTotalEarnings.text = "₱${String.format("%.2f", stats.total)}"

        // Update active delivery section
        if (activeDelivery != null) {
            binding.cardActiveDelivery.visibility = android.view.View.VISIBLE
            binding.cardNoActive.visibility = android.view.View.GONE
            binding.tvActiveTracking.text = activeDelivery.trackingNumber
            binding.tvActiveStatus.text = activeDelivery.status
            binding.tvActivePickup.text = "📍 ${activeDelivery.pickupAddress.take(50)}"
            binding.tvActiveDropoff.text = "🏁 ${activeDelivery.dropoffAddress.take(50)}"
            binding.tvActiveEarning.text = "₱${String.format("%.2f", activeDelivery.estimatedCost)}"
            binding.btnViewTracking.tag = activeDelivery.id
        } else {
            binding.cardActiveDelivery.visibility = android.view.View.GONE
            binding.cardNoActive.visibility = android.view.View.VISIBLE
        }

        // Update recent deliveries
        val recent = recentDeliveries.take(5)
        deliveryAdapter = DeliveryAdapter(recent) { delivery ->
            val intent = Intent(this, TrackingActivity::class.java)
            intent.putExtra("delivery_id", delivery.id)
            intent.putExtra("tracking_number", delivery.trackingNumber)
            startActivity(intent)
        }
        binding.rvRecentDeliveries.adapter = deliveryAdapter
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}