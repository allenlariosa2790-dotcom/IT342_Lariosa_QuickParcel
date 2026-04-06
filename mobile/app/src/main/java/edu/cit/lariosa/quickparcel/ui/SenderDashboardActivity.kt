package edu.cit.lariosa.quickparcel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.lariosa.quickparcel.R
import edu.cit.lariosa.quickparcel.adapters.ActiveDeliveryAdapter
import edu.cit.lariosa.quickparcel.adapters.RecentDeliveryAdapter
import edu.cit.lariosa.quickparcel.data.ActiveDelivery
import edu.cit.lariosa.quickparcel.data.RecentDelivery

class SenderDashboardActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var btnCreateDelivery: Button
    private lateinit var btnNotification: FrameLayout
    private lateinit var tvActiveCount: TextView
    private lateinit var tvDeliveredCount: TextView
    private lateinit var tvTotalSpent: TextView
    private lateinit var rvActiveDeliveries: RecyclerView
    private lateinit var rvRecentDeliveries: RecyclerView
    private lateinit var tvViewAll: TextView
    private lateinit var navHome: LinearLayout
    private lateinit var navCreate: LinearLayout
    private lateinit var navHistory: LinearLayout
    private lateinit var navProfile: LinearLayout
    private lateinit var sharedPref: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender_dashboard)

        // Initialize views
        tvGreeting = findViewById(R.id.tvGreeting)
        btnCreateDelivery = findViewById(R.id.btnCreateDelivery)
        btnNotification = findViewById(R.id.btnNotification)
        tvActiveCount = findViewById(R.id.tvActiveCount)
        tvDeliveredCount = findViewById(R.id.tvDeliveredCount)
        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        rvActiveDeliveries = findViewById(R.id.rvActiveDeliveries)
        rvRecentDeliveries = findViewById(R.id.rvRecentDeliveries)
        tvViewAll = findViewById(R.id.tvViewAll)
        navHome = findViewById(R.id.navHome)
        navCreate = findViewById(R.id.navCreate)
        navHistory = findViewById(R.id.navHistory)
        navProfile = findViewById(R.id.navProfile)
        sharedPref = getSharedPreferences("quickparcel_prefs", MODE_PRIVATE)

        // Set user data from SharedPreferences
        val firstName = sharedPref.getString("firstName", "User") ?: "User"
        tvGreeting.text = "$firstName!"

        // Set mock data
        tvActiveCount.text = "2"
        tvDeliveredCount.text = "12"
        tvTotalSpent.text = "$168"

        // Setup RecyclerViews
        setupActiveDeliveries()
        setupRecentDeliveries()

        // Setup click listeners
        btnCreateDelivery.setOnClickListener {
            Toast.makeText(this, "Create Delivery - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        btnNotification.setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
        }

        tvViewAll.setOnClickListener {
            Toast.makeText(this, "View All Deliveries", Toast.LENGTH_SHORT).show()
        }

        setupBottomNavigation()
    }

    private fun setupActiveDeliveries() {
        val deliveries = listOf(
                ActiveDelivery("QP-2301", "In Transit", "Downtown Mall", "Oak Street 123", "Michael J.", "15 min"),
                ActiveDelivery("QP-2299", "Picked Up", "Tech Store", "Pine Avenue 45", "Sarah K.", "25 min")
        )

        rvActiveDeliveries.layoutManager = LinearLayoutManager(this)
        rvActiveDeliveries.adapter = ActiveDeliveryAdapter(deliveries)
    }

    private fun setupRecentDeliveries() {
        val deliveries = listOf(
                RecentDelivery("QP-2298", "Oak Street 123", "Feb 28, 2026", "25 min"),
                RecentDelivery("QP-2297", "Pine Avenue 45", "Feb 28, 2026", "25 min")
        )

        rvRecentDeliveries.layoutManager = LinearLayoutManager(this)
        rvRecentDeliveries.adapter = RecentDeliveryAdapter(deliveries)
    }

    private fun setupBottomNavigation() {
        navHome.setOnClickListener {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }

        navCreate.setOnClickListener {
            Toast.makeText(this, "Create Delivery", Toast.LENGTH_SHORT).show()
        }

        navHistory.setOnClickListener {
            Toast.makeText(this, "Delivery History", Toast.LENGTH_SHORT).show()
        }

        navProfile.setOnClickListener {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
        }

        // Add logout option if needed (maybe in profile section)
    }
}