package com.quickparcel.app.features.payment

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityPaymentBinding
import com.quickparcel.app.features.sender.SenderDashboardActivity
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var paymentViewModel: PaymentViewModel

    private var deliveryId: Int = 0
    private var amount: Double = 0.0
    private var trackingNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deliveryId = intent.getIntExtra("delivery_id", 0)
        amount = intent.getDoubleExtra("amount", 0.0)
        trackingNumber = intent.getStringExtra("tracking_number") ?: ""

        if (deliveryId == 0 || amount == 0.0) {
            Toast.makeText(this, "Invalid payment data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        paymentViewModel = PaymentViewModel(retrofitClient)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.tvTrackingNumber.text = trackingNumber
        binding.tvAmount.text = "₱${String.format("%.2f", amount)}"
        binding.tvTotal.text = "₱${String.format("%.2f", amount)}"
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnPayNow.setOnClickListener {
            if (validateCardDetails()) {
                processPayment()
            }
        }
    }

    private fun validateCardDetails(): Boolean {
        val cardNumber = binding.etCardNumber.text.toString().trim()
        val expiry = binding.etExpiry.text.toString().trim()
        val cvv = binding.etCvv.text.toString().trim()

        if (cardNumber.isEmpty()) {
            Toast.makeText(this, "Please enter card number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (expiry.isEmpty()) {
            Toast.makeText(this, "Please enter expiry date", Toast.LENGTH_SHORT).show()
            return false
        }
        if (cvv.isEmpty()) {
            Toast.makeText(this, "Please enter CVV", Toast.LENGTH_SHORT).show()
            return false
        }

        // For test mode, accept test card
        val cleanCardNumber = cardNumber.replace(" ", "")
        if (cleanCardNumber.startsWith("4242")) {
            return true
        }

        Toast.makeText(this, "Use test card: 4242 4242 4242 4242", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun processPayment() {
        val description = "Delivery $trackingNumber"
        paymentViewModel.createPaymentIntent(deliveryId, amount, description)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            paymentViewModel.paymentIntentResult.collect { state ->
                when (state) {
                    is PaymentIntentState.Loading -> showLoading(true)
                    is PaymentIntentState.Success -> {
                        // Payment intent created successfully, now mark as paid
                        paymentViewModel.markDeliveryAsPaid(deliveryId)
                    }
                    is PaymentIntentState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@PaymentActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            paymentViewModel.markPaidResult.collect { state ->
                when (state) {
                    is MarkPaidState.Loading -> showLoading(true)
                    is MarkPaidState.Success -> {
                        showLoading(false)
                        Toast.makeText(this@PaymentActivity, "✅ Payment successful! Delivery has been published.", Toast.LENGTH_LONG).show()
                        startActivity(android.content.Intent(this@PaymentActivity, SenderDashboardActivity::class.java))
                        finish()
                    }
                    is MarkPaidState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@PaymentActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnPayNow.isEnabled = !show
    }
}