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
import com.quickparcel.app.features.tracking.MyDeliveriesActivity
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
    private lateinit var activeDeliveriesAdapter: DeliveryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiderDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        riderViewModel = RiderViewModel(retrofitClient)

        setupRecyclerView()
        setupActiveDeliveriesRecyclerView()
        setupListeners()
        observeViewModel()

        riderViewModel.loadDashboardData()
        riderViewModel.loadAvailableDeliveries()
        riderViewModel.loadActiveDeliveries()
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

    private fun setupActiveDeliveriesRecyclerView() {
        activeDeliveriesAdapter = DeliveryAdapter(
            deliveries = emptyList(),
            onItemClick = { delivery ->
                val intent = Intent(this, TrackingActivity::class.java)
                intent.putExtra("delivery_id", delivery.id)
                intent.putExtra("tracking_number", delivery.trackingNumber)
                startActivity(intent)
            },
            isRiderMode = true,
            onStatusUpdate = { delivery, newStatus ->
                updateDeliveryStatus(delivery.id, newStatus)
            }
        )
        binding.rvActiveDeliveries.layoutManager = LinearLayoutManager(this)
        binding.rvActiveDeliveries.adapter = activeDeliveriesAdapter
    }

    private fun updateDeliveryStatus(deliveryId: Int, newStatus: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Update Status")
            .setMessage("Mark delivery as $newStatus?")
            .setPositiveButton("Yes") { _, _ ->
                riderViewModel.updateDeliveryStatus(deliveryId, newStatus, "Current location")
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            riderViewModel.loadDashboardData()
            riderViewModel.loadAvailableDeliveries()
            riderViewModel.loadActiveDeliveries()
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

        binding.btnViewAllActive.setOnClickListener {
            startActivity(Intent(this, ActiveDeliveriesActivity::class.java))
        }

        // Add this button to your layout or remove if not present
        // binding.btnViewAllDeliveries.setOnClickListener {
        //     startActivity(Intent(this, MyDeliveriesActivity::class.java))
        // }
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

        lifecycleScope.launch {
            riderViewModel.availableResult.collect { state ->
                when (state) {
                    is RiderAvailableState.Success -> {
                        val count = state.deliveries.size
                        binding.btnAvailableDeliveries.text = "Available ($count)"
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            riderViewModel.activeDeliveriesResult.collect { state ->
                when (state) {
                    is RiderActiveDeliveriesState.Success -> {
                        activeDeliveriesAdapter.updateDeliveries(state.deliveries.take(3))
                        if (state.deliveries.isEmpty()) {
                            binding.tvActiveEmpty.visibility = android.view.View.VISIBLE
                            binding.rvActiveDeliveries.visibility = android.view.View.GONE
                        } else {
                            binding.tvActiveEmpty.visibility = android.view.View.GONE
                            binding.rvActiveDeliveries.visibility = android.view.View.VISIBLE
                        }
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            riderViewModel.statusResult.collect { state ->
                when (state) {
                    is RiderStatusState.Success -> {
                        Toast.makeText(this@RiderDashboardActivity, "✅ Status updated to ${state.delivery.status}", Toast.LENGTH_SHORT).show()
                        riderViewModel.loadDashboardData()
                        riderViewModel.loadActiveDeliveries()
                    }
                    is RiderStatusState.Error -> {
                        Toast.makeText(this@RiderDashboardActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun updateUI(stats: RiderModels.EarningsStats, activeDelivery: com.quickparcel.app.shared.models.Delivery?, recentDeliveries: List<com.quickparcel.app.shared.models.Delivery>) {
        lifecycleScope.launch {
            var firstName = "Rider"
            try {
                val userData = tokenManager.getUserData()
                if (userData != null) {
                    val gson = Gson()
                    val user = gson.fromJson(userData, AuthModels.JwtResponse::class.java)
                    firstName = user.firstName
                }
            } catch (e: Exception) {}
            binding.tvWelcome.text = "Welcome back, $firstName!"
        }

        binding.tvTodayEarnings.text = "₱${String.format("%.2f", stats.today)}"
        binding.tvCompletedCount.text = stats.completedCount.toString()
        binding.tvWeekEarnings.text = "₱${String.format("%.2f", stats.thisWeek)}"
        binding.tvTotalEarnings.text = "₱${String.format("%.2f", stats.total)}"

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

        val recent = recentDeliveries.take(5)
        deliveryAdapter.updateDeliveries(recent)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}