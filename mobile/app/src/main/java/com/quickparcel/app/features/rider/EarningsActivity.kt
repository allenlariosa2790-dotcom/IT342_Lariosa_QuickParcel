package com.quickparcel.app.features.rider

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityEarningsBinding
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Define WeeklyEarning data class locally
data class WeeklyEarning(
    val day: String,
    val earnings: Double,
    val deliveries: Int
)

class EarningsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEarningsBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var riderViewModel: RiderViewModel
    private lateinit var earningsAdapter: EarningsAdapter

    private var filter = "all" // all, thisWeek, thisMonth
    private var allDeliveries: List<com.quickparcel.app.shared.models.Delivery> = emptyList()
    private val weeklyData = mutableListOf<WeeklyEarning>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEarningsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)
        retrofitClient = RetrofitClient(tokenManager)
        riderViewModel = RiderViewModel(retrofitClient)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        riderViewModel.loadDashboardData()
    }

    private fun setupRecyclerView() {
        earningsAdapter = EarningsAdapter(emptyList())
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = earningsAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnFilterAll.setOnClickListener {
            filter = "all"
            updateFilterButtons()
            filterTransactions()
        }

        binding.btnFilterWeek.setOnClickListener {
            filter = "thisWeek"
            updateFilterButtons()
            filterTransactions()
        }

        binding.btnFilterMonth.setOnClickListener {
            filter = "thisMonth"
            updateFilterButtons()
            filterTransactions()
        }
    }

    private fun updateFilterButtons() {
        binding.btnFilterAll.isSelected = filter == "all"
        binding.btnFilterWeek.isSelected = filter == "thisWeek"
        binding.btnFilterMonth.isSelected = filter == "thisMonth"

        binding.btnFilterAll.backgroundTintList = if (filter == "all") {
            androidx.core.content.ContextCompat.getColorStateList(this, R.color.quickparcel_blue)
        } else {
            androidx.core.content.ContextCompat.getColorStateList(this, R.color.quickparcel_gray)
        }

        binding.btnFilterWeek.backgroundTintList = if (filter == "thisWeek") {
            androidx.core.content.ContextCompat.getColorStateList(this, R.color.quickparcel_blue)
        } else {
            androidx.core.content.ContextCompat.getColorStateList(this, R.color.quickparcel_gray)
        }

        binding.btnFilterMonth.backgroundTintList = if (filter == "thisMonth") {
            androidx.core.content.ContextCompat.getColorStateList(this, R.color.quickparcel_blue)
        } else {
            androidx.core.content.ContextCompat.getColorStateList(this, R.color.quickparcel_gray)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            riderViewModel.dashboardResult.collect { state ->
                when (state) {
                    is RiderDashboardState.Loading -> showLoading(true)
                    is RiderDashboardState.Success -> {
                        showLoading(false)
                        processEarningsData(state.recentDeliveries)
                    }
                    is RiderDashboardState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@EarningsActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun processEarningsData(deliveries: List<com.quickparcel.app.shared.models.Delivery>) {
        // Filter only completed deliveries
        val completed = deliveries.filter { it.status == "DELIVERED" }
        allDeliveries = completed

        // Calculate stats
        val totalEarnings = completed.sumOf { it.estimatedCost }
        val avgPerDelivery = if (completed.isNotEmpty()) totalEarnings / completed.size else 0.0

        binding.tvTotalEarnings.text = "₱${String.format("%.2f", totalEarnings)}"
        binding.tvCompletedCount.text = completed.size.toString()
        binding.tvAverageEarnings.text = "₱${String.format("%.2f", avgPerDelivery)}"

        // Calculate today's earnings
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val todayEarnings = completed.filter { delivery ->
            val date = parseDate(delivery.deliveredTime ?: delivery.updatedAt ?: delivery.createdAt)
            date != null && date >= today.time
        }.sumOf { it.estimatedCost }

        binding.tvTodayEarnings.text = "₱${String.format("%.2f", todayEarnings)}"

        // Calculate this week earnings
        val weekAgo = Calendar.getInstance()
        weekAgo.add(Calendar.DAY_OF_YEAR, -7)

        val weekEarnings = completed.filter { delivery ->
            val date = parseDate(delivery.deliveredTime ?: delivery.updatedAt ?: delivery.createdAt)
            date != null && date >= weekAgo.time
        }.sumOf { it.estimatedCost }

        binding.tvWeekEarnings.text = "₱${String.format("%.2f", weekEarnings)}"

        // Calculate last week earnings
        val twoWeeksAgo = Calendar.getInstance()
        twoWeeksAgo.add(Calendar.DAY_OF_YEAR, -14)

        val lastWeekEarnings = completed.filter { delivery ->
            val date = parseDate(delivery.deliveredTime ?: delivery.updatedAt ?: delivery.createdAt)
            date != null && date >= twoWeeksAgo.time && date < weekAgo.time
        }.sumOf { it.estimatedCost }

        binding.tvLastWeekEarnings.text = "₱${String.format("%.2f", lastWeekEarnings)}"

        // Prepare weekly chart data (last 7 days)
        weeklyData.clear()

        for (i in 6 downTo 0) {
            val date = Calendar.getInstance()
            date.add(Calendar.DAY_OF_YEAR, -i)
            date.set(Calendar.HOUR_OF_DAY, 0)
            date.set(Calendar.MINUTE, 0)
            date.set(Calendar.SECOND, 0)

            val nextDay = Calendar.getInstance()
            nextDay.time = date.time
            nextDay.add(Calendar.DAY_OF_YEAR, 1)

            val dayEarnings = completed.filter { delivery ->
                val deliveryDate = parseDate(delivery.deliveredTime ?: delivery.updatedAt ?: delivery.createdAt)
                deliveryDate != null && deliveryDate >= date.time && deliveryDate < nextDay.time
            }.sumOf { it.estimatedCost }

            val dayDeliveries = completed.count { delivery ->
                val deliveryDate = parseDate(delivery.deliveredTime ?: delivery.updatedAt ?: delivery.createdAt)
                deliveryDate != null && deliveryDate >= date.time && deliveryDate < nextDay.time
            }

            val dayName = when (date.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                Calendar.SUNDAY -> "Sun"
                else -> "Mon"
            }

            weeklyData.add(WeeklyEarning(dayName, dayEarnings, dayDeliveries))
        }

        drawChart()

        // Filter transactions
        filterTransactions()
    }

    private fun drawChart() {
        val maxEarnings = weeklyData.maxOfOrNull { it.earnings } ?: 100.0

        val bars = listOf(
            binding.bar1 to binding.barValue1 to binding.barDeliveries1,
            binding.bar2 to binding.barValue2 to binding.barDeliveries2,
            binding.bar3 to binding.barValue3 to binding.barDeliveries3,
            binding.bar4 to binding.barValue4 to binding.barDeliveries4,
            binding.bar5 to binding.barValue5 to binding.barDeliveries5,
            binding.bar6 to binding.barValue6 to binding.barDeliveries6,
            binding.bar7 to binding.barValue7 to binding.barDeliveries7
        )
        val days = listOf(
            binding.barDay1, binding.barDay2, binding.barDay3, binding.barDay4,
            binding.barDay5, binding.barDay6, binding.barDay7
        )

        weeklyData.forEachIndexed { index, data ->
            if (index < bars.size) {
                val (barWithValue, deliveriesText) = bars[index]
                val (bar, valueText) = barWithValue
                val dayText = days[index]

                val height = if (maxEarnings > 0) (data.earnings / maxEarnings * 120).toInt() else 0
                val heightPx = (height * resources.displayMetrics.density).toInt()

                bar.layoutParams?.height = if (heightPx > 0) heightPx else 4
                bar.requestLayout()

                valueText.text = if (data.earnings > 0) "₱${String.format("%.0f", data.earnings)}" else ""
                dayText.text = data.day
                deliveriesText.text = if (data.deliveries > 0) "${data.deliveries} del" else ""
                deliveriesText.visibility = if (data.deliveries > 0) android.view.View.VISIBLE else android.view.View.GONE
            }
        }

        // Show/hide chart based on data
        val hasData = weeklyData.any { it.earnings > 0 }
        binding.chartContainer.visibility = if (hasData) android.view.View.VISIBLE else android.view.View.GONE
        binding.tvNoChartData.visibility = if (hasData) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun filterTransactions() {
        val filtered = when (filter) {
            "thisWeek" -> {
                val weekAgo = Calendar.getInstance()
                weekAgo.add(Calendar.DAY_OF_YEAR, -7)
                allDeliveries.filter { delivery ->
                    val date = parseDate(delivery.deliveredTime ?: delivery.updatedAt ?: delivery.createdAt)
                    date != null && date >= weekAgo.time
                }
            }
            "thisMonth" -> {
                val monthAgo = Calendar.getInstance()
                monthAgo.add(Calendar.DAY_OF_YEAR, -30)
                allDeliveries.filter { delivery ->
                    val date = parseDate(delivery.deliveredTime ?: delivery.updatedAt ?: delivery.createdAt)
                    date != null && date >= monthAgo.time
                }
            }
            else -> allDeliveries
        }

        val sorted = filtered.sortedByDescending { delivery ->
            parseDate(delivery.deliveredTime ?: delivery.updatedAt ?: delivery.createdAt)
        }

        earningsAdapter.updateTransactions(sorted)

        if (filtered.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvTransactions.visibility = android.view.View.GONE
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.rvTransactions.visibility = android.view.View.VISIBLE
        }
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd HH:mm:ss"
            )
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    return sdf.parse(dateString)
                } catch (e: Exception) {
                    // Try next format
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
}