package com.quickparcel.app.features.payment

import android.content.Intent
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
        setupCardInputFormatting()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.tvTrackingNumber.text = trackingNumber
        binding.tvAmount.text = "₱${String.format("%.2f", amount)}"
        binding.tvTotal.text = "₱${String.format("%.2f", amount)}"
    }

    private fun setupCardInputFormatting() {
        // Auto-format card number
        binding.etCardNumber.addTextChangedListener(object : android.text.TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (isFormatting) return
                isFormatting = true

                val cleanDigits = s?.toString()?.replace(" ", "") ?: ""
                val truncated = if (cleanDigits.length > 16) cleanDigits.substring(0, 16) else cleanDigits
                val formatted = StringBuilder()
                for (i in truncated.indices) {
                    if (i > 0 && i % 4 == 0) formatted.append(" ")
                    formatted.append(truncated[i])
                }
                val newText = formatted.toString()
                if (s?.toString() != newText) {
                    s?.replace(0, s.length, newText)
                    s?.let { android.text.Selection.setSelection(it, newText.length) }
                }
                isFormatting = false
            }
        })

        // Auto-format expiry
        binding.etExpiry.addTextChangedListener(object : android.text.TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (isFormatting) return
                isFormatting = true

                val cleanDigits = s?.toString()?.replace("/", "") ?: ""
                val truncated = if (cleanDigits.length > 4) cleanDigits.substring(0, 4) else cleanDigits
                val formatted = when {
                    truncated.length >= 2 -> "${truncated.substring(0, 2)}/${truncated.substring(2)}"
                    else -> truncated
                }
                if (s?.toString() != formatted) {
                    s?.replace(0, s.length, formatted)
                    s?.let { android.text.Selection.setSelection(it, formatted.length) }
                }
                isFormatting = false
            }
        })

        // CVV limit
        binding.etCvv.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val text = s?.toString() ?: ""
                if (text.length > 4) s?.delete(4, text.length)
            }
        })

        // ZIP limit
        binding.etZip.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val text = s?.toString() ?: ""
                if (text.length > 6) s?.delete(6, text.length)
            }
        })
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
        val cardNumber = binding.etCardNumber.text.toString().trim().replace(" ", "")
        val expiry = binding.etExpiry.text.toString().trim()
        val cvv = binding.etCvv.text.toString().trim()
        val zip = binding.etZip.text.toString().trim()

        if (cardNumber.isEmpty()) {
            Toast.makeText(this, "Please enter card number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (cardNumber.length != 16) {
            Toast.makeText(this, "Card number must be 16 digits", Toast.LENGTH_SHORT).show()
            return false
        }
        if (expiry.isEmpty()) {
            Toast.makeText(this, "Please enter expiry date", Toast.LENGTH_SHORT).show()
            return false
        }
        if (expiry.length != 5 || !expiry.contains("/")) {
            Toast.makeText(this, "Use format MM/YY (e.g., 12/25)", Toast.LENGTH_SHORT).show()
            return false
        }
        val month = expiry.substring(0, 2).toIntOrNull()
        if (month == null || month < 1 || month > 12) {
            Toast.makeText(this, "Invalid month (01-12)", Toast.LENGTH_SHORT).show()
            return false
        }
        if (cvv.isEmpty()) {
            Toast.makeText(this, "Please enter CVV", Toast.LENGTH_SHORT).show()
            return false
        }
        if (cvv.length !in 3..4) {
            Toast.makeText(this, "CVV must be 3-4 digits", Toast.LENGTH_SHORT).show()
            return false
        }
        if (zip.isEmpty()) {
            Toast.makeText(this, "Please enter ZIP code", Toast.LENGTH_SHORT).show()
            return false
        }

        if (cardNumber.startsWith("4242")) {
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
                        Toast.makeText(this@PaymentActivity, "✅ Payment successful!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@PaymentActivity, SenderDashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
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