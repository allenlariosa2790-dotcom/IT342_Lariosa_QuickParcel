package edu.cit.lariosa.quickparcel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.lariosa.quickparcel.R
import edu.cit.lariosa.quickparcel.adapters.AvailableDeliveryAdapter
import edu.cit.lariosa.quickparcel.data.AvailableDelivery

class RiderDashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvTodayEarnings: TextView
    private lateinit var tvNearbyCount: TextView
    private lateinit var tvAvailableCount: TextView
    private lateinit var tvCompletedCount: TextView
    private lateinit var tvRating: TextView
    private lateinit var rvAvailableDeliveries: RecyclerView
    private lateinit var btnNotification: FrameLayout
    private lateinit var tvViewAll: TextView
    private lateinit var navHome: LinearLayout
    private lateinit var navDeliveries: LinearLayout
    private lateinit var navEarnings: LinearLayout
    private lateinit var navProfile: LinearLayout
    private lateinit var sharedPref: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rider_dashboard)

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome)
        tvTodayEarnings = findViewById(R.id.tvTodayEarnings)
        tvNearbyCount = findViewById(R.id.tvNearbyCount)
        tvAvailableCount = findViewById(R.id.tvAvailableCount)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)
        tvRating = findViewById(R.id.tvRating)
        rvAvailableDeliveries = findViewById(R.id.rvAvailableDeliveries)
        btnNotification = findViewById(R.id.btnNotification)
        tvViewAll = findViewById(R.id.tvViewAll)
        navHome = findViewById(R.id.navHome)
        navDeliveries = findViewById(R.id.navDeliveries)
        navEarnings = findViewById(R.id.navEarnings)
        navProfile = findViewById(R.id.navProfile)
        sharedPref = getSharedPreferences("quickparcel_prefs", MODE_PRIVATE)

        // Set user data from SharedPreferences
        val firstName = sharedPref.getString("firstName", "Rider") ?: "Rider"
        tvWelcome.text = "$firstName!"

        // Set mock data
        tvTodayEarnings.text = "$42"
        tvNearbyCount.text = "3 Deliveries Nearby"
        tvAvailableCount.text = "3"
        tvCompletedCount.text = "2"
        tvRating.text = "4.8"

        // Setup RecyclerView
        setupRecyclerView()

        // Setup click listeners
        btnNotification.setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
        }

        tvViewAll.setOnClickListener {
            Toast.makeText(this, "View All Deliveries", Toast.LENGTH_SHORT).show()
        }

        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        val deliveries = listOf(
                AvailableDelivery("QP-2301", "Downtown Mall", "Oak Street 123", "$12", "2.3 km"),
                AvailableDelivery("QP-2302", "Tech Store", "Pine Avenue 45", "$15", "3.1 km"),
                AvailableDelivery("QP-2303", "Fashion Boutique", "Maple Road 89", "$10", "1.8 km")
        )

        rvAvailableDeliveries.layoutManager = LinearLayoutManager(this)
        rvAvailableDeliveries.adapter = AvailableDeliveryAdapter(deliveries)
    }

    private fun setupBottomNavigation() {
        navHome.setOnClickListener {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }

        navDeliveries.setOnClickListener {
            Toast.makeText(this, "My Deliveries", Toast.LENGTH_SHORT).show()
        }

        navEarnings.setOnClickListener {
            Toast.makeText(this, "Earnings - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        navProfile.setOnClickListener {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        // Optional: Handle back button press
        super.onBackPressed()
    }
}