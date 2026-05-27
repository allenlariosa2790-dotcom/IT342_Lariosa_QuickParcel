package com.quickparcel.app.features.tracking

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var currentSort = "date_desc" // date_desc, date_asc, status, amount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyDeliveriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        trackingViewModel = TrackingViewModel(retrofitClient)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        trackingViewModel.loadMyDeliveries()
    }

    private fun setupRecyclerView() {
        deliveryAdapter = DeliveryAdapter(emptyList()) { delivery ->
            val intent = Intent(this, TrackingActivity::class.java)
            intent.putExtra("delivery_id", delivery.id)
            intent.putExtra("tracking_number", delivery.trackingNumber)
            startActivity(intent)
        }
        binding.rvDeliveries.layoutManager = LinearLayoutManager(this)
        binding.rvDeliveries.adapter = deliveryAdapter
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
            applySorting()
        }

        binding.btnSortStatus.setOnClickListener {
            currentSort = "status"
            updateSortUI()
            applySorting()
        }

        binding.btnSortAmount.setOnClickListener {
            currentSort = "amount"
            updateSortUI()
            applySorting()
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

    private fun applySorting() {
        val sorted = when (currentSort) {
            "date_asc" -> allDeliveries.sortedBy { it.createdAt }
            "date_desc" -> allDeliveries.sortedByDescending { it.createdAt }
            "status" -> allDeliveries.sortedBy { it.status }
            "amount" -> allDeliveries.sortedByDescending { it.estimatedCost }
            else -> allDeliveries
        }

        deliveryAdapter = DeliveryAdapter(sorted) { delivery ->
            val intent = Intent(this, TrackingActivity::class.java)
            intent.putExtra("delivery_id", delivery.id)
            intent.putExtra("tracking_number", delivery.trackingNumber)
            startActivity(intent)
        }
        binding.rvDeliveries.adapter = deliveryAdapter
        binding.tvCount.text = "${sorted.size} deliveries"
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
                        applySorting()
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
        binding.tvCount.text = "${deliveries.size} deliveries"

        if (deliveries.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvDeliveries.visibility = android.view.View.GONE
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.rvDeliveries.visibility = android.view.View.VISIBLE
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}