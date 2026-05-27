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
        deliveryAdapter = DeliveryAdapter(emptyList()) { delivery ->
            showAcceptDialog(delivery)
        }
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
    }

    private fun showAcceptDialog(delivery: com.quickparcel.app.shared.models.Delivery) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Accept Delivery")
            .setMessage("Do you want to accept delivery ${delivery.trackingNumber} for ₱${String.format("%.2f", delivery.estimatedCost)}?")
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
                        updateUI(state.deliveries)
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
                        Toast.makeText(this@AvailableDeliveriesActivity, "Delivery accepted successfully!", Toast.LENGTH_LONG).show()
                        riderViewModel.loadAvailableDeliveries()
                        riderViewModel.loadDashboardData()
                        finish()
                    }
                    is RiderAcceptState.Error -> {
                        Toast.makeText(this@AvailableDeliveriesActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateUI(deliveries: List<com.quickparcel.app.shared.models.Delivery>) {
        binding.tvCount.text = "${deliveries.size} deliveries available"

        deliveryAdapter = DeliveryAdapter(deliveries) { delivery ->
            showAcceptDialog(delivery)
        }
        binding.rvDeliveries.adapter = deliveryAdapter

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