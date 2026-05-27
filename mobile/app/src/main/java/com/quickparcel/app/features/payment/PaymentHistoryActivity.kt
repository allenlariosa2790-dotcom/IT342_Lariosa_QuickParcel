package com.quickparcel.app.features.payment

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityPaymentHistoryBinding
import com.quickparcel.app.features.tracking.TrackingActivity
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import com.quickparcel.app.shared.models.Delivery
import kotlinx.coroutines.launch

class PaymentHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentHistoryBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var paymentAdapter: PaymentAdapter

    private var filter = "ALL" // ALL, PAID, PENDING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        paymentViewModel = PaymentViewModel(retrofitClient)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        paymentViewModel.loadPayments()
    }

    private fun setupRecyclerView() {
        paymentAdapter = PaymentAdapter(emptyList()) { delivery ->
            val intent = Intent(this, TrackingActivity::class.java)
            intent.putExtra("delivery_id", delivery.id)
            intent.putExtra("tracking_number", delivery.trackingNumber)
            startActivity(intent)
        }
        binding.rvPayments.layoutManager = LinearLayoutManager(this)
        binding.rvPayments.adapter = paymentAdapter
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            paymentViewModel.loadPayments()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnFilterAll.setOnClickListener {
            filter = "ALL"
            updateFilterUI()
            paymentAdapter.filter(filter)
        }

        binding.btnFilterPaid.setOnClickListener {
            filter = "PAID"
            updateFilterUI()
            paymentAdapter.filter(filter)
        }

        binding.btnFilterPending.setOnClickListener {
            filter = "PENDING"
            updateFilterUI()
            paymentAdapter.filter(filter)
        }
    }

    private fun updateFilterUI() {
        val selectedColor = getColor(R.color.quickparcel_blue)
        val defaultColor = getColor(R.color.quickparcel_gray)
        val selectedBg = getDrawable(R.drawable.bg_filter_selected)
        val defaultBg = getDrawable(R.drawable.bg_filter_default)

        binding.btnFilterAll.apply {
            setTextColor(if (filter == "ALL") selectedColor else defaultColor)
            background = if (filter == "ALL") selectedBg else defaultBg
        }
        binding.btnFilterPaid.apply {
            setTextColor(if (filter == "PAID") selectedColor else defaultColor)
            background = if (filter == "PAID") selectedBg else defaultBg
        }
        binding.btnFilterPending.apply {
            setTextColor(if (filter == "PENDING") selectedColor else defaultColor)
            background = if (filter == "PENDING") selectedBg else defaultBg
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            paymentViewModel.paymentsResult.collect { state ->
                when (state) {
                    is PaymentsState.Loading -> showLoading(true)
                    is PaymentsState.Success -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        updateUI(state.deliveries, state.stats)
                    }
                    is PaymentsState.Error -> {
                        showLoading(false)
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(this@PaymentHistoryActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateUI(deliveries: List<Delivery>, stats: PaymentModels.PaymentStats) {
        binding.tvTotalSpent.text = "₱${String.format("%.2f", stats.totalSpent)}"
        binding.tvPendingCount.text = stats.pendingCount.toString()
        binding.tvTotalTransactions.text = stats.totalTransactions.toString()

        paymentAdapter.updateDeliveries(deliveries)

        if (deliveries.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvPayments.visibility = android.view.View.GONE
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.rvPayments.visibility = android.view.View.VISIBLE
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}