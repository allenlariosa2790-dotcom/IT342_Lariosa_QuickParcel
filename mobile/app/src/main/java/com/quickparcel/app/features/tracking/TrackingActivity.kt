package com.quickparcel.app.features.tracking

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityTrackingBinding
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import com.quickparcel.app.shared.utils.Constants
import kotlinx.coroutines.launch

class TrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackingBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var trackingViewModel: TrackingViewModel
    private lateinit var historyAdapter: TrackingHistoryAdapter

    private var deliveryId: Int = 0
    private var trackingNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityTrackingBinding.inflate(layoutInflater)
            setContentView(binding.root)

            deliveryId = intent.getIntExtra("delivery_id", 0)
            trackingNumber = intent.getStringExtra("tracking_number") ?: ""

            if (deliveryId == 0) {
                Toast.makeText(this, "Invalid delivery ID", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            tokenManager = TokenManager(applicationContext)
            retrofitClient = RetrofitClient(tokenManager)
            trackingViewModel = TrackingViewModel(retrofitClient)

            setupRecyclerView()
            setupListeners()
            observeViewModel()

            trackingViewModel.loadDeliveryDetails(deliveryId)
            trackingViewModel.loadTrackingHistory(deliveryId)
            trackingViewModel.loadParcelImage(deliveryId)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = TrackingHistoryAdapter(emptyList())
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = historyAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnRefresh.setOnClickListener {
            trackingViewModel.loadDeliveryDetails(deliveryId)
            trackingViewModel.loadTrackingHistory(deliveryId)
            trackingViewModel.loadParcelImage(deliveryId)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            trackingViewModel.deliveryDetailsResult.collect { state ->
                when (state) {
                    is TrackingState.DeliveryDetailsLoading -> showLoading(true)
                    is TrackingState.DeliveryDetailsSuccess -> {
                        showLoading(false)
                        updateDeliveryUI(state.delivery)
                    }
                    is TrackingState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@TrackingActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            trackingViewModel.historyResult.collect { state ->
                when (state) {
                    is TrackingState.HistorySuccess -> {
                        historyAdapter.updateHistory(state.history)
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            trackingViewModel.imageResult.collect { state ->
                when (state) {
                    is TrackingState.ImageSuccess -> {
                        binding.ivParcelImage.visibility = android.view.View.VISIBLE
                        val baseIp = Constants.BASE_URL.replace("http://", "").replace("/", "")
                        val imageUrl = "http://$baseIp${state.imageUrl}?t=${System.currentTimeMillis()}"
                        Glide.with(this@TrackingActivity)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(binding.ivParcelImage)
                    }
                    else -> {
                        binding.ivParcelImage.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }

    private fun updateDeliveryUI(delivery: com.quickparcel.app.shared.models.Delivery) {
        binding.tvTrackingNumber.text = delivery.trackingNumber
        binding.tvStatus.text = delivery.status
        binding.tvPickupAddress.text = delivery.pickupAddress
        binding.tvDropoffAddress.text = delivery.dropoffAddress
        binding.tvEstimatedCost.text = "₱${String.format("%.2f", delivery.estimatedCost)}"

        if (delivery.distance != null) {
            binding.tvDistance.text = "${String.format("%.2f", delivery.distance)} km"
        } else {
            binding.tvDistance.text = "-- km"
        }

        if (delivery.parcel != null) {
            binding.tvParcelName.text = delivery.parcel.name
            binding.tvParcelWeight.text = "${delivery.parcel.weight} kg"
            binding.tvParcelSize.text = delivery.parcel.size
        }

        // Update status badge color
        val bgRes = when (delivery.status) {
            "PENDING" -> R.drawable.bg_status_pending
            "ACCEPTED" -> R.drawable.bg_status_accepted
            "PICKED_UP", "IN_TRANSIT" -> R.drawable.bg_status_accepted
            "DELIVERED" -> R.drawable.bg_status_delivered
            "CANCELLED" -> R.drawable.bg_status_pending
            else -> R.drawable.bg_status_pending
        }
        binding.tvStatus.setBackgroundResource(bgRes)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}