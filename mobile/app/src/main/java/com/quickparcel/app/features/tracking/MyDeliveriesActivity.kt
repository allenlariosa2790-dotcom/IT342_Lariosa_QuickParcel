package com.quickparcel.app.features.tracking

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityMyDeliveriesBinding
import com.quickparcel.app.features.delivery.CreateDeliveryActivity
import com.quickparcel.app.features.sender.DeliveryAdapter
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class MyDeliveriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyDeliveriesBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var trackingViewModel: TrackingViewModel
    private lateinit var deliveryAdapter: DeliveryAdapter

    private var allDeliveries: List<com.quickparcel.app.shared.models.Delivery> = emptyList()
    private var currentSort = "date_desc"
    private var currentFilter = "ALL"
    private var isRiderMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyDeliveriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)

        checkUserType()

        trackingViewModel = TrackingViewModel(retrofitClient)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        trackingViewModel.loadMyDeliveries()
    }

    private fun checkUserType() {
        lifecycleScope.launch {
            try {
                val userData = tokenManager.getUserData()
                if (userData != null) {
                    val gson = Gson()
                    val user = gson.fromJson(userData, com.quickparcel.app.features.auth.AuthModels.JwtResponse::class.java)
                    isRiderMode = user.userType == "RIDER"
                }
            } catch (e: Exception) {
                isRiderMode = false
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
            isRiderMode = isRiderMode,
            onStatusUpdate = if (isRiderMode) { delivery, newStatus ->
                updateDeliveryStatus(delivery.id, newStatus)
            } else null
        )
        binding.rvDeliveries.layoutManager = LinearLayoutManager(this)
        binding.rvDeliveries.adapter = deliveryAdapter
    }

    private fun updateDeliveryStatus(deliveryId: Int, newStatus: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Update Status")
            .setMessage("Mark delivery as $newStatus?")
            .setPositiveButton("Yes") { _, _ ->
                val riderViewModel = com.quickparcel.app.features.rider.RiderViewModel(retrofitClient)
                riderViewModel.updateDeliveryStatus(deliveryId, newStatus, "Current location")

                lifecycleScope.launch {
                    riderViewModel.statusResult.collect { state ->
                        when (state) {
                            is com.quickparcel.app.features.rider.RiderStatusState.Success -> {
                                Toast.makeText(this@MyDeliveriesActivity, "✅ Status updated to ${state.delivery.status}", Toast.LENGTH_SHORT).show()
                                trackingViewModel.loadMyDeliveries()
                            }
                            is com.quickparcel.app.features.rider.RiderStatusState.Error -> {
                                Toast.makeText(this@MyDeliveriesActivity, state.message, Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            trackingViewModel.loadMyDeliveries()
        }

        binding.fabCreateDelivery.setOnClickListener {
            startActivity(Intent(this, CreateDeliveryActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Sort buttons
        binding.btnSortDate.setOnClickListener {
            currentSort = if (currentSort == "date_desc") "date_asc" else "date_desc"
            updateSortUI()
            applySortingAndFiltering()
        }

        binding.btnSortStatus.setOnClickListener {
            currentSort = "status"
            updateSortUI()
            applySortingAndFiltering()
        }

        binding.btnSortAmount.setOnClickListener {
            currentSort = "amount"
            updateSortUI()
            applySortingAndFiltering()
        }

        // Filter buttons
        binding.btnFilterAll.setOnClickListener {
            currentFilter = "ALL"
            updateFilterUI()
            applySortingAndFiltering()
        }

        binding.btnFilterPending.setOnClickListener {
            currentFilter = "PENDING"
            updateFilterUI()
            applySortingAndFiltering()
        }

        binding.btnFilterAccepted.setOnClickListener {
            currentFilter = "ACCEPTED"
            updateFilterUI()
            applySortingAndFiltering()
        }

        binding.btnFilterIntransit.setOnClickListener {
            currentFilter = "IN_TRANSIT"
            updateFilterUI()
            applySortingAndFiltering()
        }

        binding.btnFilterDelivered.setOnClickListener {
            currentFilter = "DELIVERED"
            updateFilterUI()
            applySortingAndFiltering()
        }
    }

    private fun updateSortUI() {
        val selectedColor = getColor(R.color.quickparcel_blue)
        val defaultColor = getColor(R.color.quickparcel_gray)

        binding.btnSortDate.setTextColor(if (currentSort.startsWith("date")) selectedColor else defaultColor)
        binding.btnSortStatus.setTextColor(if (currentSort == "status") selectedColor else defaultColor)
        binding.btnSortAmount.setTextColor(if (currentSort == "amount") selectedColor else defaultColor)

        when (currentSort) {
            "date_asc" -> binding.btnSortDate.text = "📅 Oldest First"
            "date_desc" -> binding.btnSortDate.text = "📅 Newest First"
            else -> binding.btnSortDate.text = "📅 Date"
        }
    }

    private fun updateFilterUI() {
        val selectedColor = getColor(R.color.quickparcel_blue)
        val defaultColor = getColor(R.color.quickparcel_gray)
        val selectedBg = getDrawable(R.drawable.bg_filter_selected)
        val defaultBg = getDrawable(R.drawable.bg_filter_default)

        binding.btnFilterAll.apply {
            setTextColor(if (currentFilter == "ALL") selectedColor else defaultColor)
            background = if (currentFilter == "ALL") selectedBg else defaultBg
        }
        binding.btnFilterPending.apply {
            setTextColor(if (currentFilter == "PENDING") selectedColor else defaultColor)
            background = if (currentFilter == "PENDING") selectedBg else defaultBg
        }
        binding.btnFilterAccepted.apply {
            setTextColor(if (currentFilter == "ACCEPTED") selectedColor else defaultColor)
            background = if (currentFilter == "ACCEPTED") selectedBg else defaultBg
        }
        binding.btnFilterIntransit.apply {
            setTextColor(if (currentFilter == "IN_TRANSIT") selectedColor else defaultColor)
            background = if (currentFilter == "IN_TRANSIT") selectedBg else defaultBg
        }
        binding.btnFilterDelivered.apply {
            setTextColor(if (currentFilter == "DELIVERED") selectedColor else defaultColor)
            background = if (currentFilter == "DELIVERED") selectedBg else defaultBg
        }
    }

    private fun applySortingAndFiltering() {
        var filtered = when (currentFilter) {
            "ALL" -> allDeliveries
            "IN_TRANSIT" -> allDeliveries.filter { it.status in listOf("ACCEPTED", "PICKED_UP", "IN_TRANSIT") }
            else -> allDeliveries.filter { it.status == currentFilter }
        }

        val sorted = when (currentSort) {
            "date_asc" -> filtered.sortedBy { it.createdAt }
            "date_desc" -> filtered.sortedByDescending { it.createdAt }
            "status" -> filtered.sortedBy { it.status }
            "amount" -> filtered.sortedByDescending { it.estimatedCost }
            else -> filtered
        }

        deliveryAdapter.updateDeliveries(sorted)
        binding.tvCount.text = "${sorted.size} deliveries"

        if (sorted.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvDeliveries.visibility = android.view.View.GONE
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.rvDeliveries.visibility = android.view.View.VISIBLE
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            trackingViewModel.deliveriesResult.collect { state ->
                when (state) {
                    is TrackingState.Loading -> showLoading(true)
                    is TrackingState.DeliveriesSuccess -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        allDeliveries = state.deliveries
                        updateUI(state.deliveries, state.stats)
                        applySortingAndFiltering()
                    }
                    is TrackingState.Error -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(this@MyDeliveriesActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun updateUI(deliveries: List<com.quickparcel.app.shared.models.Delivery>, stats: TrackingModels.DeliveryStats) {
        binding.tvTotalDeliveries.text = stats.total.toString()
        binding.tvActiveDeliveries.text = stats.active.toString()
        binding.tvCompletedDeliveries.text = stats.completed.toString()
        binding.tvTotalSpent.text = "₱${String.format("%.2f", stats.totalSpent)}"
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}