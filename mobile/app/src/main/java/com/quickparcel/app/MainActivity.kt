package com.quickparcel.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.quickparcel.app.features.auth.LoginActivity
import com.quickparcel.app.features.rider.RiderDashboardActivity
import com.quickparcel.app.features.sender.SenderDashboardActivity
import com.quickparcel.app.shared.datastore.TokenManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenManager = TokenManager(applicationContext)

        lifecycleScope.launch {
            val token = tokenManager.getToken()
            val userDataJson = tokenManager.getUserData()

            if (!token.isNullOrEmpty() && userDataJson != null) {
                try {
                    val gson = Gson()
                    val user = gson.fromJson(userDataJson, com.quickparcel.app.features.auth.AuthModels.JwtResponse::class.java)

                    val intent = when (user.userType) {
                        "SENDER" -> Intent(this@MainActivity, SenderDashboardActivity::class.java)
                        "RIDER" -> Intent(this@MainActivity, RiderDashboardActivity::class.java)
                        "ADMIN" -> Intent(this@MainActivity, com.quickparcel.app.features.admin.AdminDashboardActivity::class.java)
                        else -> Intent(this@MainActivity, LoginActivity::class.java)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                }
            } else {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
            finish()
        }
    }
}