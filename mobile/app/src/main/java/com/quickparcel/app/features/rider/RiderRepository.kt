package com.quickparcel.app.features.rider

import com.quickparcel.app.shared.models.Delivery
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RiderRepository(
    private val retrofitClient: RetrofitClient
) {
    private val api = retrofitClient.create(RiderApiService::class.java)

    suspend fun getAvailableDeliveries(): RiderModels.AvailableResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAvailableDeliveries()
                if (response.isSuccessful && response.body() != null) {
                    RiderModels.AvailableResult.Success(response.body()!!.data)
                } else {
                    RiderModels.AvailableResult.Error(response.message() ?: "Failed to load deliveries")
                }
            } catch (e: Exception) {
                RiderModels.AvailableResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun acceptDelivery(deliveryId: Int): RiderModels.AcceptResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.acceptDelivery(deliveryId)
                if (response.isSuccessful && response.body() != null) {
                    RiderModels.AcceptResult.Success(response.body()!!.data)
                } else {
                    RiderModels.AcceptResult.Error(response.message() ?: "Failed to accept delivery")
                }
            } catch (e: Exception) {
                RiderModels.AcceptResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getActiveDeliveries(): RiderModels.ActiveDeliveriesResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getActiveDeliveries()
                if (response.isSuccessful && response.body() != null) {
                    val deliveries = response.body()!!.data
                    // Filter only active ones (ACCEPTED, PICKED_UP, IN_TRANSIT)
                    val active = deliveries.filter {
                        it.status in listOf("ACCEPTED", "PICKED_UP", "IN_TRANSIT")
                    }
                    RiderModels.ActiveDeliveriesResult.Success(active)
                } else {
                    RiderModels.ActiveDeliveriesResult.Error(response.message() ?: "Failed to load active deliveries")
                }
            } catch (e: Exception) {
                RiderModels.ActiveDeliveriesResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun updateDeliveryStatus(deliveryId: Int, status: String, location: String): RiderModels.StatusResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updateDeliveryStatus(deliveryId, RiderModels.StatusUpdateRequest(status, location))
                if (response.isSuccessful && response.body() != null) {
                    RiderModels.StatusResult.Success(response.body()!!.data)
                } else {
                    RiderModels.StatusResult.Error(response.message() ?: "Failed to update status")
                }
            } catch (e: Exception) {
                RiderModels.StatusResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getMyDeliveries(): List<Delivery> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getMyDeliveries()
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.data
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}