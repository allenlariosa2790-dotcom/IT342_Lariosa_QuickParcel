package com.quickparcel.app.features.rider

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityActiveDeliveriesBinding
import com.quickparcel.app.features.sender.DeliveryAdapter
import com.quickparcel.app.features.tracking.TrackingActivity
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class ActiveDeliveriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActiveDeliveriesBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var riderViewModel: RiderViewModel
    private lateinit var deliveryAdapter: DeliveryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActiveDeliveriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        riderViewModel = RiderViewModel(retrofitClient)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

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
            isRiderMode = true,
            onStatusUpdate = { delivery, newStatus ->
                updateDeliveryStatus(delivery.id, newStatus)
            }
        )
        binding.rvActiveDeliveries.layoutManager = LinearLayoutManager(this)
        binding.rvActiveDeliveries.adapter = deliveryAdapter
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
            riderViewModel.loadActiveDeliveries()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            riderViewModel.activeDeliveriesResult.collect { state ->
                when (state) {
                    is RiderActiveDeliveriesState.Loading -> showLoading(true)
                    is RiderActiveDeliveriesState.Success -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        updateUI(state.deliveries)
                    }
                    is RiderActiveDeliveriesState.Error -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(this@ActiveDeliveriesActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            riderViewModel.statusResult.collect { state ->
                when (state) {
                    is RiderStatusState.Success -> {
                        Toast.makeText(this@ActiveDeliveriesActivity, "✅ Status updated to ${state.delivery.status}", Toast.LENGTH_SHORT).show()
                        riderViewModel.loadActiveDeliveries() // Refresh list
                    }
                    is RiderStatusState.Error -> {
                        Toast.makeText(this@ActiveDeliveriesActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun updateUI(deliveries: List<com.quickparcel.app.shared.models.Delivery>) {
        deliveryAdapter.updateDeliveries(deliveries)
        binding.tvCount.text = "${deliveries.size} active deliveries"

        if (deliveries.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvActiveDeliveries.visibility = android.view.View.GONE
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.rvActiveDeliveries.visibility = android.view.View.VISIBLE
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}