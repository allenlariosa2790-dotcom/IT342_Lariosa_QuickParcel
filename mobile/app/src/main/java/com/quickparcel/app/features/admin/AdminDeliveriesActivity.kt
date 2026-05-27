package com.quickparcel.app.features.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityAdminDeliveriesBinding
import com.quickparcel.app.features.sender.DeliveryAdapter
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class AdminDeliveriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDeliveriesBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var adminViewModel: AdminViewModel
    private lateinit var deliveryAdapter: DeliveryAdapter

    private var allDeliveries: List<com.quickparcel.app.shared.models.Delivery> = emptyList()
    private var currentFilter = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDeliveriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        adminViewModel = AdminViewModel(retrofitClient)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        adminViewModel.loadAllDeliveries()
    }

    private fun setupRecyclerView() {
        deliveryAdapter = DeliveryAdapter(
            deliveries = emptyList(),
            onItemClick = { delivery ->
                showDeliveryDetailsDialog(delivery)
            },
            isRiderMode = false,
            onStatusUpdate = null
        )
        binding.rvDeliveries.layoutManager = LinearLayoutManager(this)
        binding.rvDeliveries.adapter = deliveryAdapter
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            adminViewModel.loadAllDeliveries()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnFilterAll.setOnClickListener {
            currentFilter = "ALL"
            updateFilterUI()
            filterDeliveries()
        }

        binding.btnFilterPending.setOnClickListener {
            currentFilter = "PENDING"
            updateFilterUI()
            filterDeliveries()
        }

        binding.btnFilterTransit.setOnClickListener {
            currentFilter = "IN_TRANSIT"
            updateFilterUI()
            filterDeliveries()
        }

        binding.btnFilterDelivered.setOnClickListener {
            currentFilter = "DELIVERED"
            updateFilterUI()
            filterDeliveries()
        }

        binding.btnFilterCancelled.setOnClickListener {
            currentFilter = "CANCELLED"
            updateFilterUI()
            filterDeliveries()
        }
    }

    private fun updateFilterUI() {
        val selectedColor = ContextCompat.getColor(this, R.color.white)
        val defaultColor = ContextCompat.getColor(this, R.color.quickparcel_gray)
        val selectedBg = ContextCompat.getDrawable(this, R.drawable.bg_filter_selected)
        val defaultBg = ContextCompat.getDrawable(this, R.drawable.bg_filter_default)

        binding.btnFilterAll.apply {
            setTextColor(if (currentFilter == "ALL") selectedColor else defaultColor)
            background = if (currentFilter == "ALL") selectedBg else defaultBg
        }
        binding.btnFilterPending.apply {
            setTextColor(if (currentFilter == "PENDING") selectedColor else defaultColor)
            background = if (currentFilter == "PENDING") selectedBg else defaultBg
        }
        binding.btnFilterTransit.apply {
            setTextColor(if (currentFilter == "IN_TRANSIT") selectedColor else defaultColor)
            background = if (currentFilter == "IN_TRANSIT") selectedBg else defaultBg
        }
        binding.btnFilterDelivered.apply {
            setTextColor(if (currentFilter == "DELIVERED") selectedColor else defaultColor)
            background = if (currentFilter == "DELIVERED") selectedBg else defaultBg
        }
        binding.btnFilterCancelled.apply {
            setTextColor(if (currentFilter == "CANCELLED") selectedColor else defaultColor)
            background = if (currentFilter == "CANCELLED") selectedBg else defaultBg
        }
    }

    private fun filterDeliveries() {
        val filtered = when (currentFilter) {
            "ALL" -> allDeliveries
            "IN_TRANSIT" -> allDeliveries.filter { it.status in listOf("ACCEPTED", "PICKED_UP", "IN_TRANSIT") }
            else -> allDeliveries.filter { it.status == currentFilter }
        }

        deliveryAdapter.updateDeliveries(filtered)
        binding.tvCount.text = "${filtered.size} deliveries"

        if (filtered.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvDeliveries.visibility = android.view.View.GONE
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.rvDeliveries.visibility = android.view.View.VISIBLE
        }
    }

    private fun showDeliveryDetailsDialog(delivery: com.quickparcel.app.shared.models.Delivery) {
        val senderName = when {
            delivery.sender?.user != null ->
                "${delivery.sender.user.firstName} ${delivery.sender.user.lastName}"
            delivery.parcel?.sender?.user != null ->
                "${delivery.parcel.sender.user.firstName} ${delivery.parcel.sender.user.lastName}"
            else -> "Unknown"
        }

        val riderName = delivery.rider?.user?.let {
            "${it.firstName} ${it.lastName}"
        } ?: "Unassigned"

        val parcelName = delivery.parcel?.name ?: "N/A"
        val parcelSize = delivery.parcel?.size ?: "N/A"
        val parcelWeight = delivery.parcel?.weight?.let { "${it} kg" } ?: "N/A"

        val message = """
            📦 Tracking: ${delivery.trackingNumber}
            📍 Status: ${delivery.status}
            
            📦 Parcel: $parcelName ($parcelSize, $parcelWeight)
            👤 Sender: $senderName
            🏍️ Rider: $riderName
            
            📍 Pickup: ${delivery.pickupAddress.take(60)}...
            🏁 Dropoff: ${delivery.dropoffAddress.take(60)}...
            
            📏 Distance: ${delivery.distance?.let { String.format("%.2f", it) } ?: "--"} km
            💰 Amount: ₱${String.format("%.2f", delivery.estimatedCost)}
            
            💳 Payment: ${delivery.paymentMethod ?: "N/A"} - ${delivery.paymentStatus ?: "PENDING"}
            📅 Created: ${delivery.createdAt.take(16)}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delivery Details")
            .setMessage(message)
            .setPositiveButton("Cancel Delivery") { _, _ ->
                if (delivery.status !in listOf("DELIVERED", "CANCELLED")) {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Confirm Cancel")
                        .setMessage("Are you sure you want to cancel delivery ${delivery.trackingNumber}?")
                        .setPositiveButton("Yes") { _, _ ->
                            adminViewModel.cancelDelivery(delivery.id)
                        }
                        .setNegativeButton("No", null)
                        .show()
                } else {
                    Toast.makeText(this, "Cannot cancel ${delivery.status} delivery", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Close", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            adminViewModel.deliveriesResult.collect { state ->
                when (state) {
                    is AdminDeliveriesState.Loading -> showLoading(true)
                    is AdminDeliveriesState.Success -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        allDeliveries = state.deliveries
                        filterDeliveries()
                    }
                    is AdminDeliveriesState.Error -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(this@AdminDeliveriesActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            adminViewModel.cancelDeliveryResult.collect { state ->
                when (state) {
                    is AdminCancelDeliveryState.Loading -> showLoading(true)
                    is AdminCancelDeliveryState.Success -> {
                        showLoading(false)
                        Toast.makeText(this@AdminDeliveriesActivity, state.message, Toast.LENGTH_SHORT).show()
                        adminViewModel.loadAllDeliveries()
                    }
                    is AdminCancelDeliveryState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@AdminDeliveriesActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}