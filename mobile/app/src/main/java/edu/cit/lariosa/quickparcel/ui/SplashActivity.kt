package edu.cit.lariosa.quickparcel.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import edu.cit.lariosa.quickparcel.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val sharedPref = getSharedPreferences("quickparcel_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        val userType = sharedPref.getString("userType", null)

        Handler().postDelayed({
            if (token != null && token.isNotEmpty()) {
                when (userType) {
                    "SENDER" -> startActivity(Intent(this, SenderDashboardActivity::class.java))
                    "RIDER" -> startActivity(Intent(this, RiderDashboardActivity::class.java))
                    else -> startActivity(Intent(this, LoginActivity::class.java))
                }
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }
}