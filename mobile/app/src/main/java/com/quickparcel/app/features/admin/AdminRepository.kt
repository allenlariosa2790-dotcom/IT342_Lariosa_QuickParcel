package com.quickparcel.app.features.admin

import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminRepository(
    private val retrofitClient: RetrofitClient
) {
    private val api = retrofitClient.create(AdminApiService::class.java)

    suspend fun getDashboardStats(): AdminModels.StatsResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getDashboardStats()
                if (response.isSuccessful && response.body() != null) {
                    AdminModels.StatsResult.Success(response.body()!!.data)
                } else {
                    AdminModels.StatsResult.Error(response.message() ?: "Failed to load stats")
                }
            } catch (e: Exception) {
                AdminModels.StatsResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getAllUsers(): AdminModels.UsersResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAllUsers()
                if (response.isSuccessful && response.body() != null) {
                    AdminModels.UsersResult.Success(response.body()!!.data)
                } else {
                    AdminModels.UsersResult.Error(response.message() ?: "Failed to load users")
                }
            } catch (e: Exception) {
                AdminModels.UsersResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun updateUserStatus(userId: Int, isActive: Boolean): AdminModels.UserStatusResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updateUserStatus(userId, AdminModels.UpdateUserStatusRequest(isActive))
                if (response.isSuccessful && response.body() != null) {
                    AdminModels.UserStatusResult.Success(response.body()!!.message)
                } else {
                    AdminModels.UserStatusResult.Error(response.message() ?: "Failed to update user status")
                }
            } catch (e: Exception) {
                AdminModels.UserStatusResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getAllDeliveries(): AdminModels.DeliveriesResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAllDeliveries()
                if (response.isSuccessful && response.body() != null) {
                    AdminModels.DeliveriesResult.Success(response.body()!!.data)
                } else {
                    AdminModels.DeliveriesResult.Error(response.message() ?: "Failed to load deliveries")
                }
            } catch (e: Exception) {
                AdminModels.DeliveriesResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun cancelDelivery(deliveryId: Int): AdminModels.CancelDeliveryResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.cancelDelivery(deliveryId)
                if (response.isSuccessful && response.body() != null) {
                    AdminModels.CancelDeliveryResult.Success(response.body()!!.message)
                } else {
                    AdminModels.CancelDeliveryResult.Error(response.message() ?: "Failed to cancel delivery")
                }
            } catch (e: Exception) {
                AdminModels.CancelDeliveryResult.Error(e.message ?: "Network error")
            }
        }
    }
}