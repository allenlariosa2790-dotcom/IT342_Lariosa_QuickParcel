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

class EarningsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEarningsBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var riderViewModel: RiderViewModel
    private lateinit var earningsAdapter: EarningsAdapter

    private var filter = "all" // all, thisWeek, thisMonth
    private var allDeliveries: List<com.quickparcel.app.shared.models.Delivery> = emptyList()
    private val weeklyData = mutableListOf<RiderModels.WeeklyEarning>()

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

        val bgRes = R.drawable.bg_status_accepted
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

        val todayEarnings = completed.filter {
            val date = parseDate(it.deliveredTime ?: it.updatedAt ?: it.createdAt)
            date != null && date >= today.time
        }.sumOf { it.estimatedCost }

        binding.tvTodayEarnings.text = "₱${String.format("%.2f", todayEarnings)}"

        // Calculate this week earnings
        val weekAgo = Calendar.getInstance()
        weekAgo.add(Calendar.DAY_OF_YEAR, -7)

        val weekEarnings = completed.filter {
            val date = parseDate(it.deliveredTime ?: it.updatedAt ?: it.createdAt)
            date != null && date >= weekAgo.time
        }.sumOf { it.estimatedCost }

        binding.tvWeekEarnings.text = "₱${String.format("%.2f", weekEarnings)}"

        // Calculate last week earnings
        val twoWeeksAgo = Calendar.getInstance()
        twoWeeksAgo.add(Calendar.DAY_OF_YEAR, -14)

        val lastWeekEarnings = completed.filter {
            val date = parseDate(it.deliveredTime ?: it.updatedAt ?: it.createdAt)
            date != null && date >= twoWeeksAgo.time && date < weekAgo.time
        }.sumOf { it.estimatedCost }

        binding.tvLastWeekEarnings.text = "₱${String.format("%.2f", lastWeekEarnings)}"

        // Prepare weekly chart data (last 7 days)
        weeklyData.clear()
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 6 downTo 0) {
            val date = Calendar.getInstance()
            date.add(Calendar.DAY_OF_YEAR, -i)
            date.set(Calendar.HOUR_OF_DAY, 0)
            date.set(Calendar.MINUTE, 0)
            date.set(Calendar.SECOND, 0)

            val nextDay = Calendar.getInstance()
            nextDay.time = date.time
            nextDay.add(Calendar.DAY_OF_YEAR, 1)

            val dayEarnings = completed.filter {
                val deliveryDate = parseDate(it.deliveredTime ?: it.updatedAt ?: it.createdAt)
                deliveryDate != null && deliveryDate >= date.time && deliveryDate < nextDay.time
            }.sumOf { it.estimatedCost }

            val dayDeliveries = completed.count {
                val deliveryDate = parseDate(it.deliveredTime ?: it.updatedAt ?: it.createdAt)
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

            weeklyData.add(RiderModels.WeeklyEarning(dayName, dayEarnings, dayDeliveries))
        }

        drawChart()

        // Filter transactions
        filterTransactions()
    }

    private fun drawChart() {
        val maxEarnings = weeklyData.maxOfOrNull { it.earnings } ?: 100.0
        val chartHeight = 150 // dp equivalent in pixels will be handled in code

        // Find bar containers
        val barContainers = listOf(
            binding.barContainer1, binding.barContainer2, binding.barContainer3,
            binding.barContainer4, binding.barContainer5, binding.barContainer6, binding.barContainer7
        )

        weeklyData.forEachIndexed { index, data ->
            if (index < barContainers.size) {
                val container = barContainers[index]
                val bar = when (index) {
                    0 -> binding.bar1
                    1 -> binding.bar2
                    2 -> binding.bar3
                    3 -> binding.bar4
                    4 -> binding.bar5
                    5 -> binding.bar6
                    6 -> binding.bar7
                    else -> null
                }

                val valueText = when (index) {
                    0 -> binding.barValue1
                    1 -> binding.barValue2
                    2 -> binding.barValue3
                    3 -> binding.barValue4
                    4 -> binding.barValue5
                    5 -> binding.barValue6
                    6 -> binding.barValue7
                    else -> null
                }

                val dayText = when (index) {
                    0 -> binding.barDay1
                    1 -> binding.barDay2
                    2 -> binding.barDay3
                    3 -> binding.barDay4
                    4 -> binding.barDay5
                    5 -> binding.barDay6
                    6 -> binding.barDay7
                    else -> null
                }

                val deliveriesText = when (index) {
                    0 -> binding.barDeliveries1
                    1 -> binding.barDeliveries2
                    2 -> binding.barDeliveries3
                    3 -> binding.barDeliveries4
                    4 -> binding.barDeliveries5
                    5 -> binding.barDeliveries6
                    6 -> binding.barDeliveries7
                    else -> null
                }

                // Set height (max 120dp)
                val height = if (maxEarnings > 0) (data.earnings / maxEarnings * 120).toInt() else 0
                val heightPx = (height * resources.displayMetrics.density).toInt()

                bar?.layoutParams?.height = if (heightPx > 0) heightPx else 4
                bar?.requestLayout()

                valueText?.text = if (data.earnings > 0) "₱${String.format("%.0f", data.earnings)}" else ""
                dayText?.text = data.day
                deliveriesText?.text = if (data.deliveries > 0) "${data.deliveries} del" else ""
                deliveriesText?.visibility = if (data.deliveries > 0) android.view.View.VISIBLE else android.view.View.GONE
            }
        }

        // Show/hide chart based on data
        val hasData = weeklyData.any { it.earnings > 0 }
        binding.chartContainer.visibility = if (hasData) android.view.View.VISIBLE else android.view.View.GONE
        binding.tvNoChartData.visibility = if (hasData) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun filterTransactions() {
        val now = Calendar.getInstance()
        val filtered = when (filter) {
            "thisWeek" -> {
                val weekAgo = Calendar.getInstance()
                weekAgo.add(Calendar.DAY_OF_YEAR, -7)
                allDeliveries.filter {
                    val date = parseDate(it.deliveredTime ?: it.updatedAt ?: it.createdAt)
                    date != null && date >= weekAgo.time
                }
            }
            "thisMonth" -> {
                val monthAgo = Calendar.getInstance()
                monthAgo.add(Calendar.DAY_OF_YEAR, -30)
                allDeliveries.filter {
                    val date = parseDate(it.deliveredTime ?: it.updatedAt ?: it.createdAt)
                    date != null && date >= monthAgo.time
                }
            }
            else -> allDeliveries
        }

        earningsAdapter.updateTransactions(filtered.sortedByDescending {
            parseDate(it.deliveredTime ?: it.updatedAt ?: it.createdAt)
        })

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