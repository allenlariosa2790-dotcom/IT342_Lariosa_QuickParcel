package com.quickparcel.app.features.rider

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityAvailableDeliveriesBinding
import com.quickparcel.app.features.sender.DeliveryAdapter
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class AvailableDeliveriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAvailableDeliveriesBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var riderViewModel: RiderViewModel
    private lateinit var deliveryAdapter: DeliveryAdapter

    private var allDeliveries: List<com.quickparcel.app.shared.models.Delivery> = emptyList()
    private var currentSort = "distance" // distance, earnings, size, date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvailableDeliveriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        riderViewModel = RiderViewModel(retrofitClient)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        riderViewModel.loadAvailableDeliveries()
    }

    private fun setupRecyclerView() {
        deliveryAdapter = DeliveryAdapter(
            deliveries = emptyList(),
            onItemClick = { delivery ->
                showAcceptDialog(delivery)
            },
            isRiderMode = false,
            onStatusUpdate = null
        )
        binding.rvDeliveries.layoutManager = LinearLayoutManager(this)
        binding.rvDeliveries.adapter = deliveryAdapter
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            riderViewModel.loadAvailableDeliveries()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Sort buttons
        binding.btnSortDistance.setOnClickListener {
            currentSort = "distance"
            updateSortUI()
            applySorting()
        }

        binding.btnSortEarnings.setOnClickListener {
            currentSort = "earnings"
            updateSortUI()
            applySorting()
        }

        binding.btnSortSize.setOnClickListener {
            currentSort = "size"
            updateSortUI()
            applySorting()
        }

        binding.btnSortDate.setOnClickListener {
            currentSort = "date"
            updateSortUI()
            applySorting()
        }
    }

    private fun updateSortUI() {
        val selectedColor = getColor(R.color.quickparcel_blue)
        val defaultColor = getColor(R.color.quickparcel_gray)
        val selectedBg = getDrawable(R.drawable.bg_filter_selected)
        val defaultBg = getDrawable(R.drawable.bg_filter_default)

        binding.btnSortDistance.apply {
            setTextColor(if (currentSort == "distance") selectedColor else defaultColor)
            background = if (currentSort == "distance") selectedBg else defaultBg
        }

        binding.btnSortEarnings.apply {
            setTextColor(if (currentSort == "earnings") selectedColor else defaultColor)
            background = if (currentSort == "earnings") selectedBg else defaultBg
        }

        binding.btnSortSize.apply {
            setTextColor(if (currentSort == "size") selectedColor else defaultColor)
            background = if (currentSort == "size") selectedBg else defaultBg
        }

        binding.btnSortDate.apply {
            setTextColor(if (currentSort == "date") selectedColor else defaultColor)
            background = if (currentSort == "date") selectedBg else defaultBg
        }
    }

    private fun applySorting() {
        val sorted = when (currentSort) {
            "distance" -> allDeliveries.sortedBy { it.distance ?: 999.0 }
            "earnings" -> allDeliveries.sortedByDescending { it.estimatedCost }
            "size" -> {
                val sizeOrder = mapOf("SMALL" to 1, "MEDIUM" to 2, "LARGE" to 3)
                allDeliveries.sortedBy { sizeOrder[it.parcel?.size] ?: 2 }
            }
            "date" -> allDeliveries.sortedByDescending { it.createdAt }
            else -> allDeliveries
        }

        deliveryAdapter = DeliveryAdapter(
            deliveries = sorted,
            onItemClick = { delivery ->
                showAcceptDialog(delivery)
            },
            isRiderMode = false,
            onStatusUpdate = null
        )
        binding.rvDeliveries.adapter = deliveryAdapter
        binding.tvCount.text = "${sorted.size} deliveries available"

        if (sorted.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvDeliveries.visibility = android.view.View.GONE
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.rvDeliveries.visibility = android.view.View.VISIBLE
        }
    }

    private fun showAcceptDialog(delivery: com.quickparcel.app.shared.models.Delivery) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Accept Delivery")
            .setMessage("Accept delivery ${delivery.trackingNumber} for ₱${String.format("%.2f", delivery.estimatedCost)}?")
            .setPositiveButton("Accept") { _, _ ->
                riderViewModel.acceptDelivery(delivery.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            riderViewModel.availableResult.collect { state ->
                when (state) {
                    is RiderAvailableState.Loading -> showLoading(true)
                    is RiderAvailableState.Success -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        allDeliveries = state.deliveries
                        applySorting()
                    }
                    is RiderAvailableState.Error -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(this@AvailableDeliveriesActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            riderViewModel.acceptResult.collect { state ->
                when (state) {
                    is RiderAcceptState.Loading -> {
                        Toast.makeText(this@AvailableDeliveriesActivity, "Accepting...", Toast.LENGTH_SHORT).show()
                    }
                    is RiderAcceptState.Success -> {
                        Toast.makeText(this@AvailableDeliveriesActivity, "✅ Delivery accepted!", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    is RiderAcceptState.Error -> {
                        Toast.makeText(this@AvailableDeliveriesActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}