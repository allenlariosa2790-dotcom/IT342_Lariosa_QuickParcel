package com.quickparcel.app.features.delivery

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityCreateDeliveryBinding
import com.quickparcel.app.databinding.DialogSizePickerBinding
import com.quickparcel.app.features.payment.PaymentActivity
import com.quickparcel.app.features.sender.SenderDashboardActivity
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreateDeliveryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateDeliveryBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var deliveryViewModel: DeliveryViewModel

    private var currentStep = 1
    private var selectedSize = "MEDIUM"
    private var calculatedDistance = 0.0
    private var calculatedCost = 0.0
    private var isCalculatingDistance = false

    // Store coordinates from map picker
    private var pickupLat: Double? = null
    private var pickupLng: Double? = null
    private var dropoffLat: Double? = null
    private var dropoffLng: Double? = null

    // Map picker launchers
    private val pickupMapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val address = result.data?.getStringExtra("address") ?: ""
            val lat = result.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            val lng = result.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
            binding.etPickupAddress.setText(address)
            pickupLat = lat
            pickupLng = lng
        }
    }

    private val dropoffMapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val address = result.data?.getStringExtra("address") ?: ""
            val lat = result.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            val lng = result.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
            binding.etDropoffAddress.setText(address)
            dropoffLat = lat
            dropoffLng = lng
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        deliveryViewModel = DeliveryViewModel(retrofitClient)

        setupSizePicker()
        setupDateTimePicker()
        setupStepNavigation()
        setupPaymentSelection()
        setupMapButtons()
        observeViewModel()

        updateStepUI()
    }

    private fun setupMapButtons() {
        binding.btnPickupMap.setOnClickListener {
            val intent = Intent(this, OSMLocationPickerActivity::class.java)
            intent.putExtra("is_pickup", true)
            pickupMapLauncher.launch(intent)
        }

        binding.btnDropoffMap.setOnClickListener {
            val intent = Intent(this, OSMLocationPickerActivity::class.java)
            intent.putExtra("is_pickup", false)
            dropoffMapLauncher.launch(intent)
        }
    }

    private fun setupSizePicker() {
        binding.etSize.setOnClickListener {
            showSizePickerDialog()
        }
        binding.etSize.isFocusable = false
        binding.etSize.isClickable = true
    }

    private fun setupDateTimePicker() {
        binding.etScheduledTime.setOnClickListener {
            showDateTimePicker()
        }
        binding.etScheduledTime.isFocusable = false
        binding.etScheduledTime.isClickable = true
    }

    private fun showSizePickerDialog() {
        val dialogBinding = DialogSizePickerBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        when (selectedSize) {
            "SMALL" -> dialogBinding.rbSmall.isChecked = true
            "MEDIUM" -> dialogBinding.rbMedium.isChecked = true
            "LARGE" -> dialogBinding.rbLarge.isChecked = true
        }

        dialogBinding.btnConfirmSize.setOnClickListener {
            selectedSize = when {
                dialogBinding.rbSmall.isChecked -> "SMALL"
                dialogBinding.rbMedium.isChecked -> "MEDIUM"
                dialogBinding.rbLarge.isChecked -> "LARGE"
                else -> "MEDIUM"
            }
            val sizeText = selectedSize.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            binding.etSize.setText(sizeText)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)

        val minDate = Calendar.getInstance()
        minDate.add(Calendar.DAY_OF_YEAR, 1)

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)

                if (selectedCalendar.before(minDate)) {
                    Toast.makeText(this, "Please select a future date (tomorrow or later)", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }

                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        if (calendar.timeInMillis < System.currentTimeMillis()) {
                            Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show()
                            return@TimePickerDialog
                        }

                        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        binding.etScheduledTime.setText(format.format(calendar.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.minDate = minDate.timeInMillis
        datePicker.show()
    }

    private fun setupStepNavigation() {
        binding.btnNextStep1.setOnClickListener {
            if (validateStep1()) {
                goToStep(2)
            }
        }

        binding.btnPrevStep2.setOnClickListener { goToStep(1) }
        binding.btnNextStep2.setOnClickListener {
            if (validateStep2()) {
                calculateDistanceAndMoveToStep3()
            }
        }

        binding.btnPrevStep3.setOnClickListener { goToStep(2) }
        binding.btnConfirm.setOnClickListener {
            createDelivery()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupPaymentSelection() {
        // Set up radio group for mutual exclusivity
        binding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_cod -> {
                    binding.rbCod.isChecked = true
                    binding.rbStripe.isChecked = false
                    binding.llCardDetails.visibility = android.view.View.GONE
                    updatePaymentCardSelection()
                }
                R.id.rb_stripe -> {
                    binding.rbCod.isChecked = false
                    binding.rbStripe.isChecked = true
                    binding.llCardDetails.visibility = android.view.View.VISIBLE
                    updatePaymentCardSelection()
                }
            }
        }

        // Card click listeners
        binding.cardCod.setOnClickListener {
            binding.rgPaymentMethod.check(R.id.rb_cod)
        }

        binding.cardStripe.setOnClickListener {
            binding.rgPaymentMethod.check(R.id.rb_stripe)
        }

        // Initialize with COD selected
        binding.llCardDetails.visibility = android.view.View.GONE
        updatePaymentCardSelection()
    }

    private fun updatePaymentCardSelection() {
        updateCardStyle(binding.cardCod, binding.rbCod.isChecked)
        updateCardStyle(binding.cardStripe, binding.rbStripe.isChecked)
    }

    private fun updateCardStyle(card: MaterialCardView, isSelected: Boolean) {
        if (isSelected) {
            card.strokeWidth = 2
            card.strokeColor = getColor(R.color.quickparcel_blue)
        } else {
            card.strokeWidth = 0
        }
    }

    private fun validateStep1(): Boolean {
        val name = binding.etParcelName.text.toString().trim()
        val weight = binding.etWeight.text.toString().trim()
        val category = binding.etCategory.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter parcel name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (weight.isEmpty()) {
            Toast.makeText(this, "Please enter weight", Toast.LENGTH_SHORT).show()
            return false
        }
        if (category.isEmpty()) {
            Toast.makeText(this, "Please enter category", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validateStep2(): Boolean {
        val pickup = binding.etPickupAddress.text.toString().trim()
        val dropoff = binding.etDropoffAddress.text.toString().trim()

        if (pickup.isEmpty()) {
            Toast.makeText(this, "Please enter pickup address", Toast.LENGTH_SHORT).show()
            return false
        }
        if (dropoff.isEmpty()) {
            Toast.makeText(this, "Please enter dropoff address", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun calculateDistanceAndMoveToStep3() {
        if (isCalculatingDistance) return

        val pickup = binding.etPickupAddress.text.toString().trim()
        val dropoff = binding.etDropoffAddress.text.toString().trim()
        val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: 1.0

        android.util.Log.d("CreateDelivery", "Calculating distance for pickup='$pickup', dropoff='$dropoff', weight=$weight")

        if (pickup.isEmpty() || dropoff.isEmpty()) {
            android.util.Log.d("CreateDelivery", "Addresses empty, using fallback")
            val fallbackDistance = 5.0
            val fallbackCost = calculateEstimatedCost(fallbackDistance, weight)
            updateDistanceDisplay(fallbackDistance, fallbackCost)
            goToStep(3)
            return
        }

        isCalculatingDistance = true
        showLoading(true)
        deliveryViewModel.calculateDistance(pickup, dropoff, weight)
    }

    private fun goToStep(step: Int) {
        currentStep = step
        updateStepUI()

        if (step == 3) {
            updateStep3Total()
        }
    }

    private fun updateStepUI() {
        val step1Active = currentStep >= 1
        val step2Active = currentStep >= 2
        val step3Active = currentStep >= 3

        binding.step1Indicator.setTextColor(getColor(if (step1Active) R.color.quickparcel_blue else R.color.quickparcel_gray))
        binding.step2Indicator.setTextColor(getColor(if (step2Active) R.color.quickparcel_blue else R.color.quickparcel_gray))
        binding.step3Indicator.setTextColor(getColor(if (step3Active) R.color.quickparcel_blue else R.color.quickparcel_gray))

        val dividerColor = if (step2Active) getColor(R.color.quickparcel_blue) else getColor(R.color.quickparcel_gray)
        findViewById<android.view.View>(R.id.step_divider_1)?.setBackgroundColor(dividerColor)
        findViewById<android.view.View>(R.id.step_divider_2)?.setBackgroundColor(dividerColor)

        binding.step1Container.visibility = if (currentStep == 1) android.view.View.VISIBLE else android.view.View.GONE
        binding.step2Container.visibility = if (currentStep == 2) android.view.View.VISIBLE else android.view.View.GONE
        binding.step3Container.visibility = if (currentStep == 3) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun updateDistanceDisplay(distance: Double, cost: Double) {
        calculatedDistance = distance
        calculatedCost = cost

        binding.cardDistance.visibility = android.view.View.VISIBLE
        binding.tvDistanceValue.text = String.format("%.2f km", distance)
        binding.tvCostValue.text = String.format("₱%.2f", cost)

        android.util.Log.d("CreateDelivery", "Distance: $distance km, Cost: ₱$cost")
    }

    private fun updateStep3Total() {
        val total = if (calculatedCost > 0) calculatedCost else {
            val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: 1.0
            calculateEstimatedCost(calculatedDistance, weight)
        }
        binding.tvPaymentTotal.text = "Total: ₱${String.format("%.2f", total)}"
    }

    private fun calculateEstimatedCost(distance: Double, weight: Double): Double {
        val baseFare = 50.0
        val perKmRate = 20.0
        val weightSurcharge = maxOf(0.0, (weight - 2) * 10)
        return baseFare + (distance * perKmRate) + weightSurcharge
    }

    private fun createDelivery() {
        val name = binding.etParcelName.text.toString().trim()
        val description = binding.etParcelDescription.text.toString().trim()
        val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: 0.0
        val category = binding.etCategory.text.toString().trim()
        val pickup = binding.etPickupAddress.text.toString().trim()
        val dropoff = binding.etDropoffAddress.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()
        val scheduledTimeRaw = binding.etScheduledTime.text.toString().trim()
        val isFragile = binding.cbFragile.isChecked
        val paymentMethod = if (binding.rbCod.isChecked) "COD" else "STRIPE"

        val scheduledTime = if (scheduledTimeRaw.isNotEmpty()) scheduledTimeRaw else null

        showLoading(true)
        deliveryViewModel.createDelivery(
            parcelName = name,
            parcelDescription = description.takeIf { it.isNotEmpty() },
            parcelWeight = weight,
            parcelSize = selectedSize,
            parcelCategory = category,
            isFragile = isFragile,
            pickupAddress = pickup,
            dropoffAddress = dropoff,
            notes = notes.takeIf { it.isNotEmpty() },
            scheduledTime = scheduledTime,
            paymentMethod = paymentMethod,
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            dropoffLat = dropoffLat,
            dropoffLng = dropoffLng
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            deliveryViewModel.distanceResult.collect { state ->
                isCalculatingDistance = false
                when (state) {
                    is DistanceState.Loading -> { }
                    is DistanceState.Success -> {
                        showLoading(false)
                        updateDistanceDisplay(state.distance, state.estimatedCost)
                        goToStep(3)
                    }
                    is DistanceState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@CreateDeliveryActivity, state.message, Toast.LENGTH_SHORT).show()
                        val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: 1.0
                        val fallbackDistance = 5.0
                        val fallbackCost = calculateEstimatedCost(fallbackDistance, weight)
                        updateDistanceDisplay(fallbackDistance, fallbackCost)
                        goToStep(3)
                    }
                }
            }
        }

        lifecycleScope.launch {
            deliveryViewModel.createResult.collect { state ->
                when (state) {
                    is CreateDeliveryState.Loading -> { }
                    is CreateDeliveryState.Success -> {
                        showLoading(false)
                        val paymentMethod = if (binding.rbCod.isChecked) "COD" else "STRIPE"

                        Toast.makeText(
                            this@CreateDeliveryActivity,
                            "✅ Delivery created successfully!\nTracking: ${state.delivery.trackingNumber}",
                            Toast.LENGTH_LONG
                        ).show()

                        if (paymentMethod == "COD") {
                            startActivity(Intent(this@CreateDeliveryActivity, SenderDashboardActivity::class.java))
                            finish()
                        } else {
                            val intent = Intent(this@CreateDeliveryActivity, PaymentActivity::class.java)
                            intent.putExtra("delivery_id", state.delivery.id)
                            intent.putExtra("amount", calculatedCost)
                            intent.putExtra("tracking_number", state.delivery.trackingNumber)
                            startActivity(intent)
                            finish()
                        }
                    }
                    is CreateDeliveryState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@CreateDeliveryActivity, "❌ Failed: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnNextStep1.isEnabled = !show
        binding.btnNextStep2.isEnabled = !show
        binding.btnConfirm.isEnabled = !show
    }
}